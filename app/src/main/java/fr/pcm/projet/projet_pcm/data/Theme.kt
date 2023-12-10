package fr.pcm.projet.projet_pcm.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity
data class Theme(
    @PrimaryKey val nom: String
)