package com.apol.myapplication.data.converters

import androidx.room.TypeConverter

class ListConverters {
    @TypeConverter
    fun fromString(value: String?): List<String>? = value?.split(",")?.map { it.trim() }

    @TypeConverter
    fun listToString(list: List<String>?): String? = list?.joinToString(",")
}
