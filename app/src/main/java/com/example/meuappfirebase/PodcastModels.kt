package com.apol.myapplication

class PodcastModels {
    data class Podcast(val title_original: String, val publisher_original: String, val image: String)
    data class PodcastResponse(val results: List<Podcast>)
}