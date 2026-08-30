package io.github.zakayothuku.roominspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.roominspector.driver.QueryResult

@Composable
fun SqlConsoleView(
    activeTableName: String?,
    sqlResult: QueryResult?,
    sqlHistory: List<String>,
    onExecuteSql: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sqlQuery by remember {
        mutableStateOf(
            if (activeTableName != null) "SELECT * FROM `$activeTableName` LIMIT 20;"
            else "SELECT * FROM sqlite_master;"
        )
    }

    val horizontalScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Quick Snippets
        if (activeTableName != null) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { sqlQuery = "SELECT * FROM `$activeTableName` LIMIT 20;" },
                        label = { Text("SELECT *", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { sqlQuery = "SELECT COUNT(*) FROM `$activeTableName`;" },
                        label = { Text("COUNT(*)", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { sqlQuery = "PRAGMA table_info(`$activeTableName`);" },
                        label = { Text("PRAGMA info", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { sqlQuery = "DELETE FROM `$activeTableName` WHERE id = 1;" },
                        label = { Text("DELETE WHERE", fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // SQL Input Box
        OutlinedTextField(
            value = sqlQuery,
            onValueChange = { sqlQuery = it },
            label = { Text("SQL Query") },
            placeholder = { Text("SELECT * FROM table...") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Execute Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sqlResult != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (sqlResult.isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = if (sqlResult.isSuccess) "⚡ ${sqlResult.executionTimeMs} ms" else "FAILED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (sqlResult.isSuccess) {
                        Text(
                            text = "${sqlResult.rows.size} rows returned",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = { onExecuteSql(sqlQuery) },
                enabled = sqlQuery.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("▶ Run SQL", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results View
        if (sqlResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Enter a SQL statement and tap 'Run SQL'.", color = Color.Gray, fontSize = 13.sp)
            }
        } else if (!sqlResult.isSuccess) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SQL Execution Failed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sqlResult.errorMessage ?: "Unknown error",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else if (sqlResult.columns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Query executed successfully. ${sqlResult.affectedRows} row(s) affected.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        } else {
            // Display Result Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier.width(36.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        sqlResult.columns.forEach { colName ->
                            Text(
                                text = colName,
                                modifier = Modifier.width(130.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        itemsIndexed(sqlResult.rows) { index, row ->
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
                                    text = "${index + 1}",
                                    modifier = Modifier.width(36.dp),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )

                                row.forEach { cell ->
                                    Text(
                                        text = cell ?: "NULL",
                                        modifier = Modifier.width(130.dp),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (cell == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
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
    }
}
