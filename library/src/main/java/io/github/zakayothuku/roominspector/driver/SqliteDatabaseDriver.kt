package io.github.zakayothuku.roominspector.driver

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.zakayothuku.roominspector.model.ColumnInfo
import io.github.zakayothuku.roominspector.model.TableSchema

/**
 * Driver wrapping SupportSQLiteDatabase to inspect schemas and execute queries safely.
 */
class SqliteDatabaseDriver(
    private val db: SupportSQLiteDatabase
) {

    /**
     * Discovers all non-system user tables in the SQLite database.
     */
    fun getTableNames(): List<String> {
        val tables = mutableListOf<String>()
        val query = """
            SELECT name FROM sqlite_master 
            WHERE type='table' 
              AND name NOT LIKE 'sqlite_%' 
              AND name NOT LIKE 'room_master_table' 
              AND name NOT LIKE 'android_metadata'
            ORDER BY name ASC
        """.trimIndent()

        db.query(query).use { cursor ->
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                if (tableName.isNotBlank()) {
                    tables.add(tableName)
                }
            }
        }
        return tables
    }

    /**
     * Retrieves column schemas and total row count for a given table.
     */
    fun getTableSchema(tableName: String): TableSchema {
        val columns = mutableListOf<ColumnInfo>()
        val pragmaQuery = "PRAGMA table_info(`$tableName`)"

        db.query(pragmaQuery).use { cursor ->
            val cidIndex = cursor.getColumnIndex("cid")
            val nameIndex = cursor.getColumnIndex("name")
            val typeIndex = cursor.getColumnIndex("type")
            val notNullIndex = cursor.getColumnIndex("notnull")
            val dfltIndex = cursor.getColumnIndex("dflt_value")
            val pkIndex = cursor.getColumnIndex("pk")

            while (cursor.moveToNext()) {
                val cid = if (cidIndex != -1) cursor.getInt(cidIndex) else 0
                val name = if (nameIndex != -1) cursor.getString(nameIndex) else "unknown"
                val type = if (typeIndex != -1) cursor.getString(typeIndex) else "TEXT"
                val notNull = if (notNullIndex != -1) cursor.getInt(notNullIndex) == 1 else false
                val dfltValue = if (dfltIndex != -1 && !cursor.isNull(dfltIndex)) cursor.getString(dfltIndex) else null
                val isPk = if (pkIndex != -1) cursor.getInt(pkIndex) > 0 else false

                columns.add(
                    ColumnInfo(
                        cid = cid,
                        name = name,
                        type = type,
                        notNull = notNull,
                        defaultValue = dfltValue,
                        isPrimaryKey = isPk
                    )
                )
            }
        }

        val rowCount = getRowCount(tableName)
        return TableSchema(tableName = tableName, columns = columns, rowCount = rowCount)
    }

    /**
     * Gets the total row count of a table.
     */
    fun getRowCount(tableName: String): Long {
        return try {
            db.query("SELECT COUNT(*) FROM `$tableName`").use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Fetches a paginated page of records from a table.
     */
    fun getTablePage(
        tableName: String,
        page: Int = 0,
        pageSize: Int = 25,
        searchQuery: String? = null
    ): TablePageResult {
        val schema = getTableSchema(tableName)
        val offset = page * pageSize

        val baseSql = StringBuilder("SELECT * FROM `$tableName`")
        val filterSql = if (!searchQuery.isNullOrBlank() && schema.columns.isNotEmpty()) {
            val searchClauses = schema.columns.joinToString(" OR ") { "`${it.name}` LIKE '%${searchQuery.replace("'", "''")}%'" }
            " WHERE $searchClauses"
        } else {
            ""
        }

        val paginatedSql = "$baseSql$filterSql LIMIT $pageSize OFFSET $offset"
        val rows = mutableListOf<List<String?>>()

        try {
            db.query(paginatedSql).use { cursor ->
                val columnCount = cursor.columnCount
                while (cursor.moveToNext()) {
                    val row = (0 until columnCount).map { i ->
                        if (cursor.isNull(i)) null else cursor.getString(i)
                    }
                    rows.add(row)
                }
            }
        } catch (e: Exception) {
            // fallback gracefully
        }

        val totalCount = if (filterSql.isNotBlank()) {
            try {
                db.query("SELECT COUNT(*) FROM `$tableName`$filterSql").use {
                    if (it.moveToFirst()) it.getLong(0) else 0L
                }
            } catch (e: Exception) {
                schema.rowCount
            }
        } else {
            schema.rowCount
        }

        return TablePageResult(
            tableName = tableName,
            columns = schema.columns,
            rows = rows,
            totalRowCount = totalCount,
            page = page,
            pageSize = pageSize
        )
    }

    /**
     * Executes arbitrary raw SQL (SELECT, INSERT, UPDATE, DELETE, PRAGMA) and captures execution time.
     */
    fun executeSql(sql: String): QueryResult {
        val trimmed = sql.trim()
        val startTime = System.currentTimeMillis()

        return try {
            val isSelect = trimmed.startsWith("SELECT", ignoreCase = true) ||
                    trimmed.startsWith("PRAGMA", ignoreCase = true) ||
                    trimmed.startsWith("EXPLAIN", ignoreCase = true)

            if (isSelect) {
                db.query(trimmed).use { cursor ->
                    val columns = (0 until cursor.columnCount).map { cursor.getColumnName(it) }
                    val rows = mutableListOf<List<String?>>()
                    while (cursor.moveToNext()) {
                        val row = (0 until cursor.columnCount).map { i ->
                            if (cursor.isNull(i)) null else cursor.getString(i)
                        }
                        rows.add(row)
                    }
                    val duration = System.currentTimeMillis() - startTime
                    QueryResult(
                        isSuccess = true,
                        sql = trimmed,
                        columns = columns,
                        rows = rows,
                        affectedRows = rows.size,
                        executionTimeMs = duration
                    )
                }
            } else {
                db.execSQL(trimmed)
                val duration = System.currentTimeMillis() - startTime
                QueryResult(
                    isSuccess = true,
                    sql = trimmed,
                    columns = emptyList(),
                    rows = emptyList(),
                    affectedRows = 1,
                    executionTimeMs = duration
                )
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            QueryResult(
                isSuccess = false,
                sql = trimmed,
                executionTimeMs = duration,
                errorMessage = e.localizedMessage ?: "SQL execution error"
            )
        }
    }
}
