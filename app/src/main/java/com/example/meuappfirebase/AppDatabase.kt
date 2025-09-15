package com.apol.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apol.myapplication.data.model.*
import com.apol.myapplication.Converters

@Database(
    entities = [
        User::class,
        TreinoEntity::class,
        DivisaoTreino::class,
        TreinoNota::class,
        WeightEntry::class,
        Habito::class,
        HabitoProgresso::class,
        Note::class,
        Bloco::class,
        HabitoAgendamento::class
    ],
    version = 17,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun treinoDao(): TreinoDao
    abstract fun habitoDao(): HabitoDao
    abstract fun notesDao(): NotesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Isso já estava correto!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}