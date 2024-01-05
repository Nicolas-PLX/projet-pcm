package fr.pcm.projet.projet_pcm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.ABORT
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


@Dao
interface DaoDB{
    @Insert(onConflict = IGNORE)
    suspend fun insertTheme(theme:Theme)

    @Insert(onConflict = IGNORE)
    suspend fun insertThemesList(themes:List<Theme>)

    @Insert(onConflict = IGNORE)
    suspend fun insertJeuDeQuestions(jeuDeQuestions:JeuDeQuestions)

    @Insert(onConflict = IGNORE)
    suspend fun insertJeuxDeQuestionsList(jeuxDeQuestions:List<JeuDeQuestions>)

    @Insert(onConflict = IGNORE)
    suspend fun insertQuestion(question:Question)

    @Insert(onConflict = IGNORE)
    suspend fun insertQuestionsList(questions:List<Question>)

    @Query("SELECT * FROM Theme")
    fun loadAllTheme(): Flow<List<Theme>>

    //La liste des noms des thèmes
    @Query("SELECT DISTINCT nom FROM Theme")
    fun loadThemeName():List<String>

    //La liste des noms des jeux de questions pour un thème donné
    @Query("SELECT nom FROM JeuDeQuestions WHERE nomTheme=:n")
    fun loadJDQName(n:String):Flow<List<String>>

    //Selectionne l'id du jeu de questions de nom n.
    @Query("SELECT id FROM JeuDeQuestions WHERE nom=:n")
    fun loadIdJDQ(n:String):Flow<Int>
    //Selectionne toutes les questions d'un jeu de question de nom n.
    @Query("SELECT question FROM Question JOIN JeuDeQuestions ON Question.idJeuDeQuestions = JeuDeQuestions.id WHERE nom=:n")
    fun loadQuestionsFromJDQ(n:String):Flow<List<String>>

    //La liste des questions, réponses et statuts de d'un jeu de questions
    @Query("SELECT * FROM Question WHERE idJeuDeQuestions=:idJDK")
    fun loadAllQuestion(idJDK:Int): Flow<List<Question>>

    //Selectionne toute les questions issu d'une liste de questions (en format chaîne de caractère) pour les mettre sous format Liste de question.
    @Query("SELECT * FROM Question WHERE question IN (:questions)")
    suspend fun getQuestionsByContent(questions : List<String>): List<Question>


    @Query ("SELECT * FROM JeuDeQuestions WHERE nom=:n")
    suspend fun getJDQbyName(n : String) : JeuDeQuestions

    // Requête qui sélectionne un certains nombres de questions issu d'un jeu de question. On choisit les
    // questions avec comme statut 1
    @Query("SELECT Question.* FROM Question JOIN JeuDeQuestions ON Question.idJeuDeQuestions = JeuDeQuestions.id WHERE nom =:nameJDQ AND statut = 1 ORDER BY RANDOM() LIMIT :nbr")
    fun getNbrQuestionsFromJDQ(nameJDQ : String, nbr : Int) : Flow<List<Question>>


    //Supprime les questions de la base de donnée
    @Delete
    suspend fun deleteQuestions(questions: List<Question>)

    //Supprime le JDQ de la base de donnée
    @Delete
    suspend fun deleteJDQ(jdq : JeuDeQuestions)

    //Selectionne la question de question n
    @Query ("SELECT * FROM Question WHERE id=:n")
    suspend fun getQuestion(n : Int) : Question

    @Update
    suspend fun updateQuestion(question : Question)

    // Reduis de 1 le statut tout les jours
    @Query("UPDATE Question SET statut = CASE WHEN statut > 1 THEN statut - 1 ELSE 1 END")
    suspend fun updateQuestionStatus()
}