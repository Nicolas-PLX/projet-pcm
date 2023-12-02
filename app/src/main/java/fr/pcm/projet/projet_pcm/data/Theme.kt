package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


//Mon idée c'était une table pour les jeux de question mais jsp si c'est une bonne idée ou bien fait
@Entity
data class Theme(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val jeuDeQuestion: String,
    val nom: String
)