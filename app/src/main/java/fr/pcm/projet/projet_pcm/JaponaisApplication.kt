package fr.pcm.projet.projet_pcm

import android.app.Application
import fr.pcm.projet.projet_pcm.data.BD

class JaponaisApplication : Application() {
    val database: BD by lazy {BD.getDataBase(this)}
}