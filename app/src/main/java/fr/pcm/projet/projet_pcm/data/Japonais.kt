package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ ForeignKey(entity = Theme::class, parentColumns = ["jeuDeQuestion"], childColumns = ["jeuDeQuestion"], onDelete = ForeignKey.CASCADE)] )
data class Japonais(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val jeuDeQuestion: String, //Je me suis dit que j'allais rajouter ça comme chaque question est associé à un jeu de question
    val question: String,
    val reponse: String,
    val statut: Int? //Doit etre initialisé à 1 par defaut
)