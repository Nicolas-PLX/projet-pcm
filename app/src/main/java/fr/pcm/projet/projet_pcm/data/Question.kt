package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


//Chaque question est associé à un jeu de questions
@Entity(foreignKeys = [ ForeignKey(entity = JeuDeQuestions::class, parentColumns = ["id"], childColumns = ["idJeuDeQuestions"], onDelete = ForeignKey.CASCADE)] )
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val idJeuDeQuestions: Int,
    val question: String,
    val reponse: String,
    val statut: Int = 1 //Initialisé à 1 par defaut
)