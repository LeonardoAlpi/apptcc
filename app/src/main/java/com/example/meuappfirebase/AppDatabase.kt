package com.apol.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apol.myapplication.data.model.*
import com.apol.myapplication.Converters // <- nosso conversor único

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
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class) // <- garante que Room use nosso conversor
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
                    .fallbackToDestructiveMigration() // recria o banco se a versão mudar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
