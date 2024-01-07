package fr.pcm.projet.projet_pcm

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
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
import kotlin.math.*
import com.opencsv.CSVReaderBuilder
import com.opencsv.CSVParserBuilder
import com.opencsv.enums.CSVReaderNullFieldIndicator
import fr.pcm.projet.projet_pcm.data.Statistique
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileInputStream
import java.util.Date
import java.util.concurrent.TimeUnit

/* Model associé à la Vue du GameActivity */
class GameModel(private val application: Application) : AndroidViewModel (application) {

    private val data = (application as GameApplication).database.dao()
    /* Chargement de la base de donnée */
    val tousLesThemes=data.loadAllTheme()
    var jdq = data.loadJDQName("")
    var qjdq = data.loadQuestionsFromJDQ("")
    var idJDQ = data.loadIdJDQ("")
    var idJDQbis = data.loadIdJDQWithThemeName("")
    var loadQuestions = data.getNbrQuestionsFromJDQ("",0)

    /* Chargement BDD pour les Statistiques et pour l'affichage de questions*/
    var loadHistoJDQ = data.loadAllStatsFromIdJDQ(0)
    var loadHisto = data.getAllHisto()
    var allJdq = data.loadAllJDQ()
    var allQuestions = data.loadAllQuestion(0)

    var themeGame = mutableStateOf("")
    var jdqGame = mutableStateOf("")
    var idjdqGame = mutableStateOf(-2)

    /*paramétrage de la partie */
    var tempsInitial = mutableLongStateOf(15000) //en milliseconde
    var tempsRestant = mutableLongStateOf(15000)
    var nbQuestion = mutableIntStateOf(10) // Nombre de question
    var badRep = mutableIntStateOf(0)
    var nbrQuestionRep = mutableIntStateOf(1)
    var bonneRep = false
    var gameFinished = false

    var questionActuelle = Question(-1,-1,"null","null",-1)
    private var timer: CountDownTimer? = null


    private val prefs = application.getSharedPreferences("Connexion", Context.MODE_PRIVATE)

