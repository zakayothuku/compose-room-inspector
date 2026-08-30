package io.github.zakayothuku.roominspector

import io.github.zakayothuku.roominspector.driver.SqliteDatabaseDriver
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SqliteDatabaseDriverTest {

    private lateinit var db: FakeSupportSQLiteDatabase
    private lateinit var driver: SqliteDatabaseDriver

    @Before
    fun setup() {
        db = FakeSupportSQLiteDatabase()
        driver = SqliteDatabaseDriver(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `test getTableNames returns user defined tables`() {
        val tables = driver.getTableNames()
        assertTrue(tables.contains("users"))
        assertTrue(tables.contains("products"))
        assertFalse(tables.contains("sqlite_master"))
    }

    @Test
    fun `test getTableSchema returns column definitions and primary keys`() {
        val schema = driver.getTableSchema("users")
        assertEquals("users", schema.tableName)
        assertEquals(4, schema.columns.size)

        val idCol = schema.columns.find { it.name == "id" }
        assertNotNull(idCol)
        assertTrue(idCol!!.isPrimaryKey)

        val userCol = schema.columns.find { it.name == "username" }
        assertNotNull(userCol)
        assertTrue(userCol!!.notNull)

        assertEquals(3L, schema.rowCount)
    }

    @Test
    fun `test getTablePage returns paginated rows`() {
        val page = driver.getTablePage("users", page = 0, pageSize = 2)
        assertEquals("users", page.tableName)
        assertEquals(3, page.rows.size)
        assertEquals(3L, page.totalRowCount)
    }

    @Test
    fun `test getTablePage filters rows with search query`() {
        val page = driver.getTablePage("users", page = 0, pageSize = 10, searchQuery = "alice")
        assertEquals(1, page.rows.size)
        assertEquals("alice", page.rows[0][1])
    }

    @Test
    fun `test executeSql runs SELECT and returns structured columns and rows`() {
        val result = driver.executeSql("SELECT * FROM users")
        println("DEBUG: isSuccess=${result.isSuccess}, error=${result.errorMessage}, columns=${result.columns}")
        assertTrue(result.isSuccess)
        assertEquals(listOf("id", "username", "email", "score"), result.columns)
        assertEquals(3, result.rows.size)
        assertEquals("alice", result.rows[0][1])
    }

    @Test
    fun `test executeSql runs DML statement and returns affected rows`() {
        val result = driver.executeSql("INSERT INTO products (id, title, price) VALUES (1, 'Widget', 19.99)")
        assertTrue(result.isSuccess)
        assertEquals(1L, driver.getRowCount("products"))
    }
}
