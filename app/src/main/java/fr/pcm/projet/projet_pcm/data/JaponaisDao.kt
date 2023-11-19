package fr.pcm.projet.projet_pcm.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JaponaisDao{
    @Query("SELECT * FROM Japonais")
    fun loadAll(): Flow<List<Japonais>>
}