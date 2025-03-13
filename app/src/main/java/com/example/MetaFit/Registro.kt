@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.MetaFit

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Registro(navController: NavHostController) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isPortrait) {
            // Diseño en modo vertical
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleSection()
                Spacer(modifier = Modifier.height(16.dp))
                IconSection("Sign in")
                Spacer(modifier = Modifier.height(32.dp))
                RegistroContent(navController)             }
        } else {
            // Diseño en modo horizontal
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TitleSection()
                    Spacer(modifier = Modifier.height(16.dp))
                    IconSection("Sign in")
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    RegistroContent(navController)
                }
            }
        }
    }
}

@Composable
fun RegistroTextField(
    label: String,
    estadoTexto: MutableState<String>,
    esContrasena: Boolean = false,
    mostrarContrasena: MutableState<Boolean>? = null
) {
    OutlinedTextField(
        value = estadoTexto.value, // El valor actual del campo de texto
        onValueChange = { estadoTexto.value = it }, // Función que se llama cada vez que cambia el valor del campo de texto
        label = {
            Text(
                text = label, // El texto que aparecerá como la etiqueta del campo
                style = TextStyle(color = Color.Gray, fontSize = 14.sp) // Estilo de la etiqueta (color y tamaño de texto)
            )
        },
        visualTransformation = if (esContrasena && mostrarContrasena != null && !mostrarContrasena.value) {
            // Si es un campo de contraseña y la opción de mostrar contrasena es falsa, aplicamos la transformación para ocultar la contraseña
            PasswordVisualTransformation() // Transforma el texto para que se muestre como asteriscos (oculta la contraseña)
        } else {
            VisualTransformation.None // Si no es un campo de contraseña o el estado de mostrar contraseña es verdadero, no se aplica ninguna transformación
        },
        //Agregar icono al final
        trailingIcon = if (esContrasena && mostrarContrasena != null) {
            // Si el campo es para una contraseña y tenemos el estado para mostrar la contraseña, agregamos un ícono para alternar la visibilidad
            {
                IconButton(onClick = { mostrarContrasena.value = !mostrarContrasena.value }) {
                    Icon(
                        imageVector = if (mostrarContrasena.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        // Muestra un ícono de "ojo" si la contraseña está visible, o un ícono de "ojo cerrado" si está oculta
                        contentDescription = "Mostrar/Ocultar Contraseña",
                        tint = Color.Gray // Color del ícono
                    )
                }
            }
        } else null, // Si no es un campo de contraseña, no se muestra el ícono
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedLabelColor = Color.Gray,
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color.White,
            focusedBorderColor = Color(0xFF6A1B9A),
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun RegistroContent(navController: NavHostController) {
    // Estados para los campos
    val estadoEmail = rememberSaveable { mutableStateOf("") }
    val estadoContrasena = rememberSaveable { mutableStateOf("") }
    val estadoConfirmarContrasena = rememberSaveable { mutableStateOf("") }
    val mostrarContrasena = rememberSaveable { mutableStateOf(false) }
    val mostrarConfirmarContrasena = rememberSaveable { mutableStateOf(false) }
    val estadoErrorMensaje = rememberSaveable { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Campo de email
        RegistroTextField(label = "Email", estadoTexto = estadoEmail)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña
        RegistroTextField(
            label = "Contraseña",
            estadoTexto = estadoContrasena,
            esContrasena = true,
            mostrarContrasena = mostrarContrasena
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de confirmar contraseña
        RegistroTextField(
            label = "Confirmar contraseña",
            estadoTexto = estadoConfirmarContrasena,
            esContrasena = true,
            mostrarContrasena = mostrarConfirmarContrasena
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de error
        estadoErrorMensaje.value?.let { mensajeError ->
            Text(
                text = mensajeError,
                color = Color.Red,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de registro
        Button(
            onClick = {
                val email = estadoEmail.value.trim()
                val contrasena = estadoContrasena.value.trim()
                val confirmarContrasena = estadoConfirmarContrasena.value.trim()

                // Validaciones
                if (email.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
                    estadoErrorMensaje.value = "Por favor, completa todos los campos."
                    //Confirmación email siga parametros
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    estadoErrorMensaje.value = "El correo no es válido."
                } else if (contrasena.length < 6) {
                    estadoErrorMensaje.value = "La contraseña debe tener al menos 6 caracteres."
                } else if (contrasena != confirmarContrasena) {
                    estadoErrorMensaje.value = "Las contraseñas no coinciden."
                } else {
                    estadoErrorMensaje.value = null

                    // Firebase autenticacion
                    val auth = FirebaseAuth.getInstance()
                    //metodo para crear usuario
                    auth.createUserWithEmailAndPassword(email, contrasena)
                        // Se añade un escuchador para saber cuándo la tarea de registro se completa.
                        .addOnCompleteListener { tarea ->
                            // Se verifica si la tarea fue exitosa
                            if (tarea.isSuccessful) {
                                // Si el registro es exitoso, redirige al usuario a la pantalla de login
                                navController.navigate("login") {
                                    // Se elimina la pantalla de registro del stack de navegación
                                    popUpTo("Registro") { inclusive = true }
                                }
                            } else {
                                // Si la tarea falla, muestra un mensaje de error genérico
                                estadoErrorMensaje.value = "Error al registrar: El correo ya está registrado o no es válido."
                            }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
        ) {
            Text(text = "Registrarse", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(
                text = "¿Ya tienes cuenta?",
                color = Color.White,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = " Inicia sesión",
                color = Color(0xFF00CED1),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }
    }
}




