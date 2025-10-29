package com.example.meuappfirebase

data class HabitUI(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    val isFavorited: Boolean,
    val isGoodHabit: Boolean,
    val userOwnerId: String = "", // <-- Valor padrão
    val daysOfWeek: Set<String> = emptySet(), // ✅ Adicionado campo que guarda os dias do hábito
    var isSelected: Boolean = false
)
