package com.apol.myapplication.data.model

import androidx.room.*

@Dao
interface UserDao {

    // --- Operações para a Tabela 'users' ---

    @Insert
    suspend fun insertUser(user: User)

    // Busca o usuário pelo ID único do Firebase (UID)
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User> // Para a tela de admin

    // Busca o usuário atualmente salvo no Room
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    // --- Operações para a Tabela 'weight_history' ---

    @Insert
    suspend fun insertWeightEntry(entry: WeightEntry)

    @Query("SELECT * FROM weight_history WHERE userOwnerId = :userId ORDER BY date ASC")
    suspend fun getWeightHistory(userId: String): List<WeightEntry>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: User)
}
