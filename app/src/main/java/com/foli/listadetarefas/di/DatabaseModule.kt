package com.foli.listadetarefas.di

import android.content.Context
import androidx.room.Room
import com.foli.listadetarefas.BuildConfig
import com.foli.listadetarefas.core.di.IoDispatcher
import com.foli.listadetarefas.data.local.db.AppDatabase
import com.foli.listadetarefas.data.local.db.LabelDao
import com.foli.listadetarefas.data.local.db.MIGRATION_1_2
import com.foli.listadetarefas.data.local.db.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import javax.inject.Singleton

/**
 * Fornece o banco Room (singleton) e o DAO. Migração destrutiva só em `debug`,
 * conforme ARCHITECTURE.md §9.1; em release a migração é explícita.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "lista_de_tarefas.db"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AppDatabase {
        val executor = ioDispatcher.asExecutor()
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .setQueryExecutor(executor)
            .setTransactionExecutor(executor)
            .addMigrations(MIGRATION_1_2)
            .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration(dropAllTables = true) }
            .build()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideLabelDao(database: AppDatabase): LabelDao = database.labelDao()
}
