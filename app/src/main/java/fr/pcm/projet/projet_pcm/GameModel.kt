package fr.pcm.projet.projet_pcm

import android.app.Application
import android.os.CountDownTimer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVParserBuilder
import com.opencsv.enums.CSVReaderNullFieldIndicator

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
    var tempsInitial = mutableLongStateOf(15000) //en milliseconde
    var tempsRestant = mutableLongStateOf(15000)
    var nbQuestion = mutableIntStateOf(10) // Nombre de question
    var badRep = mutableIntStateOf(0)
    var nbrQuestionRep = mutableIntStateOf(0)
    private var timer: CountDownTimer? = null

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
        val entreeStream = application.resources.openRawResource(R.raw.theme)
        val csvReader = CSVReaderBuilder(entreeStream.reader())
            .withCSVParser(CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build())
            .build()
        csvReader.use{ reader ->
            reader.skip(1)
            val csvThemes = reader.readAll().map { row ->
                Theme(nom = row[0])
            }
            viewModelScope.launch(Dispatchers.IO){
                data.insertThemesList(csvThemes.map{ it.toTheme()})
            }
        }
    }

    private fun Theme.toTheme(): Theme {
        return Theme(nom = this.nom)
    }

    fun remplissageJDQ(){
        val entreeStream = application.resources.openRawResource(R.raw.jeudequestions)
        val csvReader = CSVReaderBuilder(entreeStream.reader())
            .withCSVParser(CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build())
            .build()
        csvReader.use{ reader ->
            reader.skip(1)
            val csvJDQ = reader.readAll().map { row ->
                val id = row[0].toIntOrNull() ?: 0
                JeuDeQuestions(id = id, nom = row[1], nomTheme = row[2])
            }
            viewModelScope.launch(Dispatchers.IO){
                data.insertJeuxDeQuestionsList(csvJDQ.map{ it.toJDQ()})
            }
        }
    }

    private fun JeuDeQuestions.toJDQ(): JeuDeQuestions {
        return JeuDeQuestions(id = this.id, nom = this.nom, nomTheme = this.nomTheme)
    }

    fun remplissageQuestions(){
        val entreeStream = application.resources.openRawResource(R.raw.question)
        val csvReader = CSVReaderBuilder(entreeStream.reader())
            .withCSVParser(CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build())
            .build()
        csvReader.use{ reader ->
            reader.skip(1)
            val csvQuestions = reader.readAll().map { row ->
                val id = row[0].toIntOrNull() ?: 0
                val idJeuDeQuestions = row[1].toIntOrNull() ?: 0
                val statut = row[4].toIntOrNull() ?: 0
                Question(id = id, idJeuDeQuestions = idJeuDeQuestions, question = row[2], reponse = row[3], statut = statut)
            }
            viewModelScope.launch(Dispatchers.IO){
                data.insertQuestionsList(csvQuestions.map{ it.toQuestion()})
            }
        }
    }

    private fun Question.toQuestion(): Question {
        return Question(id = this.id, idJeuDeQuestions = this.idJeuDeQuestions, question = this.question, reponse = this.reponse, statut = this.statut)
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
            viewModelScope.launch(Dispatchers.IO){
                incrementQuestion(repUser)
            }
        } else {
            this.badRep.value++
            viewModelScope.launch(Dispatchers.IO){
                decrementQuestion(repUser)
            }
        }
    }

    suspend fun incrementQuestion(question : String){
        val q = this.data.getQuestion(question)
        q.statut = q.statut + 1

        data.updateQuestion(q)
    }

    suspend fun decrementQuestion(question : String){
        val q = this.data.getQuestion(question)
        q.statut = q.statut - 1

        data.updateQuestion(q)
    }

    fun startTimer(){
        timer = object : CountDownTimer(tempsRestant.value, 1000){
            override fun onTick(millisUntilFinished: Long) {
                tempsRestant.value = millisUntilFinished
            }

            override fun onFinish() {
                questionNonRep()
            }
        }.start()
    }

    fun resetTimer(){
        timer?.cancel()
        tempsRestant.value = tempsInitial.value
        startTimer()
    }

    fun questionNonRep(){
        badRep.value + 1
    }

    fun finJeu(){
        /*Todo Fonction fin de jeu*/
    }
}