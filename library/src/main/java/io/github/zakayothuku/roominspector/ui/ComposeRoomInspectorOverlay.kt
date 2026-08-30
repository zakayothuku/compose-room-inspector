package io.github.zakayothuku.roominspector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.roominspector.driver.QueryResult
import io.github.zakayothuku.roominspector.driver.TablePageResult
import io.github.zakayothuku.roominspector.model.ColumnInfo
import io.github.zakayothuku.roominspector.repository.RoomInspectorRepository
import io.github.zakayothuku.roominspector.repository.RoomInspectorState

/**
 * Stateful Container collecting RoomInspectorRepository state.
 */
@Composable
fun ComposeRoomInspectorOverlay(
    modifier: Modifier = Modifier
) {
    val state by RoomInspectorRepository.state.collectAsState()

    ComposeRoomInspectorOverlayContent(
        state = state,
        onSelectDatabase = { RoomInspectorRepository.selectDatabase(it) },
        onSelectTable = { RoomInspectorRepository.selectTable(it) },
        onPageChange = { RoomInspectorRepository.setPage(it) },
        onSearchQueryChange = { RoomInspectorRepository.setSearchQuery(it) },
        onRefreshTable = { RoomInspectorRepository.refreshCurrentTable() },
        onExecuteSql = { RoomInspectorRepository.executeSql(it) },
        onExportCsv = { RoomInspectorRepository.exportCurrentTableAsCsv() },
        onExportJson = { RoomInspectorRepository.exportCurrentTableAsJson() },
        modifier = modifier
    )
}

/**
 * Stateless Content Composable adhering to Safaricom Compose Previews & Clean Architecture standards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeRoomInspectorOverlayContent(
    state: RoomInspectorState,
    onSelectDatabase: (String) -> Unit,
    onSelectTable: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefreshTable: () -> Unit,
    onExecuteSql: (String) -> Unit,
    onExportCsv: () -> String,
    onExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Table Browser, 1 = SQL Console

    val tableCount = state.tableNames.size

    Box(modifier = modifier.fillMaxSize()) {
        // Floating Database Inspector Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { isExpanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🗄️ Room DB",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "$tableCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Full Screen Database Inspector Bottom Sheet
        if (isExpanded) {
            ModalBottomSheet(
                onDismissRequest = { isExpanded = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Room Database Inspector",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DB: ${state.selectedDatabaseName ?: "None"} (${state.tableNames.size} Tables)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(onClick = { isExpanded = false }) {
                            Text("Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Database Selector Chips (if multiple databases registered)
                    if (state.databases.size > 1) {
                        Text("Databases:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.databases.keys.forEach { dbName ->
                                FilterChip(
                                    selected = state.selectedDatabaseName == dbName,
                                    onClick = { onSelectDatabase(dbName) },
                                    label = { Text(dbName, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Table Selector Chips
                    if (state.tableNames.isNotEmpty()) {
                        Text("Tables:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.tableNames.forEach { tableName ->
                                FilterChip(
                                    selected = state.selectedTableName == tableName,
                                    onClick = { onSelectTable(tableName) },
                                    label = { Text(tableName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Navigation Tabs
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Table Browser", fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("SQL Console", fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Views
                    if (selectedTab == 0) {
                        TableBrowserView(
                            pageResult = state.currentPageResult,
                            searchQuery = state.searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            onPageChange = onPageChange,
                            onRefresh = onRefreshTable,
                            onExportCsv = onExportCsv,
                            onExportJson = onExportJson
                        )
                    } else {
                        SqlConsoleView(
                            activeTableName = state.selectedTableName,
                            sqlResult = state.activeSqlResult,
                            sqlHistory = state.sqlHistory,
                            onExecuteSql = onExecuteSql
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS (Following sfc-android-compose-previews standards)
// ============================================================================

@PreviewLightDark
@Composable
private fun ComposeRoomInspectorOverlay_Populated_Preview() {
    MaterialTheme {
        Surface {
            ComposeRoomInspectorOverlayContent(
                state = RoomInspectorState(
                    selectedDatabaseName = "AppDatabase",
                    tableNames = listOf("users", "products", "orders"),
                    selectedTableName = "users",
                    currentPageResult = TablePageResult(
                        tableName = "users",
                        columns = listOf(
                            ColumnInfo(0, "id", "INTEGER", isPrimaryKey = true),
                            ColumnInfo(1, "username", "TEXT"),
                            ColumnInfo(2, "email", "TEXT"),
                            ColumnInfo(3, "balance", "REAL")
                        ),
                        rows = listOf(
                            listOf("1", "alice", "alice@example.com", "250.50"),
                            listOf("2", "bob", "bob@example.com", "120.00")
                        ),
                        totalRowCount = 2,
                        page = 0,
                        pageSize = 25
                    )
                ),
                onSelectDatabase = {},
                onSelectTable = {},
                onPageChange = {},
                onSearchQueryChange = {},
                onRefreshTable = {},
                onExecuteSql = {},
                onExportCsv = { "" },
                onExportJson = { "" }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ComposeRoomInspectorOverlay_Empty_Preview() {
    MaterialTheme {
        Surface {
            ComposeRoomInspectorOverlayContent(
                state = RoomInspectorState(),
                onSelectDatabase = {},
                onSelectTable = {},
                onPageChange = {},
                onSearchQueryChange = {},
                onRefreshTable = {},
                onExecuteSql = {},
                onExportCsv = { "" },
                onExportJson = { "" }
            )
        }
    }
}
