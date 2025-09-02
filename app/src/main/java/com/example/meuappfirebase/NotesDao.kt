package com.apol.myapplication.data.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    // --- Funções para Notes ---
    @Insert
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNotes(notes: List<Note>)

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM notes WHERE userOwnerId = :userId ORDER BY lastModified DESC")
    fun getNotesByUser(userId: String): Flow<List<Note>>

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM notes WHERE userOwnerId = :userId ORDER BY lastModified DESC")
    suspend fun getNotesByUserNow(userId: String): List<Note>


    // --- Funções para Blocos ---
    @Insert
    suspend fun insertBloco(bloco: Bloco)

    @Update
    suspend fun updateBloco(bloco: Bloco)

    @Delete
    suspend fun deleteBlocos(blocos: List<Bloco>)

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM blocos WHERE userOwnerId = :userId")
    fun getBlocosByUser(userId: String): Flow<List<Bloco>>

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM blocos WHERE userOwnerId = :userId")
    suspend fun getBlocosByUserNow(userId: String): List<Bloco>
}