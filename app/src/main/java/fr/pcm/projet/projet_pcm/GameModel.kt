package fr.pcm.projet.projet_pcm

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    /* Chargement de la base de donnée */
    val tousLesThemes=data.loadAllTheme()
    var jdq = data.loadJDQName("")
    var qjdq = data.loadQuestionsFromJDQ("")
    var idJDQ = data.loadIdJDQ("")
    var loadQuestions = data.getNbrQuestionsFromJDQ(-1,0)
    var themeGame = ""
    var jdqGame = ""

    /*paramétrage de la partie */
    var temps = 10000 //en milliseconde
    var nbQuestion = 10 // Nombre de question

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
        viewModelScope.launch(Dispatchers.IO){
            qjdq = data.loadQuestionsFromJDQ(n)
        }
    }

    fun loadIdJDQ(n:String){
        idJDQ = data.loadIdJDQ(n)
    }

    fun remplissageThemes(){
        val list = application.resources.getStringArray((R.array.theme_array))
        viewModelScope.launch(Dispatchers.IO){
            for(i in list.indices)
                data.insertTheme(Theme(nom = list[i]))
        }
    }

    fun addNewQuestion(question : String, reponse : String, idJDQ : Int){
        viewModelScope.launch(Dispatchers.IO){
            data.insertQuestion(Question(id = 0, idJeuDeQuestions =  idJDQ,question =question, reponse = reponse))
        }
    }

    suspend fun getQuestionsByContent(questions : List<String>) : List<Question>{
        return data.getQuestionsByContent(questions)

    }

    fun deleteQuestions(questions : List<String>){
        viewModelScope.launch(Dispatchers.IO){
            val questionsSuppr = getQuestionsByContent(questions)
            data.deleteQuestions(questionsSuppr)
        }
    }

    suspend fun getJDQByName(jdq : String) : JeuDeQuestions{
        return data.getJDQbyName(jdq)
    }

    fun deleteJDQ(jdq : String){
        viewModelScope.launch(Dispatchers.IO){
            val jdqSuppr = getJDQByName(jdq)
            data.deleteJDQ(jdqSuppr)
        }
    }

    fun newJDQ(theme : String, jdq : String){
        viewModelScope.launch(Dispatchers.IO){
            data.insertJeuDeQuestions(JeuDeQuestions(id = 0,jdq,theme))
        }
    }

    fun loadNbrQuestionFromJDQ(idJDQ : Int, nbr : Int){
        viewModelScope.launch(Dispatchers.IO){
            loadQuestions = data.getNbrQuestionsFromJDQ(idJDQ,nbr)
        }
    }

    fun verifRep(repUser : String, repQ : String){
        if (repUser == repQ) {
            //Incrémenter le niveau de la question
        } else {
            // Remettre à 0 le niveau des questions
        }
    }
}