package fr.pcm.projet.projet_pcm.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.pcm.projet.projet_pcm.Converters

@Database(entities = [Theme::class, JeuDeQuestions::class, Question::class, Statistique::class], version = 11, exportSchema = false)
@TypeConverters(Converters::class)
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