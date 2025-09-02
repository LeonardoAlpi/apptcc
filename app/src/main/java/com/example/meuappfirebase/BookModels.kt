package com.apol.myapplication

class BookModels {
    data class Book(val volumeInfo: VolumeInfo)
    data class VolumeInfo(val title: String, val authors: List<String>?)
    data class BookResponse(val items: List<Book>)
}