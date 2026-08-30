package io.github.zakayothuku.roominspector.repository

import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.zakayothuku.roominspector.driver.QueryResult
import io.github.zakayothuku.roominspector.driver.SqliteDatabaseDriver
import io.github.zakayothuku.roominspector.driver.TablePageResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RoomInspectorState(
    val databases: Map<String, SqliteDatabaseDriver> = emptyMap(),
    val selectedDatabaseName: String? = null,
    val tableNames: List<String> = emptyList(),
    val selectedTableName: String? = null,
    val currentPageResult: TablePageResult? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 25,
    val searchQuery: String = "",
    val activeSqlResult: QueryResult? = null,
    val sqlHistory: List<String> = emptyList(),
    val isBusy: Boolean = false
)

object RoomInspectorRepository {

    private val _state = MutableStateFlow(RoomInspectorState())
    val state: StateFlow<RoomInspectorState> = _state.asStateFlow()

    fun registerDatabase(name: String, db: SupportSQLiteDatabase) {
        val driver = SqliteDatabaseDriver(db)
        _state.update { current ->
            val updatedDbs = current.databases + (name to driver)
            val selectedName = current.selectedDatabaseName ?: name
            val tables = driver.getTableNames()
            val selectedTable = current.selectedTableName ?: tables.firstOrNull()

            val pageResult = if (selectedTable != null) {
                driver.getTablePage(tableName = selectedTable, page = 0, pageSize = current.pageSize)
            } else null

            current.copy(
                databases = updatedDbs,
                selectedDatabaseName = selectedName,
                tableNames = tables,
                selectedTableName = selectedTable,
                currentPageResult = pageResult
            )
        }
    }

    fun selectDatabase(name: String) {
        val driver = _state.value.databases[name] ?: return
        val tables = driver.getTableNames()
        val firstTable = tables.firstOrNull()
        val pageResult = if (firstTable != null) {
            driver.getTablePage(tableName = firstTable, page = 0, pageSize = _state.value.pageSize)
        } else null

        _state.update {
            it.copy(
                selectedDatabaseName = name,
                tableNames = tables,
                selectedTableName = firstTable,
                currentPage = 0,
                searchQuery = "",
                currentPageResult = pageResult
            )
        }
    }

    fun selectTable(tableName: String) {
        val driver = getActiveDriver() ?: return
        val pageResult = driver.getTablePage(
            tableName = tableName,
            page = 0,
            pageSize = _state.value.pageSize,
            searchQuery = _state.value.searchQuery.ifBlank { null }
        )

        _state.update {
            it.copy(
                selectedTableName = tableName,
                currentPage = 0,
                currentPageResult = pageResult
            )
        }
    }

    fun setPage(page: Int) {
        val driver = getActiveDriver() ?: return
        val table = _state.value.selectedTableName ?: return
        val pageResult = driver.getTablePage(
            tableName = table,
            page = maxOf(0, page),
            pageSize = _state.value.pageSize,
            searchQuery = _state.value.searchQuery.ifBlank { null }
        )

        _state.update {
            it.copy(
                currentPage = maxOf(0, page),
                currentPageResult = pageResult
            )
        }
    }

    fun setSearchQuery(query: String) {
        val driver = getActiveDriver() ?: return
        val table = _state.value.selectedTableName ?: return
        val pageResult = driver.getTablePage(
            tableName = table,
            page = 0,
            pageSize = _state.value.pageSize,
            searchQuery = query.ifBlank { null }
        )

        _state.update {
            it.copy(
                searchQuery = query,
                currentPage = 0,
                currentPageResult = pageResult
            )
        }
    }

    fun refreshCurrentTable() {
        val table = _state.value.selectedTableName ?: return
        setPage(_state.value.currentPage)
    }

    fun executeSql(sql: String) {
        val driver = getActiveDriver() ?: return
        val result = driver.executeSql(sql)

        _state.update {
            it.copy(
                activeSqlResult = result,
                sqlHistory = (listOf(sql) + it.sqlHistory.filterNot { h -> h == sql }).take(20)
            )
        }

        // Auto refresh table if DML query executed
        if (!sql.trim().startsWith("SELECT", ignoreCase = true)) {
            refreshCurrentTable()
        }
    }

    fun exportCurrentTableAsCsv(): String {
        val result = _state.value.currentPageResult ?: return ""
        val sb = StringBuilder()
        sb.append(result.columns.joinToString(",") { "\"${it.name}\"" }).append("\n")
        result.rows.forEach { row ->
            sb.append(row.joinToString(",") { "\"${it?.replace("\"", "\"\"") ?: "NULL"}\"" }).append("\n")
        }
        return sb.toString()
    }

    fun exportCurrentTableAsJson(): String {
        val result = _state.value.currentPageResult ?: return "[]"
        val sb = StringBuilder("[\n")
        result.rows.forEachIndexed { rowIndex, row ->
            sb.append("  {")
            result.columns.forEachIndexed { colIndex, col ->
                val value = row.getOrNull(colIndex)
                val formattedVal = if (value == null) "null" else "\"${value.replace("\"", "\\\"")}\""
                sb.append("\"${col.name}\": $formattedVal")
                if (colIndex < result.columns.size - 1) sb.append(", ")
            }
            sb.append("}")
            if (rowIndex < result.rows.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun getActiveDriver(): SqliteDatabaseDriver? {
        val selectedName = _state.value.selectedDatabaseName ?: return null
        return _state.value.databases[selectedName]
    }

    fun clearAll() {
        _state.value = RoomInspectorState()
    }
}
