package fr.pcm.projet.projet_pcm

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
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
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/* Model associé à la Vue du GameActivity */
class GameModel(private val application: Application) : AndroidViewModel (application) {

    private val data = (application as GameApplication).database.dao()
    /* Chargement de la base de donnée */
    val tousLesThemes=data.loadAllTheme()
    var jdq = data.loadJDQName("")
    var qjdq = data.loadQuestionsFromJDQ("")
    var idJDQ = data.loadIdJDQ("")
    var loadQuestions = data.getNbrQuestionsFromJDQ("",0)
    var themeGame = mutableStateOf("")
    var jdqGame = mutableStateOf("")

    /*paramétrage de la partie */
    var tempsInitial = mutableLongStateOf(15000) //en milliseconde
    var tempsRestant = mutableLongStateOf(15000)
    var nbQuestion = mutableIntStateOf(10) // Nombre de question
    var badRep = mutableIntStateOf(0)
    var nbrQuestionRep = mutableIntStateOf(1)
    var bonneRep = false

    var questionActuelle = Question(-1,-1,"null","null",-1)
    private var timer: CountDownTimer? = null

    private val prefs = application.getSharedPreferences("Connexion", Context.MODE_PRIVATE)



    fun chargerJDQ(n:String){
        //viewModelScope.launch(Dispatchers.IO){
         //   jdq = data.loadJDQName(n)

        //}
        jdq = data.loadJDQName(n)
    }

    fun loadQuestionsFromJDQ(n:String){
        viewModelScope.launch(Dispatchers.IO){
            qjdq = data.loadQuestionsFromJDQ(n)
        }
    }

    fun loadIdJDQ(n:String){
        viewModelScope.launch(Dispatchers.IO){
            idJDQ = data.loadIdJDQ(n)
        }
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

    fun loadQuestions() {
        viewModelScope.launch(Dispatchers.IO) {
            loadQuestions = data.getNbrQuestionsFromJDQ(jdqGame.value, nbQuestion.value)
        }
    }


    fun loadNextQuestions(){
        viewModelScope.launch(Dispatchers.IO){
            loadQuestions.collect { questions ->
                if (questions.isNotEmpty()){
                    questionActuelle = questions.get(nbrQuestionRep.value)
                }
            }
        }
    }

    fun verifRep(repUser : String, repQ : String, idQ : Int){
        Log.d("VERIF_REP","$repUser")
        Log.d("VERIF_REP","$repQ")
        Log.d("QCT VERIF REP AVANT","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

        if (repUser == repQ) {
            Log.d("VERIF_REP","TRUE")
            bonneRep = true
            Log.d("VERIF_BONNEREP","$bonneRep")
            Log.d("QCT VERIF REP TRUE","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")


        } else {
            Log.d("VERIF_REP","FALSE")
            bonneRep = false
            Log.d("VERIF_BONNEREP","$bonneRep")
            Log.d("QCT VERIF REP FALSE","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

            this.badRep.value++
        }
    }
    suspend fun incrementQuestion(idQ : Int){
        val q = this.data.getQuestion(idQ)
        q.statut = q.statut + 1

        data.updateQuestion(q)
    }

    suspend fun decrementQuestion(idQ : Int){
        val q = this.data.getQuestion(idQ)
        q.statut = q.statut - 1

        data.updateQuestion(q)
    }

    fun startTimer(){
        timer = object : CountDownTimer(tempsRestant.value, 1000){
            override fun onTick(millisUntilFinished: Long) {
                viewModelScope.launch(Dispatchers.Main){
                    tempsRestant.value = millisUntilFinished
                    Log.d("TIMER","${tempsRestant.value}")
                }

            }

            override fun onFinish() {
                questionNonRep()
            }
        }.start()
    }

    fun resetTimer(){
        timer?.cancel()
        tempsRestant.value = tempsInitial.value
        viewModelScope.launch(Dispatchers.Main){
            startTimer()
        }
    }

    fun cancelTimer(){
        timer?.cancel()
        tempsRestant.value = tempsInitial.value
    }

    fun questionNonRep(){
        Log.d("QCT QUESTIONNONREP","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

        badRep.value + 1
        nbrQuestionRep.value + 1
        viewModelScope.launch(Dispatchers.IO){
            decrementQuestion(questionActuelle.id)
        }
        if (nbQuestion.value == nbrQuestionRep.value){
            finJeu()
            Log.d("QCT NONREP FIN","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

        } else {
            Log.d("QCT NONREPEND","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

            loadNextQuestions()
            /*
            viewModelScope.launch(Dispatchers.IO){
                loadQuestions.collect { questions ->
                    if (questions.isNotEmpty()){
                        questionActuelle = questions.first()
                    }
                }
            }*/
        }
    }

    fun questionRep(repUser : String, idQ : Int){
        Log.d("QCT QUESTION REP","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

        questionActuelle?.reponse?.let { verifRep(repUser, it, idQ) }
        nbrQuestionRep.value + 1
        if(bonneRep){
            viewModelScope.launch(Dispatchers.IO){
                incrementQuestion(idQ)
            }
        } else {
            viewModelScope.launch(Dispatchers.IO){
                decrementQuestion(idQ)
            }
        }
        if (nbQuestion.value == nbrQuestionRep.value){
            finJeu()
        } else {
            loadNextQuestions()
            Log.d("QCT REPEND","${questionActuelle.id} | ${questionActuelle.question} | ${questionActuelle.reponse} | ")

            /*viewModelScope.launch(Dispatchers.IO){
                loadQuestions.collect { questions ->
                    if (questions.isNotEmpty()){
                        questionActuelle = questions.first()
                    }
                }
            }*/
        }
    }

    fun finJeu(){
        /*Todo Fonction fin de jeu*/
    }

    fun startJeu(){
        this.nbrQuestionRep.value = 0
        tempsRestant.value = tempsInitial.value
        loadQuestions()
        loadNextQuestions()

        startTimer()
    }

    /* CONNEXION JOURNALIERE POUR REDUIRE LE STATUT DE 1 POUR QUE LE JOUEUR PUISSE JOUER */

    fun updateStatus(){
        if(isFirstLaunch()){
            viewModelScope.launch(Dispatchers.IO){
                data.updateQuestionStatus()
            }
        }
    }

    fun isFirstLaunch() : Boolean{
        val lastLaunchDate = prefs.getLong(LAST_LAUNCH_DATE_KEY, 0)
        val currentDate = System.currentTimeMillis()

        if(currentDate - lastLaunchDate >= TimeUnit.DAYS.toMillis(1)){
            prefs.edit().putLong(LAST_LAUNCH_DATE_KEY, currentDate).apply()
            return true
        }
    return false
    }

    companion object {
        private const val LAST_LAUNCH_DATE_KEY = "last_launch_date"
    }


}