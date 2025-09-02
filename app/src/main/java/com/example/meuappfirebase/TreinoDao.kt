package com.apol.myapplication.data.model // Mantendo seu pacote original

import androidx.room.*

@Dao
interface TreinoDao {

    // --- Treinos ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreino(treino: TreinoEntity): Long

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM treinos WHERE userOwnerId = :userId")
    suspend fun getAllTreinos(userId: String): List<TreinoEntity>

    @Query("SELECT * FROM treinos WHERE id = :treinoId")
    suspend fun getTreinoById(treinoId: Long): TreinoEntity?

    @Update
    suspend fun updateTreino(treino: TreinoEntity)

    @Query("DELETE FROM treinos WHERE id IN (:treinoIds)")
    suspend fun deleteTreinosByIds(treinoIds: List<Long>)

    // --- Divisões ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDivisao(divisao: DivisaoTreino): Long

    @Query("SELECT * FROM divisoes_treino WHERE treinoId = :treinoId ORDER BY ordem ASC")
    suspend fun getDivisoesByTreinoId(treinoId: Long): List<DivisaoTreino>

    @Update
    suspend fun updateDivisao(divisao: DivisaoTreino)

    @Delete
    suspend fun deleteDivisoes(divisoes: List<DivisaoTreino>)

    // --- Notas de Treino ---
    @Insert
    suspend fun insertTreinoNota(nota: TreinoNota)

    @Update
    suspend fun updateTreinoNota(nota: TreinoNota)

    @Delete
    suspend fun deleteTreinoNota(nota: TreinoNota)

    @Delete
    suspend fun deleteTreinoNotas(notas: List<TreinoNota>)

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM treino_notas WHERE divisaoId = :divisaoId AND userOwnerId = :userId")
    suspend fun getNotasByDivisaoId(divisaoId: Long, userId: String): List<TreinoNota>
}