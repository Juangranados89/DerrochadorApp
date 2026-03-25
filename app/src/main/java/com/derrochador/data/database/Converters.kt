package com.derrochador.data.database

import androidx.room.TypeConverter
import com.derrochador.data.model.TransactionSource

class Converters {
    @TypeConverter
    fun fromTransactionSource(source: TransactionSource): String = source.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource =
        TransactionSource.valueOf(value)
}
