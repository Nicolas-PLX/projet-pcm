package fr.pcm.projet.projet_pcm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Japonais::class], version = 1)
abstract class BD : RoomDatabase() {
    abstract fun japonaisDao(): JaponaisDao
    companion object{
        @Volatile
        private var instance: BD? = null

        fun getDataBase(c: Context): BD{
            if(instance!=null) return instance!!
            val db = Room.databaseBuilder(c.applicationContext, BD::class.java, "japonais").fallbackToDestructiveMigration().build()
            instance = db
            return instance!!
        }

    }
}