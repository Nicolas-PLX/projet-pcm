package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


//Chaque jeu de questions est associé à un thème
@Entity(foreignKeys = [ ForeignKey(entity = Theme::class, parentColumns = ["nom"], childColumns = ["nomTheme"], onDelete = ForeignKey.CASCADE)] )
data class JeuDeQuestions(
    @PrimaryKey val id: Int,
    val nom: String,
    val nomTheme: String
)