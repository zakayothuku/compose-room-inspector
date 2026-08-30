package io.github.zakayothuku.roominspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.roominspector.driver.TablePageResult

@Composable
fun TableBrowserView(
    pageResult: TablePageResult?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onRefresh: () -> Unit,
    onExportCsv: () -> String,
    onExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var exportStatusText by remember { mutableStateOf<String?>(null) }
    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search rows...", fontSize = 12.sp) },
                placeholder = { Text("Filter content", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(onClick = onRefresh) {
                Text("🔄", fontSize = 16.sp)
            }

            TextButton(
                onClick = {
                    val csv = onExportCsv()
                    clipboardManager.setText(AnnotatedString(csv))
                    exportStatusText = "CSV copied to clipboard!"
                }
            ) {
                Text("CSV", fontSize = 12.sp)
            }

            TextButton(
                onClick = {
                    val json = onExportJson()
                    clipboardManager.setText(AnnotatedString(json))
                    exportStatusText = "JSON copied to clipboard!"
                }
            ) {
                Text("JSON", fontSize = 12.sp)
            }
        }

        if (exportStatusText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exportStatusText!!,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (pageResult == null || pageResult.columns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No table selected or table is empty.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            // Table Row Count Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Records: ${pageResult.totalRowCount} | Page ${pageResult.page + 1} of ${pageResult.totalPages}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2D Scrollable Table Data Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column {
                    // Sticky Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier.width(40.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        pageResult.columns.forEach { col ->
                            Row(
                                modifier = Modifier.width(130.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = col.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (col.isPrimaryKey) {
                                    Text("🔑", fontSize = 10.sp)
                                }
                                Text(
                                    text = "(${col.type})",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Data Rows
                    if (pageResult.rows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No records match query.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            itemsIndexed(pageResult.rows) { index, row ->
                                val rowNum = (pageResult.page * pageResult.pageSize) + index + 1
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$rowNum",
                                        modifier = Modifier.width(40.dp),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    row.forEach { cellValue ->
                                        Text(
                                            text = cellValue ?: "NULL",
                                            modifier = Modifier.width(130.dp),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (cellValue == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pagination Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onPageChange(pageResult.page - 1) },
                    enabled = pageResult.page > 0,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("◀ Prev", fontSize = 12.sp)
                }

                Text(
                    text = "Page ${pageResult.page + 1} / ${pageResult.totalPages}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = { onPageChange(pageResult.page + 1) },
                    enabled = pageResult.page < pageResult.totalPages - 1,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Next ▶", fontSize = 12.sp)
                }
            }
        }
    }
}
