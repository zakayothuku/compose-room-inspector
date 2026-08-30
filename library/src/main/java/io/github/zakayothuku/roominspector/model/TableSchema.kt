package io.github.zakayothuku.roominspector.model

/**
 * Metadata representing a single database table column.
 */
data class ColumnInfo(
    val cid: Int,
    val name: String,
    val type: String,
    val notNull: Boolean = false,
    val defaultValue: String? = null,
    val isPrimaryKey: Boolean = false
)

/**
 * Metadata representing the complete schema of a database table.
 */
data class TableSchema(
    val tableName: String,
    val columns: List<ColumnInfo>,
    val rowCount: Long = 0
)