    private val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private var mapIdToUrl: MutableMap<Long, String> = mutableMapOf()
    private var mapIdToFile: MutableMap<Long, String> = mutableMapOf()
    fun startDownload(adr: String, fName: String){
        val uri = Uri.parse(adr)
        val request = DownloadManager.Request(uri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(application, Environment.DIRECTORY_DOWNLOADS, fName)
        val idLoad: Long = downloadManager.enqueue(request)
        mapIdToUrl[idLoad] = adr
        mapIdToFile[idLoad] = fName
        Log.d("start", "téléchargement commencé : $idLoad")
    }

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?){
            val reference = intent?.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS)
            if(reference!=null) downloadManager.remove(*reference)
        }
    }

    private val cancelFilter = IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED)

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val idDownload: Long = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)?:-1
            if(idDownload == -1L){
                Log.d("Receiver", "idDownload marche pas")
                return
            }
            if(idDownload !in mapIdToUrl.keys){
                Log.d("idDownload", "mauvais $idDownload")
            }
            Log.d("Receiver", "téléchargement réussi")
            dlToBDD(mapIdToFile[idDownload]!!)
        }
    }

    fun dlToBDD(fName: String){
        val file = File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fName)
        val entreeStream = FileInputStream(file)
        val csvReader = CSVReaderBuilder(entreeStream.reader())
            .withCSVParser(CSVParserBuilder().withFieldAsNull(CSVReaderNullFieldIndicator.BOTH).build())
            .build()
        csvReader.use{ reader ->
            reader.skip(1)
            val csvQuestions = reader.readAll().map { row ->
                //Peut être pb ici...
                val idJeuDeQuestions = data.loadIdJDQ(fName).toString().toIntOrNull() ?: 0
                addNewQuestion(question = row[2], reponse = row[3], idJeuDeQuestions)
            }
        }
    }

    private val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

    init {
        application.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        application.registerReceiver(cancelReceiver, cancelFilter, Context.RECEIVER_EXPORTED)
    }

    override fun onCleared(){
        super.onCleared()
        application.unregisterReceiver(receiver)
        application.unregisterReceiver(cancelReceiver)
    }

    fun loadAllQuestions(idJDQ: Int){
        viewModelScope.launch{
            allQuestions = data.loadAllQuestion(idJDQ)
        }
    }
    fun getAllHisto(){
        viewModelScope.launch {
            loadHisto = data.getAllHisto()
        }
    }

    fun loadAllStatsFromIdJDQ(n : Int){
        viewModelScope.launch {
            loadHistoJDQ = data.getAllStatistiqueFromJDQ(n)
        }
    }

    fun getAllJDQ() {
        viewModelScope.launch(Dispatchers.IO){
            allJdq = data.loadAllJDQ()
        }
    }

    fun chargerJDQ(n:String){
        jdq = data.loadJDQName(n)
    }


    /*Fonction qui reset le model*/
    fun resetModel(){
        themeGame = mutableStateOf("")
        jdqGame = mutableStateOf("")
        tempsInitial = mutableLongStateOf(15000) //en milliseconde
        tempsRestant = mutableLongStateOf(15000)
        nbQuestion = mutableIntStateOf(10) // Nombre de question
        badRep = mutableIntStateOf(0)
        nbrQuestionRep = mutableIntStateOf(1)
        bonneRep = false
        gameFinished = false

        questionActuelle = Question(-1,-1,"null","null",-1)
        viewModelScope.launch(Dispatchers.IO){
            jdq = data.loadJDQName("")
            qjdq = data.loadQuestionsFromJDQ("")
            idJDQ = data.loadIdJDQ("")
            loadQuestions = data.getNbrQuestionsFromJDQ("",0)
        }
    }

    fun loadQuestionsFromJDQ(n:String){
        viewModelScope.launch(Dispatchers.IO){
            qjdq = data.loadQuestionsFromJDQ(n)
        }
    }

    fun loadIdJDQ(n:String){
        viewModelScope.launch(Dispatchers.IO) {
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
                val prochainJour = row[5].toIntOrNull() ?: 0
                Question(id = id, idJeuDeQuestions = idJeuDeQuestions, question = row[2], reponse = row[3], statut = statut, prochainJour = prochainJour)
            }
            viewModelScope.launch(Dispatchers.IO){
                data.insertQuestionsList(csvQuestions.map{ it.toQuestion()})
            }
        }
    }

    private fun Question.toQuestion(): Question {
        return Question(id = this.id, idJeuDeQuestions = this.idJeuDeQuestions, question = this.question, reponse = this.reponse, statut = this.statut, prochainJour = this.prochainJour)
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

    fun deleteSingleQuestion(question : Question){
        viewModelScope.launch(Dispatchers.IO){
            data.deleteQuestion(question)
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
                    questionActuelle = questions.first()
                }
            }
        }
    }

    fun verifRep(repUser : String, repQ : String, idQ : Int){

        if (repUser == repQ) {
            bonneRep = true


        } else {
            bonneRep = false
            this.badRep.value++
        }
    }
    suspend fun incrementQuestion(idQ : Int){
        val q = this.data.getQuestion(idQ)
        q.statut = q.statut + 1
        q.prochainJour = /*(q.statut-1).toDouble().pow(2).toInt()*/ 2.0.pow(q.statut - 1).toInt()

        data.updateQuestion(q)
    }

    suspend fun decrementQuestion(idQ : Int){
        val q = this.data.getQuestion(idQ)
        if(q.statut > 1){
            q.statut = 1
            q.prochainJour = 1
        }
        data.updateQuestion(q)
    }

    fun updateStatutQuestion(question : Question,statut : Int){
        viewModelScope.launch(Dispatchers.IO) {
            question.statut = statut
            question.prochainJour = /*(question.statut-1).toDouble().pow(2).toInt()*/2.0.pow(statut - 1).toInt()

            data.updateQuestion(question)
        }
    }

    fun startTimer(){
        timer = object : CountDownTimer(tempsRestant.value, 1000){
            override fun onTick(millisUntilFinished: Long) {
                viewModelScope.launch(Dispatchers.Main){
                    tempsRestant.value = millisUntilFinished
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

        badRep.value = badRep.value + 1
        nbrQuestionRep.value = nbrQuestionRep.value + 1
        viewModelScope.launch(Dispatchers.IO){
            decrementQuestion(questionActuelle.id)
        }
        if (nbQuestion.value == nbrQuestionRep.value){
            finJeu()
        } else {
            loadNextQuestions()
        }
    }

    fun questionRep(repUser : String, idQ : Int){

        questionActuelle?.reponse?.let { verifRep(repUser, it, idQ) }
        nbrQuestionRep.value = nbrQuestionRep.value + 1
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
        }
    }


    fun finJeu(){
        cancelTimer()
        gameFinished = true
        Log.d("AJOUT STATS :","${idjdqGame.value} | ${nbQuestion.value} |${jdqGame.value}| ${nbQuestion.value - badRep.value}")

        viewModelScope.launch(Dispatchers.IO) {

        data.insertStatistique(Statistique(id = 0,
                idjdqGame.value,
                jdqGame.value,
                nbQuestion.value,
                nbQuestion.value - badRep.value,
            ))
        }
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