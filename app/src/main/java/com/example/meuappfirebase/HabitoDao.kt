package com.apol.myapplication.data.model

import androidx.room.*

@Dao
interface HabitoDao {

    // --- Operações para Hábitos ---

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM habitos WHERE userOwnerId = :userId")
    suspend fun getHabitosByUser(userId: String): List<Habito>

    @Insert
    suspend fun insertHabito(habito: Habito)

    @Update
    suspend fun updateHabito(habito: Habito)

    @Insert
    suspend fun insertHabitoComRetornoDeId(habito: Habito): Long

    @Query("DELETE FROM habitos WHERE id IN (:ids)")
    suspend fun deleteHabitosByIds(ids: List<Long>)

    // **CONSULTA CORRIGIDA AQUI**
    @Query("SELECT * FROM habitos WHERE userOwnerId = :userId AND isFavorito = 1 LIMIT 3")
    suspend fun getFavoritedHabitsByUser(userId: String): List<Habito>

    @Query("SELECT * FROM habitos WHERE id = :habitoId")
    suspend fun getHabitoById(habitoId: Long): Habito?

    // --- Operações para Progresso dos Hábitos ---

    @Query("SELECT * FROM habito_progresso WHERE habitoId = :habitoId")
    suspend fun getProgressoForHabito(habitoId: Long): List<HabitoProgresso>

    @Insert
    suspend fun insertProgresso(progresso: HabitoProgresso)

    @Query("DELETE FROM habito_progresso WHERE habitoId = :habitoId AND data = :data")
    suspend fun deleteProgresso(habitoId: Long, data: String)


    // --- Operações para Agendamentos de Hábitos ---

    @Insert
    suspend fun insertAgendamento(agendamento: HabitoAgendamento)

    @Query("SELECT * FROM habito_agendamentos WHERE habitoId = :habitoId ORDER BY dataDeInicio DESC")
    suspend fun getAgendamentosParaHabito(habitoId: Long): List<HabitoAgendamento>
}