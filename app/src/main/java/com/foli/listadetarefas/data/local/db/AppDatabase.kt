package com.foli.listadetarefas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Banco local offline. v1 continha apenas `task`; a v2 (US-04) acrescenta
 * `label` com índice único de `name_key` e a FK `task.label_id` com
 * `ON DELETE SET NULL`, preservando dados existentes via [MIGRATION_1_2].
 * Schema exportado em `app/schemas/` (`exportSchema = true`).
 */
@Database(
    entities = [TaskEntity::class, LabelEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun labelDao(): LabelDao
}

/** Cria a tabela `label` e recria `task` para incluir a FK de etiqueta. */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `label` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`name_key` TEXT NOT NULL, " +
                "`color_key` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_label_name_key` ON `label` (`name_key`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `task_new` (" +
                "`id` TEXT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`label_id` TEXT, " +
                "`due_date` INTEGER, " +
                "`created_at` INTEGER NOT NULL, " +
                "`completed_at` INTEGER, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`label_id`) REFERENCES `label`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE SET NULL)",
        )
        db.execSQL(
            "INSERT INTO `task_new` (`id`, `title`, `description`, `label_id`, `due_date`, `created_at`, `completed_at`) " +
                "SELECT `id`, `title`, `description`, `label_id`, `due_date`, `created_at`, `completed_at` FROM `task`",
        )
        db.execSQL("DROP TABLE `task`")
        db.execSQL("ALTER TABLE `task_new` RENAME TO `task`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_label_id` ON `task` (`label_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_created_at` ON `task` (`created_at`)")
    }
}
