
package com.apol.myapplication

// Esta é a "planta" de como um hábito é exibido na tela.
data class Habit(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    var isSelected: Boolean = false,
    var isFavorited: Boolean = false
)