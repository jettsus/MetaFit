package com.example.MetaFit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Frame2(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A1A), Color(0xFF4f378a))
                )
            )
    ) {
        //Icono
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .size(70.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF4f378a))
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Icono",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(35.dp)
                    .clickable {
                        FirebaseAuth.getInstance().signOut() // Cierra sesión en Firebase
                        navController.navigate("login") {
                            popUpTo("Frame2") { inclusive = true } // Limpia la pila de navegación
                        }
                    },
                tint = Color.White
            )
        }

        // Botón de "Entrenamiento" con fondo verde lima
        Button(
            onClick = { navController.navigate("Entrenamientos") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 140.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFF00)),
            shape = RoundedCornerShape(30.dp),
        ) {
            Text(
                text = "Entrenamiento",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Botón de "IMC" con fondo verde lima
        Button(
            onClick = { navController.navigate("IMC") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 220.dp)
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFF00)),
            shape = RoundedCornerShape(30.dp),
        ) {
            Text(
                text = "IMC",
                color = Color.Black,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

    }
}
