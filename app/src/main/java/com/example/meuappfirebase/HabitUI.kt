package com.example.meuappfirebase

data class HabitUI(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    val isFavorited: Boolean,
    val isGoodHabit: Boolean, // Adicionado para facilitar o filtro
    var isSelected: Boolean = false
)