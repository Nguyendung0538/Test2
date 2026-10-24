package com.example.test2.data.local

import androidx.room.TypeConverter

/** The schema stores statuses as strings, so no enum converter is required yet.
 * This converter is provided for java.time.LocalDate if the app later stores it directly.
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(value: java.time.LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): java.time.LocalDate? =
        value?.let { java.time.LocalDate.parse(it) }
}
