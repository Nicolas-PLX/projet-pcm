package fr.pcm.projet.projet_pcm

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.pcm.projet.projet_pcm.data.Question
import fr.pcm.projet.projet_pcm.ui.theme.ProjetpcmTheme
import kotlinx.coroutines.delay
import java.lang.NumberFormatException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjetpcmTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    menuDemarrage()
                }
            }
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") //temporaire
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun menuDemarrage(model : GameModel = viewModel()){
    model.updateStatus()
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBarPrincipal()},
        //content = { centreMenu() }, //Je l'ai commenté car sinon ça marchait pas
        bottomBar = { BottomBar(navController)}){ padding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(padding)){
            composable("home"){ centreMenu(padding,navController)} //Mettre ici la fonction pour l'écran principale
            composable("jouer"){ DebutGameScreen(padding,navController,model)}
            composable("modifier"){ GestionDatabaseScreen(padding,navController,model)}
            composable("modifierJDQ"){GestionJDQScreen(padding,navController,model)}
            composable("modifierQ"){ GestionQScreen(padding,navController,model)}
            composable("game"){GameScreen(padding, navController,model)}
        }
    }
}



@Composable
fun BottomBar(navController: NavHostController) = BottomNavigation{
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BottomNavigationItem(
        selected = currentRoute == "home",
        onClick = { navController.navigate("home"){launchSingleTop = true} },
        icon = {Icon(Icons.Default.Home,"home") })
    BottomNavigationItem(
        selected = currentRoute == "jouer",
        onClick = { navController.navigate("jouer"){popUpTo("home")} },
        icon = { Icon(Icons.Default.ArrowForward,"jouer") })
    BottomNavigationItem(
        selected = currentRoute == "modifier",
        onClick = { navController.navigate("modifier"){popUpTo("home")} },
        icon = { Icon(Icons.Default.Build, "modifier") })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
//TODO : s'occuper plus tard du style couleur etc ... c'est pas important pour l'instant
fun TopBarPrincipal() = // Dans l'idéal, ici mettre le nom du jeu ou je sais pas quoi, avec le bouton paramètre ?
    TopAppBar(title = {Text(text = "Bienvenue !", modifier = Modifier
        .fillMaxWidth()
        /*.fillMaxHeight()*/,
        // TODO : faire l'alignement (je suis golmon) .align(CenterVertically),
        style = TextStyle(color = Color.Black, fontSize = 24.sp))},
        navigationIcon = {
            Image(painter = painterResource(id = R.drawable.engrenage), //Todo : rendre l'image vraiment png?
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Transparent)
                    .clickable {/*Todo: action sur le click*/
                    })
        }, modifier = Modifier.background(Color.Green),
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color(248,203,219),
            titleContentColor = MaterialTheme.colorScheme.primary,
        )

    )


@Composable
fun centreMenu(padding : PaddingValues,navController: NavHostController){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Column (
        modifier = Modifier
            .padding(vertical = 64.dp) //taille parfaite par rapport au TopAppBar : au dessus on dépasse, en dessous espace vide
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Row(){
            Button(onClick = { currentRoute == "jouer"
                navController.navigate("jouer"){popUpTo("home")} },
            modifier = Modifier.padding(end = 8.dp)) {
                Text(text = "Jouer")
            }
            Button(onClick = { currentRoute == "modifier"
                             navController.navigate("modifier"){popUpTo("home")}},
                modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "Modifier")
            }

        }
    }

}

