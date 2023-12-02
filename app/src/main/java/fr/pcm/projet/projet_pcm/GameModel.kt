package fr.pcm.projet.projet_pcm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.pcm.projet.projet_pcm.GameApplication
import fr.pcm.projet.projet_pcm.R
import fr.pcm.projet.projet_pcm.data.Theme
import fr.pcm.projet.projet_pcm.data.Japonais
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* Model associé à la Vue du GameActivity */
class GameModel(private val application: Application) : AndroidViewModel (application) {

    private val dao = (application as GameApplication).database.dao()

    //jsp pourquoi ça marche pas
    //var nomTheme = dao.loadThemeName()

    fun listeThemes(){
        val list = application.resources.getStringArray((R.array.theme_array))
        viewModelScope.launch(Dispatchers.IO){

        }
    }
}