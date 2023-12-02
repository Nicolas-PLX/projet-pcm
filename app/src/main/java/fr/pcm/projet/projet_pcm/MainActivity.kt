package fr.pcm.projet.projet_pcm

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toolbar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.pcm.projet.projet_pcm.GameModel
import fr.pcm.projet.projet_pcm.ui.theme.ProjetpcmTheme

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
fun menuDemarrage(){
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBarPrincipal()},
        //content = { centreMenu() }, //Je l'ai commenté car sinon ça marchait pas
        bottomBar = { BottomBar(navController)}){ padding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(padding)){
            composable("home"){ centreMenu(padding,navController)} //Mettre ici la fonction pour l'écran principale
            composable("jouer"){ GameScreen(padding)}
            composable("modifier"){ GestionDatabaseScreen(padding) }
        }
    }
}

/*TODO A finir*/
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
fun GameScreen(padding : PaddingValues, model: GameModel = viewModel()){
    val context = LocalContext.current
    val themes = LocalContext.current.resources.getStringArray(R.array.theme_array)
    var selected by remember {mutableStateOf("")}
    Column(modifier = Modifier
        .padding(vertical = 64.dp) //taille parfaite par rapport au TopAppBar : au dessus on dépasse, en dessous espace vide
        .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        /*
        Spinner(
            TODO : voir comment faire un bon spinner (les changements de package aident pas ...
            context = context,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            value = selectedTheme,
            onValueChange = { selectedTheme = it },
            items = themeOptions
        )*/
        Text(text = "BONJOUR")
    }

}

//Composable pour l'onglet "Modifier"
@Composable
fun GestionDatabaseScreen(padding : PaddingValues, model: GestionDatabaseModel = GestionDatabaseModel()){

}