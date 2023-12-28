package fr.pcm.projet.projet_pcm

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.pcm.projet.projet_pcm.GameApplication
import fr.pcm.projet.projet_pcm.R
import fr.pcm.projet.projet_pcm.data.Theme
import fr.pcm.projet.projet_pcm.data.JeuDeQuestions
import fr.pcm.projet.projet_pcm.data.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* Model associé à la Vue du GameActivity */
class GameModel(private val application: Application) : AndroidViewModel (application) {

    private val data = (application as GameApplication).database.dao()

    val tousLesThemes=data.loadAllTheme()
    var jdq = data.loadJDQName("")
    var qjdq = data.loadQuestionsFromJDQ("")


    fun chargerJDQ(n:String){
        //viewModelScope.launch(Dispatchers.IO){
         //   jdq = data.loadJDQName(n)

        //}
        jdq = data.loadJDQName(n)
    }

    fun loadQuestionsFromJDQ(n:String){
        //viewModelScope.launch(Dispatchers.IO){
        //    qjdq = data.loadQuestionsFromJDQ(n)
        //}
        qjdq = data.loadQuestionsFromJDQ(n)
    }

    fun remplissageThemes(){
        val list = application.resources.getStringArray((R.array.theme_array))
        viewModelScope.launch(Dispatchers.IO){
            for(i in list.indices)
                data.insertTheme(Theme(nom = list[i]))
        }
    }
}