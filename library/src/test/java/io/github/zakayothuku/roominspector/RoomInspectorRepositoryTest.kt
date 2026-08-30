package io.github.zakayothuku.roominspector

import io.github.zakayothuku.roominspector.repository.RoomInspectorRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RoomInspectorRepositoryTest {

    private lateinit var db: FakeSupportSQLiteDatabase

    @Before
    fun setup() {
        RoomInspectorRepository.clearAll()
        db = FakeSupportSQLiteDatabase()
        RoomInspectorRepository.registerDatabase("TestDb", db)
    }

    @After
    fun teardown() {
        db.close()
        RoomInspectorRepository.clearAll()
    }

    @Test
    fun `test registerDatabase initializes state correctly`() {
        val state = RoomInspectorRepository.state.value
        assertEquals("TestDb", state.selectedDatabaseName)
        assertTrue(state.tableNames.contains("users"))
        assertEquals("users", state.selectedTableName)
        assertNotNull(state.currentPageResult)
    }

    @Test
    fun `test exportCurrentTableAsCsv produces valid CSV string`() {
        val csv = RoomInspectorRepository.exportCurrentTableAsCsv()
        assertTrue(csv.contains("\"id\",\"username\",\"email\",\"score\""))
        assertTrue(csv.contains("\"1\",\"alice\",\"alice@test.com\",\"95.5\""))
    }

    @Test
    fun `test exportCurrentTableAsJson produces valid JSON string`() {
        val json = RoomInspectorRepository.exportCurrentTableAsJson()
        assertTrue(json.contains("\"username\": \"alice\""))
        assertTrue(json.contains("\"email\": \"alice@test.com\""))
    }
}
