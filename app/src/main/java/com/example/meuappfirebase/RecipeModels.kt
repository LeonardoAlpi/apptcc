package com.apol.myapplication

class RecipeModels {
    data class Recipe(val id: Int, val title: String, val image: String)
    data class RecipeResponse(val results: List<Recipe>)
}