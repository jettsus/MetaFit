package com.example.MetaFit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.*
import androidx.navigation.NavHostController

// Función para formatear un valor a dos decimales
fun String.formatearADosDecimales(): String {
    val valor = this.toDoubleOrNull()
    return if (valor != null) {
        String.format("%.2f", valor)
    } else {
        this // Devuelve el texto original si no es válido
    }
}

@Composable
fun IMCScreen(navController: NavHostController) {
    // Estados para peso, altura e IMC con persistencia al girar la pantalla
    var peso by rememberSaveable { mutableStateOf("") } // Guarda el peso ingresado
    var altura by rememberSaveable { mutableStateOf("") } // Guarda la altura ingresada
    var imc by rememberSaveable { mutableStateOf<Double?>(null) } // Guarda el resultado del IMC
    var mensajeIMC by rememberSaveable { mutableStateOf("") } // Guarda el mensaje de la categoría del IMC

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de retroceso
            Button(
                onClick = { navController.popBackStack() }, // Vuelve a la pantalla anterior
                modifier = Modifier.align(Alignment.Start),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4f378a))
            ) {
                Text(
                    text = "Atrás",
                    style = TextStyle(fontSize = 14.sp, color = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título de la pantalla
            Text(
                text = "Calculadora de IMC",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4f378a)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para ingresar el peso
            val pesoFocusRequester = remember { FocusRequester() }
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it }, // Guarda el valor ingresado sin formato
                label = { Text("Peso (kg)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(pesoFocusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            peso = peso.formatearADosDecimales() // Aplica el formato al perder el foco
                        }
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para ingresar la altura
            val alturaFocusRequester = remember { FocusRequester() }
            OutlinedTextField(
                value = altura,
                onValueChange = { altura = it }, // Guarda el valor ingresado sin formato
                label = { Text("Altura (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(alturaFocusRequester)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            altura = altura.formatearADosDecimales() // Aplica el formato al perder el foco
                        }
                    }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón para calcular el IMC
            Button(
                onClick = {
                    val pesoDouble = peso.toDoubleOrNull() // Convierte el peso a Double
                    val alturaDouble = altura.toDoubleOrNull() // Convierte la altura a Double

                    if (pesoDouble != null && alturaDouble != null && pesoDouble > 0 && alturaDouble > 0) {
                        // Calcula el IMC y muestra la categoría correspondiente
                        imc = pesoDouble / (alturaDouble * alturaDouble)
                        mensajeIMC = when {
                            imc!! < 18.5 -> "Bajo peso"
                            imc!! in 18.5..24.9 -> "Peso normal"
                            imc!! in 25.0..29.9 -> "Sobrepeso"
                            else -> "Obesidad"
                        }
                    } else {
                        // Muestra un mensaje de error si los valores ingresados no son válidos
                        imc = null
                        mensajeIMC = "Por favor, ingresa valores válidos."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7FFF00)), // Botón verde lima
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "Calcular",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Muestra el resultado del IMC si se ha calculado
            imc?.let {
                Text(
                    text = "Tu IMC es: ${"%.2f".format(it)}",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4f378a)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeIMC,
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Selección de imagen según el IMC calculado
                val imageRes = when {
                    imc!! < 18.5 -> R.drawable.bajo_peso
                    imc!! in 18.5..24.9 -> R.drawable.peso_normal
                    imc!! in 25.0..29.9 -> R.drawable.sobrepeso
                    else -> R.drawable.obesidad
                }

                // Imagen de la categoría del IMC
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Categoría IMC",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Explicación de las categorías de IMC
            Text(
                text = "Categorías de IMC:\n" +
                        "\u2022 Bajo peso: Menos de 18.5\n" +
                        "\u2022 Peso normal: 18.5 - 24.9\n" +
                        "\u2022 Sobrepeso: 25.0 - 29.9\n" +
                        "\u2022 Obesidad: 30.0 o más",
                style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
