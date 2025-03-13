@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.MetaFit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.platform.LocalConfiguration
@Composable
fun LoginScreen(navController: NavHostController) {
    val estadoEmail = rememberSaveable { mutableStateOf("") }
    val estadoContrasena = rememberSaveable { mutableStateOf("") }
    val estadoErrorMensaje = rememberSaveable { mutableStateOf<String?>(null) }
    val mostrarContrasena = rememberSaveable { mutableStateOf(false) }

    // Detectar la orientación
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black, Color(0xFF1A1A1A)) // Degradado negro a gris oscuro
                )
            )
    ) {
        if (isPortrait) {
            // Diseño en modo vertical
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleSection()
                IconSection("Log in")
                FormSection(
                    estadoEmail = estadoEmail,
                    estadoContrasena = estadoContrasena,
                    mostrarContrasena = mostrarContrasena,
                    estadoErrorMensaje = estadoErrorMensaje,
                    navController = navController
                )
            }
        } else {
            // Diseño en modo horizontal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TitleSection()
                    IconSection("Log in")
                }
                FormSection(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    estadoEmail = estadoEmail,
                    estadoContrasena = estadoContrasena,
                    mostrarContrasena = mostrarContrasena,
                    estadoErrorMensaje = estadoErrorMensaje,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun TitleSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MetaFit",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                color = Color.White
            )
        )
        Text(
            text = "Transforma tu vida",
            style = TextStyle(
                fontSize = 18.sp,
                color = Color.Gray
            )
        )
    }
}

@Composable
fun IconSection(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.5f) // Ocupa el 50% del ancho disponible
            .aspectRatio(1f) // Mantiene la proporción cuadrada
            .shadow(8.dp, CircleShape)
            .background(Color(0xFF2D2D2D), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Icono",
            modifier = Modifier.fillMaxSize(0.8f), // Escala el ícono proporcionalmente
            tint = Color.White
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = titulo,
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    )
}

@Composable
fun FormSection(
    modifier: Modifier = Modifier,
    estadoEmail: MutableState<String>,
    estadoContrasena: MutableState<String>,
    mostrarContrasena: MutableState<Boolean>,
    estadoErrorMensaje: MutableState<String?>,
    navController: NavHostController
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextFieldCorreo(
            label = "Email",
            estadoTexto = estadoEmail
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldContrasena(
            label = "Password",
            estadoTexto = estadoContrasena,
            mostrarContrasena = mostrarContrasena
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // Obtener los valores de los campos de email y contraseña
                val email = estadoEmail.value.trim() // Eliminar espacios adicionales
                val contrasena = estadoContrasena.value.trim()

                // Verificar que los campos no estén vacíos
                if (email.isNotEmpty() && contrasena.isNotEmpty()) {
                    val autenticador = FirebaseAuth.getInstance() // Obtener instancia de Firebase Authentication

                    // Intentar iniciar sesión con email y contraseña
                    autenticador.signInWithEmailAndPassword(email, contrasena)
                        .addOnCompleteListener { tarea -> // Escucha cuando se complete la tarea de inicio de sesión
                            if (tarea.isSuccessful) {
                                // Si el inicio de sesión es exitoso, no hay error y navega a la siguiente pantalla
                                estadoErrorMensaje.value = null
                                navController.navigate("SplashScreen") // Navega al SplashScreen
                            } else {
                                // Si falla, muestra un mensaje de error
                                estadoErrorMensaje.value = "Error: correo o contraseña incorrecto"
                            }
                        }
                } else {
                    // Si los campos están vacíos, mostrar un mensaje de advertencia
                    estadoErrorMensaje.value = "Por favor, completa todos los campos."
                }
            },
            modifier = Modifier
                .fillMaxWidth() // El botón ocupa todo el ancho disponible
                .shadow(4.dp, CircleShape), // Sombra con forma circular
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)) // Color de fondo del botón
        ) {
            // Texto dentro del botón
            Text(text = "Iniciar Sesión", color = Color.White) // Texto en blanco
        }
        Spacer(modifier = Modifier.height(16.dp))
        estadoErrorMensaje.value?.let { mensajeError ->
            Text(
                text = mensajeError,
                color = Color.Red,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Forgot password?",
            color = Color.Gray,
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Row  {
            Text(
                text = "¿No tienes cuenta?",
                color = Color.White, // Azul claro verdoso (DarkTurquoise)
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )

            Text(
                text = " Regístrate",
                color = Color(0xFF00CED1), // Azul claro verdoso (DarkTurquoise)
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable {
                    navController.navigate("Registro") // Cambia a la pantalla de registro
                }

            )
        }
    }
}

@Composable
fun TextFieldCorreo(label: String, estadoTexto: MutableState<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(color = Color.Gray, fontSize = 14.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = estadoTexto.value,
            onValueChange = { estadoTexto.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = TextStyle(color = Color.White),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                cursorColor = Color.White,
                focusedIndicatorColor = Color(0xFF6A1B9A),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}

@Composable
fun TextFieldContrasena(label: String, estadoTexto: MutableState<String>, mostrarContrasena: MutableState<Boolean>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(color = Color.Gray, fontSize = 14.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = estadoTexto.value,
            onValueChange = { estadoTexto.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = TextStyle(color = Color.White),
            // Configura si la contraseña se muestra o está oculta.

            visualTransformation = if (mostrarContrasena.value) VisualTransformation.None else PasswordVisualTransformation(),
            //mostrar un ícono que alterna entre mostrar y ocultar la contraseña:
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena.value = !mostrarContrasena.value }) {
                    Icon(
                        imageVector = if (mostrarContrasena.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Mostrar/Ocultar Contraseña",
                        tint = Color.Gray
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                cursorColor = Color.White,
                focusedIndicatorColor = Color(0xFF6A1B9A),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}
