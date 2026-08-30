package io.github.zakayothuku.roominspector.driver

import io.github.zakayothuku.roominspector.model.ColumnInfo

/**
 * Result of an arbitrary SQL query execution.
 */
data class QueryResult(
    val isSuccess: Boolean,
    val sql: String,
    val columns: List<String> = emptyList(),
    val rows: List<List<String?>> = emptyList(),
    val affectedRows: Int = 0,
    val executionTimeMs: Long = 0,
    val errorMessage: String? = null
)

/**
 * Result of a paginated table browse query.
 */
data class TablePageResult(
    val tableName: String,
    val columns: List<ColumnInfo> = emptyList(),
    val rows: List<List<String?>> = emptyList(),
    val totalRowCount: Long = 0,
    val page: Int = 0,
    val pageSize: Int = 20
) {
    val totalPages: Int
        get() = if (totalRowCount == 0L) 1 else ((totalRowCount + pageSize - 1) / pageSize).toInt()
}
