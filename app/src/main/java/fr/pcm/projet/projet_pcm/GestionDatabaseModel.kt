package fr.pcm.projet.projet_pcm

import android.app.Application
import androidx.lifecycle.AndroidViewModel

/* Model de la vu associé à la gestion de la DB*/
class GestionDatabaseModel (private val application: Application) : AndroidViewModel (application) {

    private val data = (application as GameApplication).database.dao()
}