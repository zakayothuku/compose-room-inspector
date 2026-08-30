package io.github.zakayothuku.roominspector

import android.content.ContentResolver
import android.content.ContentValues
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.database.sqlite.SQLiteTransactionListener
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import java.util.Locale

class SimpleTestCursor(
    private val columnNamesArray: Array<String>,
    private val rowData: List<Array<Any?>>
) : Cursor {
    private var pos = -1

    override fun close() {}
    override fun isClosed(): Boolean = false
    override fun getCount(): Int = rowData.size
    override fun getPosition(): Int = pos
    override fun move(offset: Int): Boolean = moveToPosition(pos + offset)
    override fun moveToPosition(position: Int): Boolean {
        if (position in 0 until rowData.size) {
            pos = position
            return true
        }
        pos = if (position < 0) -1 else rowData.size
        return false
    }
    override fun moveToFirst(): Boolean = moveToPosition(0)
    override fun moveToLast(): Boolean = moveToPosition(rowData.size - 1)
    override fun moveToNext(): Boolean = moveToPosition(pos + 1)
    override fun moveToPrevious(): Boolean = moveToPosition(pos - 1)
    override fun isFirst(): Boolean = pos == 0 && rowData.isNotEmpty()
    override fun isLast(): Boolean = pos == rowData.size - 1 && rowData.isNotEmpty()
    override fun isBeforeFirst(): Boolean = pos < 0
    override fun isAfterLast(): Boolean = pos >= rowData.size
    override fun getColumnIndex(columnName: String): Int = columnNamesArray.indexOf(columnName)
    override fun getColumnIndexOrThrow(columnName: String): Int {
        val idx = getColumnIndex(columnName)
        if (idx == -1) throw IllegalArgumentException("Column $columnName not found")
        return idx
    }
    override fun getColumnName(columnIndex: Int): String = columnNamesArray[columnIndex]
    override fun getColumnNames(): Array<String> = columnNamesArray
    override fun getColumnCount(): Int = columnNamesArray.size
    override fun getString(columnIndex: Int): String? = rowData[pos][columnIndex]?.toString()
    override fun getShort(columnIndex: Int): Short = getLong(columnIndex).toShort()
    override fun getInt(columnIndex: Int): Int = getLong(columnIndex).toInt()
    override fun getLong(columnIndex: Int): Long = (rowData[pos][columnIndex] as? Number)?.toLong()
        ?: rowData[pos][columnIndex]?.toString()?.toLongOrNull() ?: 0L
    override fun getFloat(columnIndex: Int): Float = getDouble(columnIndex).toFloat()
    override fun getDouble(columnIndex: Int): Double = (rowData[pos][columnIndex] as? Number)?.toDouble()
        ?: rowData[pos][columnIndex]?.toString()?.toDoubleOrNull() ?: 0.0
    override fun getType(columnIndex: Int): Int = if (isNull(columnIndex)) Cursor.FIELD_TYPE_NULL else Cursor.FIELD_TYPE_STRING
    override fun isNull(columnIndex: Int): Boolean = rowData[pos][columnIndex] == null
    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {}
    override fun getBlob(columnIndex: Int): ByteArray? = null
    @Deprecated("Deprecated in Java")
    override fun deactivate() {}
    @Deprecated("Deprecated in Java")
    override fun requery(): Boolean = true
    override fun registerContentObserver(observer: ContentObserver?) {}
    override fun unregisterContentObserver(observer: ContentObserver?) {}
    override fun registerDataSetObserver(observer: DataSetObserver?) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}
    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {}
    override fun getNotificationUri(): Uri? = null
    override fun getWantsAllOnMoveCalls(): Boolean = false
    override fun setExtras(extras: Bundle?) {}
    override fun getExtras(): Bundle = Bundle.EMPTY
    override fun respond(extras: Bundle?): Bundle = Bundle.EMPTY
}

class FakeSupportSQLiteDatabase : SupportSQLiteDatabase {

    private val tables = mutableMapOf<String, MutableList<MutableMap<String, Any?>>>()
    private val schemas = mutableMapOf<String, List<Map<String, Any?>>>()

    init {
        // Initialize default test schema
        schemas["users"] = listOf(
            mapOf("cid" to 0, "name" to "id", "type" to "INTEGER", "notnull" to 1, "dflt_value" to null, "pk" to 1),
            mapOf("cid" to 1, "name" to "username", "type" to "TEXT", "notnull" to 1, "dflt_value" to null, "pk" to 0),
            mapOf("cid" to 2, "name" to "email", "type" to "TEXT", "notnull" to 0, "dflt_value" to null, "pk" to 0),
            mapOf("cid" to 3, "name" to "score", "type" to "REAL", "notnull" to 0, "dflt_value" to null, "pk" to 0)
        )
        tables["users"] = mutableListOf(
            mutableMapOf("id" to "1", "username" to "alice", "email" to "alice@test.com", "score" to "95.5"),
            mutableMapOf("id" to "2", "username" to "bob", "email" to "bob@test.com", "score" to "82.0"),
            mutableMapOf("id" to "3", "username" to "charlie", "email" to "charlie@test.com", "score" to "74.0")
        )

        schemas["products"] = listOf(
            mapOf("cid" to 0, "name" to "id", "type" to "INTEGER", "notnull" to 1, "dflt_value" to null, "pk" to 1),
            mapOf("cid" to 1, "name" to "title", "type" to "TEXT", "notnull" to 1, "dflt_value" to null, "pk" to 0),
            mapOf("cid" to 2, "name" to "price", "type" to "REAL", "notnull" to 0, "dflt_value" to null, "pk" to 0)
        )
        tables["products"] = mutableListOf()
    }

