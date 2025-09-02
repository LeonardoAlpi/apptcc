package com.apol.myapplication

class WgerModels {
    data class WgerExercise(val name: String, val description: String)
    data class WgerExerciseResponse(val results: List<WgerExercise>)
}