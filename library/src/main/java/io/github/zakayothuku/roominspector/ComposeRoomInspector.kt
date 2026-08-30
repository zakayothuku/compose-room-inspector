package io.github.zakayothuku.roominspector

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.zakayothuku.roominspector.repository.RoomInspectorRepository

/**
 * Top-level entrypoint to register Room and SQLite databases for on-device inspection.
 */
object ComposeRoomInspector {

    /**
     * Registers a SupportSQLiteDatabase instance under a given name.
     */
    fun registerDatabase(name: String, database: SupportSQLiteDatabase) {
        RoomInspectorRepository.registerDatabase(name, database)
    }

    /**
     * Registers an AndroidX RoomDatabase instance.
     */
    fun registerRoomDatabase(name: String, roomDatabase: RoomDatabase) {
        val sqliteDb = roomDatabase.openHelper.writableDatabase
        RoomInspectorRepository.registerDatabase(name, sqliteDb)
    }
}