    override fun query(query: String): Cursor {
        val trimmed = query.trim()
        if (trimmed.contains("sqlite_master", ignoreCase = true)) {
            val rows = tables.keys.map { arrayOf<Any?>(it) }
            return SimpleTestCursor(arrayOf("name"), rows)
        }

        if (trimmed.startsWith("PRAGMA table_info", ignoreCase = true)) {
            val tableName = if (trimmed.contains("`")) {
                trimmed.substringAfter("`").substringBefore("`")
            } else {
                trimmed.substringAfter("(").substringBefore(")").trim('`', '\'', '"')
            }
            val schema = schemas[tableName] ?: emptyList()
            val rows = schema.map {
                arrayOf(it["cid"], it["name"], it["type"], it["notnull"], it["dflt_value"], it["pk"])
            }
            return SimpleTestCursor(arrayOf("cid", "name", "type", "notnull", "dflt_value", "pk"), rows)
        }

        if (trimmed.contains("COUNT(*)", ignoreCase = true)) {
            val tableName = if (trimmed.contains("FROM `", ignoreCase = true)) {
                trimmed.substringAfter("FROM `").substringBefore("`")
            } else {
                trimmed.substringAfter("FROM ", "").substringBefore(" ").substringBefore(";").trim('`', '\'', '"')
            }
            val count = tables[tableName]?.size?.toLong() ?: 0L
            return SimpleTestCursor(arrayOf("count"), listOf(arrayOf(count)))
        }

        if (trimmed.startsWith("SELECT", ignoreCase = true)) {
            val tableName = if (trimmed.contains("FROM `", ignoreCase = true)) {
                trimmed.substringAfter("FROM `").substringBefore("`")
            } else {
                trimmed.substringAfter("FROM ", "").substringBefore(" ").substringBefore(";").trim('`', '\'', '"')
            }
            val rows = tables[tableName] ?: emptyList()
            val schema = schemas[tableName] ?: emptyList()

            val selectPart = trimmed.substringAfter("SELECT", "").substringBefore("FROM", "").trim()
            val cols = if (selectPart == "*" || selectPart.isBlank()) {
                schema.map { it["name"] as String }.toTypedArray()
            } else {
                selectPart.split(",").map { it.trim().trim('`', '\'', '"') }.toTypedArray()
            }

            val searchQuery = if (trimmed.contains("LIKE '%")) trimmed.substringAfter("LIKE '%").substringBefore("%'") else null
            val matchedRows = rows.filter { row ->
                searchQuery == null || row.values.any { it?.toString()?.contains(searchQuery, ignoreCase = true) == true }
            }.map { row ->
                cols.map { row[it] }.toTypedArray()
            }
            return SimpleTestCursor(cols, matchedRows)
        }

        return SimpleTestCursor(emptyArray(), emptyList())
    }

    override fun execSQL(sql: String) {
        val trimmed = sql.trim()
        if (trimmed.startsWith("INSERT INTO products", ignoreCase = true)) {
            tables["products"]?.add(mutableMapOf("id" to "1", "title" to "Widget", "price" to "19.99"))
        }
    }

    override fun close() {}
    override val isOpen: Boolean get() = true
    override val isDbLockedByCurrentThread: Boolean get() = false
    override val isReadOnly: Boolean get() = false
    override val isWriteAheadLoggingEnabled: Boolean get() = true
    override val isDatabaseIntegrityOk: Boolean get() = true
    override var version: Int = 1
    override val maximumSize: Long get() = 0L
    override var pageSize: Long = 4096L
    override val path: String? get() = null
    override val attachedDbs: List<android.util.Pair<String, String>>? get() = null

    override fun compileStatement(sql: String): SupportSQLiteStatement = throw NotImplementedError()
    override fun beginTransaction() {}
    override fun beginTransactionNonExclusive() {}
    override fun beginTransactionReadOnly() {}
    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {}
    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {}
    override fun endTransaction() {}
    override fun setTransactionSuccessful() {}
    override fun inTransaction(): Boolean = false
    override fun yieldIfContendedSafely(): Boolean = false
    override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean = false
    override fun needUpgrade(newVersion: Int): Boolean = false
    override fun setMaximumSize(numBytes: Long): Long = 0
    override fun query(query: String, bindArgs: Array<out Any?>): Cursor = this.query(query)
    override fun query(query: SupportSQLiteQuery): Cursor = this.query(query.sql)
    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor = this.query(query.sql)
    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long = 1L
    override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int = 1
    override fun update(table: String, conflictAlgorithm: Int, values: ContentValues, whereClause: String?, whereArgs: Array<out Any?>?): Int = 1
    override fun execSQL(sql: String, bindArgs: Array<out Any?>) = execSQL(sql)
    override fun setLocale(locale: Locale) {}
    override fun setMaxSqlCacheSize(cacheSize: Int) {}
    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) {}
    override fun enableWriteAheadLogging(): Boolean = true
    override fun disableWriteAheadLogging() {}
}
