package com.example.MetaFit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.MetaFit.ui.theme.MetaFitTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Inicializa Firebase

        setContent {
            MetaFitTheme {
                val navController = rememberNavController()
                MainNavigation(navController)
            }
        }
    }
}

@Composable
fun MainNavigation(navController: NavHostController) {
    // Configura el NavHost para manejar la navegación entre pantallas
    NavHost(navController = navController, startDestination = "SplashScreen") {

        // Composable para la pantalla inicial (SplashScreen)
        composable("SplashScreen") { SplashScreen(navController) }

        // Composable para la pantalla de login
        composable("login") { LoginScreen(navController) }

        // Composable para la pantalla principal después del login
        composable("Frame2") { Frame2(navController) }

        // Composable para la pantalla de registro
        composable("Registro") { Registro(navController) }

        // Composable para la pantalla de Entrenamientos
        composable("Entrenamientos") { Entrenamientos(navController) }

        // Composable para la pantalla de DiasEntrenamiento
        composable("DiasEntrenamiento/{nombreBloque}") { backStackEntry ->
            val nombreBloque = backStackEntry.arguments?.getString("nombreBloque") ?: "Sin Nombre"
            DiasEntrenamiento(nombreBloque = nombreBloque, navController = navController)
        }

        // Composable para la pantalla de ejercicios de un bloque específico
        composable("Ejercicios/{nombreBloque}") { backStackEntry ->
            val nombreBloque = backStackEntry.arguments?.getString("nombreBloque") ?: "Sin Nombre"
            Ejercicios(nombreBloque = nombreBloque, navController = navController)
        }

        // Composable para la pantalla de series dentro de un ejercicio
        composable("Series/{nombreBloque}/{nombreDiaEntrenamiento}/{nombreEjercicio}") { backStackEntry ->
            val nombreBloque = backStackEntry.arguments?.getString("nombreBloque") ?: "Sin Nombre"
            val nombreEjercicio = backStackEntry.arguments?.getString("nombreEjercicio") ?: "Sin Nombre"

            // Llama a la función Series con los parámetros correctos
            Series(
                nombreBloque = nombreBloque,
                nombreEjercicio = nombreEjercicio,
                navController = navController
            )
        }
        composable("IMC") { IMCScreen(navController) }


    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        // Espera 2 segundos antes de continuar
        kotlinx.coroutines.delay(2000L)

        // Verifica si el usuario está autenticado
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Usuario autenticado, redirige a Frame2
            navController.navigate("Frame2") {
                // Elimina ventanas anteriores a esto
                popUpTo("SplashScreen") { inclusive = true }
            }
        } else {
            // Usuario no autenticado, redirige a LoginScreen
            navController.navigate("login") {
                popUpTo("SplashScreen") { inclusive = true }
            }
        }
    }

    // Interfaz del Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Imagen en el centro del SplashScreen
        Image(
            painter = painterResource(id = R.drawable.icono4),
            contentDescription = "Splash Icono",
            modifier = Modifier.size(150.dp)
        )
    }
}
