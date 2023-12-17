package fr.pcm.projet.projet_pcm.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Theme::class, JeuDeQuestions::class, Question::class], version = 2, exportSchema = false)
abstract class BD : RoomDatabase() {
    abstract fun dao(): DaoDB
    companion object{
        @Volatile
        private var instance: BD? = null

        fun getDataBase(c: Context): BD{
            if(instance!=null) return instance!!
            val db = Room.databaseBuilder(c.applicationContext, BD::class.java, "themes et questions").fallbackToDestructiveMigration().build()
            instance = db
            return instance!!
        }

    }
}