//Composable pour l'onglet "Jouer"
@Composable
fun DebutGameScreen(padding : PaddingValues,navController: NavHostController, model: GameModel/* = viewModel()*/){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedTheme by remember {mutableStateOf("Thème")}
    var selectedJDQ by remember { mutableStateOf("Jeux") }
    val jdq by model.jdq.collectAsState(listOf())
    val allThemes by model.tousLesThemes.collectAsState(listOf())
    model.remplissageThemes()
    model.remplissageJDQ()
    model.remplissageQuestions()
    Column(modifier = Modifier
        .padding(vertical = 64.dp) //taille parfaite par rapport au TopAppBar : au dessus on dépasse, en dessous espace vide
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)) {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { expanded = true }) { Text(selectedTheme, fontSize = 25.sp) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    allThemes.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme.nom, fontSize = 25.sp) },
                            onClick = { selectedTheme = theme.nom;expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(horizontal = 5.dp)) {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { model.chargerJDQ(selectedTheme)
                    expanded = true }) { Text(selectedJDQ, fontSize = 25.sp) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    jdq.forEach { jd ->
                        DropdownMenuItem(text = { Text("$jd") },
                            onClick = {selectedJDQ = jd; expanded = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        var temps by remember { mutableStateOf("15") }
        var nbrQuestion by remember { mutableStateOf("10") }
        Row {
            OutlinedTextField(value = temps, onValueChange = {temps = it}, label = {Text("Temps (seconde)")},
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)/*,
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = White)*/
            )
            OutlinedTextField(value = nbrQuestion, onValueChange = {nbrQuestion = it}, label = {Text("Nombre de questions")},
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)/*,
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = White)*/
            )
        }
        Spacer(modifier = Modifier.height(60.dp))
        Row {
            Button(onClick = { navController.navigateUp() }) {
                Text(text = "Retour")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { if (verificationThemeEtJDQ(selectedTheme,selectedJDQ) && verifParam(nbrQuestion,temps)){ /*Todo : lancer une partie */
                model.tempsRestant.value = temps.toLong() * 1000
                model.tempsInitial.value = model.tempsRestant.value
                model.nbQuestion.value = nbrQuestion.toInt()
                model.jdqGame.value = selectedJDQ; model.themeGame.value = selectedTheme
                currentRoute == "game"; navController.navigate("game")
            }}) {
                Text("Jouer")
            }
        }
    }
}

fun verifParam(nbr : String, temps : String) : Boolean{
    return try {
        val int_nbr = nbr.toInt()
        val int_time = temps.toInt()
        int_nbr > 0 && int_time > 0
    } catch (e : NumberFormatException) {
        false
    }
}
/* Todo pour moi demain
Faire la fin d'un jeu (quand il n'y a plus de questions)
Gérer le bug du timer qui ne se re affiche pas quand on répond à une question (mais qui se reset si on ne repond pas)
Vérifier que "ma" et "me" sont passé à 1 dans le statut

 */
