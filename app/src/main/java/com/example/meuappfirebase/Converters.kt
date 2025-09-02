package com.apol.myapplication

import androidx.room.TypeConverter

class Converters {

    // --- CONVERSORES PARA LISTA DE STRINGS ---

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        // Usa a versão mais segura que remove espaços e itens vazios
        return value?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    // --- CONVERSORES PARA LISTA DE INTEIROS ---

    @TypeConverter
    fun fromIntList(value: String?): List<Int> {
        return value?.split(',')?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }
}