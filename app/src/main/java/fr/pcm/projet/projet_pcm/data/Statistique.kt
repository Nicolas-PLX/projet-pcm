package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = JeuDeQuestions::class,
            parentColumns = ["id"],
            childColumns = ["idJeuDeQuestions"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idJeuDeQuestions")]
)
data class Statistique(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val idJeuDeQuestions: Int,
    val nomJeuDeQuestions : String,
    val nbrQ: Int,
    val nbrBonneRep: Int,
    val date: Date = Calendar.getInstance().time
)