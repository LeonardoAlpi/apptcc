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

    @Query("SELECT * FROM notes WHERE userOwnerId = :userId ORDER BY lastModified DESC")
    fun getNotesByUser(userId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE userOwnerId = :userId ORDER BY lastModified DESC")
    suspend fun getNotesByUserNow(userId: String): List<Note>


    // --- Funções para Blocos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloco(bloco: Bloco)

    @Update
    suspend fun updateBloco(bloco: Bloco)

    @Delete
    suspend fun deleteBlocos(blocos: List<Bloco>)

    @Query("SELECT * FROM blocos WHERE userOwnerId = :userId")
    fun getBlocosByUser(userId: String): Flow<List<Bloco>>

    @Query("SELECT * FROM blocos WHERE userOwnerId = :userId")
    suspend fun getBlocosByUserNow(userId: String): List<Bloco>

    // --- NOVAS FUNÇÕES PARA SINCRONIZAÇÃO ---

    /**
     * Apaga todos os blocos de um usuário e insere a nova lista do Firestore.
     * Isso garante que o Room seja um espelho fiel do Firestore.
     */
    @Transaction
    suspend fun syncBlocos(userId: String, blocosDoFirestore: List<Bloco>) {
        deleteBlocosByUser(userId)
        insertAllBlocos(blocosDoFirestore)
    }

    @Query("DELETE FROM blocos WHERE userOwnerId = :userId")
    suspend fun deleteBlocosByUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBlocos(blocos: List<Bloco>)

    @Query("SELECT * FROM blocos WHERE id = :blocoId LIMIT 1")
    suspend fun getBlocoById(blocoId: String): Bloco?
}