@Composable
fun GameScreen(padding: PaddingValues, navController: NavHostController, model: GameModel){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var timer by remember { mutableLongStateOf(model.tempsRestant.value)}
    var rep by remember {mutableStateOf("")}
    var questionRep by remember { mutableStateOf(false) }
    var isGameRuning by remember { mutableStateOf(false) }


    var show by remember{mutableStateOf(" ")}

    var repQuestion by remember{ mutableStateOf("") }

    Log.d("TIMER DEBUT","$timer")

    if(timer <= 1000 && isGameRuning == true){
        isGameRuning = false
        repQuestion = model.questionActuelle.reponse
        model.questionNonRep()
        var GoodOrFalse = model.bonneRep
        GameScreenAlertDialog(
            bonneRep = GoodOrFalse,
            rep = repQuestion,
            relanceTimer = {model.resetTimer()},
            reponse = {questionRep = false},
            refreshRep = {rep = " "}
        )
    }

    if (questionRep){
        var GoodOrFalse = model.bonneRep
        GameScreenAlertDialog(
            bonneRep = GoodOrFalse,
            rep = repQuestion,
            relanceTimer = {model.resetTimer()},
            reponse = {questionRep = false},
            refreshRep = {rep = " "}
        )
    }


    model.loadIdJDQ(model.jdqGame.value)
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)){
            var timerInSeconds = timer / 1000
            Text("$timerInSeconds", fontSize = 60.sp)
        }
    }
    Column(modifier = Modifier
        .padding(vertical = 64.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            val question = model.questionActuelle
            Box(modifier = Modifier
                .border(1.dp, Color(0.9725f, 0.7961f, 0.8588f), RectangleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp)) {

                var idQ = question.id
                if (idQ != -1){show = question.question}
                Text(show, fontSize = 30.sp)

            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            OutlinedTextField(value = rep, onValueChange = {rep = it}, /*colors = TextFieldDefaults.outlinedTextFieldColors(textColor = White)*/)
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Button(onClick = {
                repQuestion = model.questionActuelle.reponse
                Log.d("REP MAIN","$rep")
                Log.d("QCT MAIN ","${model.questionActuelle.id} | ${model.questionActuelle.question} | ${model.questionActuelle.reponse} | ")
                model.questionRep(rep,model.questionActuelle.id)
                Log.d("QCT MAIN ","${model.questionActuelle.id} | ${model.questionActuelle.question} | ${model.questionActuelle.reponse} | ")
                questionRep = true
                isGameRuning = false
                model.cancelTimer()
                Log.d("QCT MAIN APRES CANCEL","${model.questionActuelle.id} | ${model.questionActuelle.question} | ${model.questionActuelle.reponse} | ")

            }){Text("Valider")}
        }
        Spacer(modifier = Modifier.height(80.dp))
        Row {
            Button(onClick = {model.startJeu();/*model.loadNextQuestions()*/

                isGameRuning = true}) {Text("Start")}
        }

        LaunchedEffect(isGameRuning,timer){
            while (isGameRuning && model.tempsRestant.value > 0){
                delay(1000)
                timer = model.tempsRestant.value
                Log.d("TIMER LAUNCHED","$timer")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GameScreenAlertDialog(bonneRep:
                          Boolean, rep : String,
                          relanceTimer:() -> Unit,
                          reponse:() -> Unit,
                          refreshRep:() -> Unit){

    AlertDialog(
        text = {
            if (bonneRep) {
                Text("Bonne Réponse !")
            } else {
                Text("Mauvaise Réponse !\nRéponse : $rep")
            }
        },
        onDismissRequest = {
            relanceTimer()
            reponse()
            refreshRep()
        },
        confirmButton = {
            Button(
                onClick = {
                    relanceTimer()
                    reponse()
                    refreshRep()
                }
            ) {
                Text("OK")
            }
        }
    )
}






//Fonction qui vérifie si les paramètres de lancement sont corrects, càd s'il y a bien un thème et un jdq qui ont été sélectionné
fun verificationThemeEtJDQ(th : String, jdq : String) : Boolean{
    return (th != "Thème") && (jdq != "Jeux")
}

//Composable pour l'onglet "Modifier"
@Composable
fun GestionDatabaseScreen(padding : PaddingValues,navController: NavHostController, model: GameModel){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Column(modifier = Modifier
        .padding(vertical = 64.dp)
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally)  {
        Row{
            Button(onClick = { currentRoute == "modifierJDQ"; navController.navigate("modifierJDQ")}) {Text("Créer / Supprimer un jeu")}
            Button(onClick = { currentRoute == "modifierQ" ; navController.navigate("modifierQ") }) {Text("Ajouter / Enlever une question")}
        }
        Row{
            Button(onClick = { navController.navigateUp() }) {Text("Retour")}
        }
    }
}

/* Fonction qui s'occupera de modifier (créer ou supprimer) un jeu de question . Il faudra aussi qu'il puisse le télécharger*/
@Composable
fun GestionJDQScreen(padding: PaddingValues, navController: NavHostController,model: GameModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf("Thème") }
    var selectedJDQ by remember { mutableStateOf("Jeux") }
    val jdq by model.jdq.collectAsState(listOf())
    val allThemes by model.tousLesThemes.collectAsState(listOf())
    model.remplissageThemes()
    Column(
        modifier = Modifier
            .padding(vertical = 64.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)) {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = { expanded = true }) { Text(selectedTheme, fontSize = 25.sp) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    allThemes.forEach { theme ->
                        DropdownMenuItem(text = { Text(theme.nom, fontSize = 25.sp) },
                            onClick = { selectedTheme = theme.nom;expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 5.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                TextButton(onClick = {
                    model.chargerJDQ(selectedTheme)
                    expanded = true
                }) { Text(selectedJDQ, fontSize = 25.sp) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    jdq.forEach { jd ->
                        DropdownMenuItem(text = { Text("$jd") },
                            onClick = { selectedJDQ = jd; expanded = false })
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        var newJDQ by remember { mutableStateOf("") }
        Row {
            Text("Nom du nouveau jeu :")
            OutlinedTextField(value = newJDQ, onValueChange = { newJDQ = it })
        }
        Row {
            Button(onClick = { navController.navigateUp() }) { Text("Retour") }
            Button(onClick = {if (newJDQ != "" && selectedTheme != ""){
                model.newJDQ(selectedTheme,newJDQ)
                Toast.makeText(context,"Le jeu de question a bien été créé.",Toast.LENGTH_LONG).show()
            } }) { Text("Créer") }
            Button(onClick = {if (verificationThemeEtJDQ(selectedTheme,selectedJDQ)){ /* TODO : supprimer aussi les questions qui sont dans le jeu de question*/
                model.deleteJDQ(selectedJDQ)
                Toast.makeText(context,"Le jeu de question a bien été supprimé.",Toast.LENGTH_LONG).show()
            }
            }) {Text("Supprimer")}
        }
    }
}


    /* Fonction qui s'occupera de modifier (ajouter ou enlever) une question d'un jeu de question. */
    @Composable
    fun GestionQScreen(padding: PaddingValues, navController: NavHostController, model: GameModel) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val context = LocalContext.current
        var selectedTheme by remember { mutableStateOf("Thème") }
        var selectedJDQ by remember { mutableStateOf("Jeux") }
        val jdq by model.jdq.collectAsState(listOf())
        val allThemes by model.tousLesThemes.collectAsState(listOf())
        val listeQuestions by model.qjdq.collectAsState(listOf())
        val id by model.idJDQ.collectAsState(initial = 0)
        model.remplissageThemes()
        Row {
            Text(
                "Si vous souhaitez ajouter une question, veuillez selectionner en premier lieu" +
                        "un thème, puis un jeu de question. Ensuite, veuillez insérer votre question et sa" +
                        "réponse, puis presser le bouton.\n" +
                        "Si vous souhaitez enlever une question, faites de même, puis en affichant la liste" +
                        "de question déjà existante, cliquer pour sélectionner les questions que vous souhaitez" +
                        "supprimer, puis presser le bouton correspondant."
            )
        }
        Column(
            modifier = Modifier
                .padding(vertical = 64.dp) //taille parfaite par rapport au TopAppBar : au dessus on dépasse, en dessous espace vide
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Box(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)) {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedTheme, fontSize = 25.sp)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        allThemes.forEach { theme ->
                            DropdownMenuItem(text = { Text(theme.nom, fontSize = 25.sp) },
                                onClick = { selectedTheme = theme.nom;expanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 5.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    TextButton(onClick = {
                        model.chargerJDQ(selectedTheme)
                        expanded = true
                    }) { Text(selectedJDQ, fontSize = 25.sp) }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        jdq.forEach { jd ->
                            DropdownMenuItem(text = { Text("$jd") },
                                onClick = { selectedJDQ = jd;model.loadQuestionsFromJDQ(selectedJDQ); expanded = false })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            var selectedQuestions = afficherQuestions(liste = listeQuestions)
            var newQuestion by remember { mutableStateOf("") }
            var newQuestionRep by remember { mutableStateOf("") }
            Row {
                Text("Votre question :")
                OutlinedTextField(value = newQuestion, onValueChange = { newQuestion = it })
            }
            Row {
                Text("Votre réponse :")
                OutlinedTextField(value = newQuestionRep, onValueChange = { newQuestionRep = it })
            }
            Row {
                Button(onClick = { navController.navigateUp() }) { Text("Retour") }

                Button(onClick = {
                    if (verifAjout(newQuestion, newQuestionRep)) {
                        model.loadIdJDQ(selectedJDQ)
                        model.addNewQuestion(newQuestion, newQuestionRep, id)
                        Toast.makeText(
                            context,
                            "Votre question à bien été ajouté.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) { Text("Ajouter") }

                Button(onClick = {
                    if (!selectedQuestions.isEmpty()) {
                        model.deleteQuestions(selectedQuestions.toList())
                        Toast.makeText(
                            context,
                            "Le(s) question(s) ont bien été enlevé.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) { Text("Enlever") }
            }
        }
    }



fun verifAjout(question : String, reponse : String) : Boolean {
    return (question != "") && (reponse != "")
}

@Composable
/*TODO : mieux gérer l'affichage */
fun afficherQuestions(liste : List<String>) : Set<String>{
    var selectedQuestions by remember { mutableStateOf<Set<String>>(emptySet()) }

    if (liste.isEmpty()){
        Text("La liste est vide")
    } else {
        LazyColumn(
            modifier =
            Modifier
                .padding(15.dp)
                .fillMaxSize(0.8f)
                .height(60.dp)

        ) {
            item {
                Text("Liste de questions")
            }
            items(liste) {
                question -> val selected = selectedQuestions.contains(question)
                Card(
                    Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clickable {
                            selectedQuestions = if (selected) {
                                selectedQuestions - question
                            } else {
                                selectedQuestions + question
                            }
                        }
                        .background(if (selected) Color.Gray else Color.White)
                ) { Text(question, fontSize = 15.sp) }
            }
        }
    }
    return  selectedQuestions
}



