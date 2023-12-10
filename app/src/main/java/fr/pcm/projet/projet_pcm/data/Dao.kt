package fr.pcm.projet.projet_pcm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.ABORT
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao{
    @Insert(onConflict = ABORT)
    suspend fun insertTheme(theme:Theme)

    @Insert(onConflict = ABORT)
    suspend fun insertJeuDeQuestions(jeuDeQuestions:JeuDeQuestions)

    @Insert(onConflict = ABORT)
    suspend fun insertQuestion(question:Question)

    @Query("SELECT * FROM Theme")
    fun loadAllTheme(): Flow<List<Theme>>

    //La liste des noms des thèmes
    @Query("SELECT DISTINCT nom FROM Theme")
    fun loadThemeName():List<String>

    //La liste des noms des jeux de questions pour un thème donné
    @Query("SELECT nom FROM JeuDeQuestions WHERE nomTheme=:n")
    fun loadJDQName(n:String):List<String>

    //La liste des questions, réponses et statuts de d'un jeu de questions
    @Query("SELECT question, reponse, statut FROM Question WHERE idJeuDeQuestions=:idJDK")
    fun loadAllQuestion(idJDK:Int): Flow<List<Question>>
}