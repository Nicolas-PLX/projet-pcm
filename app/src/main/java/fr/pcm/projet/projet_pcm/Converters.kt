package fr.pcm.projet.projet_pcm


import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}