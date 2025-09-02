package com.apol.myapplication

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromIntList(value: String?): List<Int> {
        return value?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }
}