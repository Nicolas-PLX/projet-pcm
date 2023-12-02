package fr.pcm.projet.projet_pcm.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao{
    @Query("SELECT * FROM Japonais")
    fun loadAllJp(): Flow<List<Japonais>>

    @Query("SELECT * FROM Theme")
    fun loadAllTheme(): Flow<List<Theme>>

    @Query("SELECT DISTINCT nom FROM Theme")
    fun loadThemeName():List<String>
}