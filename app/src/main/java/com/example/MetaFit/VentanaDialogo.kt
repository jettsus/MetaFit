@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.MetaFit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VentanaDialogo(
    onCancel: () -> Unit, // Callback para cancelar
    onSave: (String) -> Unit, // Callback para guardar
    contexto: String = "General" // Contexto con valor por defecto
) {
    val nuevoTexto = rememberSaveable { mutableStateOf("") }

    // título y placeholder dinamico según el contexto
    val titulo = when (contexto) {
        "Entrenamientos" -> "Crea un nuevo entreno"
        "DiasEntrenamiento" -> "Crea un nuevo día de entreno"
        "Ejercicios" -> "Crea un nuevo entrenamiento"
        else -> "Nuevo elemento"
    }
    //Texto explicativo en textfield
    val placeholder = when (contexto) {
        "Entrenamientos" -> "Ejemplo: Entreno primeros 4 meses del año"
        "DiasEntrenamiento" -> "Ejemplo: Empuje"
        "Ejercicios" -> "Ejemplo: Press banca"
        else -> "Escribe aquí"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título dinámico según el contexto
                Text(
                    text = titulo,
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto para el nuevo elemento
                TextField(
                    value = nuevoTexto.value,
                    onValueChange = { nuevoTexto.value = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color(0xFF6A1B9A),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Botón para cancelar
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancelar")
                    }

                    // Botón para guardar
                    Button(
                        onClick = {
                            if (nuevoTexto.value.isNotBlank()) {
                                onSave(nuevoTexto.value)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
