package com.foli.listadetarefas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Banco local (única forma de persistência da US-01). Schema v1 exportado em
 * `app/schemas/` (`exportSchema = true`, `room.schemaLocation` no `app/build.gradle.kts`).
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
