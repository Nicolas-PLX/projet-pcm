package fr.pcm.projet.projet_pcm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Japonais::class], version = 1)
abstract class BD : RoomDatabase() {
    abstract fun japonaisDao(): JaponaisDao
}