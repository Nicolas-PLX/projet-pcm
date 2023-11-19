package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Japonais(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val question: String,
    val reponse: String,
    val statut: Int? //Doit etre initialisé à 1 par defaut
)