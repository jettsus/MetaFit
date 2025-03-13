package com.example.MetaFit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavHostController

@Composable
fun Series(
    nombreBloque: String,               // Nombre del bloque de entrenamiento
    nombreEjercicio: String,            // Ejercicio en el que estamos
    navController: NavHostController,   // Controlador para la navegación entre pantallas
    viewModel: EntrenamientosViewModel = androidx.lifecycle.viewmodel.compose.viewModel() // ViewModel que gestiona los datos
) {
    val series = viewModel.seriesOrdenadas // Obtenemos la lista de series desde el ViewModel
    var errorMessage by remember { mutableStateOf<String?>(null) } // Mensaje de error, por si las cosas no salen bien

    // Aquí guardamos los valores que el usuario ingresa para peso y repeticiones
    val nuevoPeso = rememberSaveable { mutableStateOf("") }
    val nuevasRepeticiones = rememberSaveable { mutableStateOf("") }

    // Calculamos el tonelaje total (peso total levantado) y el E1RM (estimación de 1 repetición máxima)
    val tonelajeTotal = series.sumOf { it.first * it.second }
    val e1rmMax = series.maxOfOrNull { it.first * (1 + it.second / 30.0) }?.toInt() ?: 0

    // Variables para la edición de una serie existente
    val pesoEditado = rememberSaveable { mutableStateOf("") }
    val repeticionesEditadas = rememberSaveable { mutableStateOf("") }
    val serieEditadaIndex = rememberSaveable { mutableStateOf<Int?>(null) }

    // Esto es para abrir el menú de edición o eliminación de una serie específica
    var menuAbiertoParaIndice by remember { mutableStateOf<Int?>(null) }

    // Al iniciar, cargamos las series del ejercicio desde el ViewModel
    LaunchedEffect(nombreEjercicio) {
        try {
            viewModel.getSeries(nombreEjercicio)
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error al cargar las series del ejercicio." // Si algo falla, mostramos este mensaje
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Scroll para que se pueda ver todo el contenido
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado con el nombre del bloque y botón de retroceso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "< $nombreBloque", // Botón de retroceso textual
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.clickable { navController.popBackStack() } // Al hacer clic, volvemos a la pantalla anterior
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostramos el nombre del ejercicio en el que estamos
            Text(
                text = "Ejercicio: $nombreEjercicio",
                style = TextStyle(fontSize = 18.sp, color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Si hay un error, lo mostramos aquí
            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = TextStyle(color = Color.Red, fontSize = 16.sp),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Aquí mostramos la lista de series que ha agregado el usuario
            // Si la lista de series no está vacía, mostramos las series en una tarjeta (Card)
            if (series.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(), // La tarjeta ocupa todo el ancho disponible
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Color de fondo oscuro
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Recorremos las series y mostramos cada una en un Row
                        series.forEachIndexed { index, (peso, repeticiones) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth() // Cada fila ocupa todo el ancho disponible
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween // Separación uniforme entre elementos
                            ) {
                                // Mostramos el peso y las repeticiones de la serie
                                Text(
                                    text = "$peso kg x $repeticiones reps",
                                    style = TextStyle(fontSize = 16.sp, color = Color.White)
                                )
                                // Icono de opciones con menú desplegable
                                Box {
                                    IconButton(onClick = {
                                        menuAbiertoParaIndice =
                                            index // Al hacer clic, abre el menú para esta serie
                                    }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.editar2),
                                            contentDescription = "Opciones",
                                            tint = Color.White
                                        )
                                    }
                                    // Menú desplegable con opciones para editar o eliminar la serie
                                    DropdownMenu(
                                        expanded = menuAbiertoParaIndice == index, // Verifica si el menú debe estar abierto para esta serie
                                        onDismissRequest = {
                                            menuAbiertoParaIndice = null
                                        } // Cierra el menú al hacer clic fuera
                                    ) {
                                        // Opción para editar la serie
                                        DropdownMenuItem(
                                            onClick = {
                                                // Cargamos los valores de la serie seleccionada para editarlos
                                                serieEditadaIndex.value = index
                                                pesoEditado.value = peso.toString()
                                                repeticionesEditadas.value = repeticiones.toString()
                                                menuAbiertoParaIndice =
                                                    null // Cerramos el menú tras seleccionar
                                            },
                                            text = { Text("Editar") }
                                        )
                                        // Opción para eliminar la serie
                                        DropdownMenuItem(
                                            onClick = {
                                                viewModel.removeSerie(
                                                    nombreEjercicio,
                                                    index
                                                ) // Eliminamos la serie del ViewModel
                                                menuAbiertoParaIndice =
                                                    null // Cerramos el menú tras eliminar
                                            },
                                            text = { Text("Eliminar", color = Color.Red) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (errorMessage == null) { // Si no hay series y tampoco error, mostramos un mensaje indicando que no hay datos
                Text(
                    text = "No hay series agregadas",
                    style = TextStyle(color = Color.Gray, fontSize = 16.sp),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre las secciones

// Si hay series, mostramos un resumen del tonelaje total y el E1RM estimado
            if (series.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(), // La tarjeta ocupa todo el ancho disponible
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)) // Color de fondo oscuro
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resumen",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Tonelaje total levantado en todas las series
                        Text(
                            text = "Tonelaje total: $tonelajeTotal kg",
                            style = TextStyle(fontSize = 16.sp, color = Color.White)
                        )
                        // E1RM estimado: estimación de una repetición máxima
                        Text(
                            text = "E1RM estimado: $e1rmMax kg",
                            style = TextStyle(fontSize = 16.sp, color = Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre las secciones

// Formulario para agregar una nueva serie
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Campo de texto para añadir el peso de la serie
                        OutlinedTextField(
                            value = nuevoPeso.value,
                            onValueChange = { nuevoPeso.value = it },
                            label = { Text("Peso (kg)") },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        // Campo de texto para añadir las repeticiones de la serie
                        OutlinedTextField(
                            value = nuevasRepeticiones.value,
                            // Actualiza el valor cuando el usuario escribe
                            onValueChange = { nuevasRepeticiones.value = it },
                            label = { Text("Repeticiones") },
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para agregar la nueva serie
                    Button(
                        onClick = {
                            //Convierto en numero entero el peso y reps
                            val pesoInt = nuevoPeso.value.toIntOrNull()
                            val repsInt = nuevasRepeticiones.value.toIntOrNull()

                            if (pesoInt != null && repsInt != null && pesoInt > 0 && repsInt > 0) {
                                // Si el peso y las repeticiones son válidos, agregamos la serie al ViewModel y limpiamos los campos
                                viewModel.addSerie(nombreEjercicio, Pair(pesoInt, repsInt))
                                nuevoPeso.value = ""
                                nuevasRepeticiones.value = ""
                                errorMessage = null
                            } else {
                                // Si los valores son inválidos, mostramos un mensaje de error
                                nuevoPeso.value = ""
                                nuevasRepeticiones.value = ""
                                errorMessage = "Peso debe ser mayor a menos 1 y repeticiones deben ser mayor a 0."
                            }
                        },
                        enabled = nuevoPeso.value.isNotEmpty() && nuevasRepeticiones.value.isNotEmpty(), // Solo habilita el botón si los campos no están vacíos
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Añadir", color = Color.White)
                    }
                }
            }

// Diálogo para editar una serie existente
            if (serieEditadaIndex.value != null) {
                AlertDialog(
                    onDismissRequest = {
                        serieEditadaIndex.value = null
                    }, // Cierra el diálogo si se hace clic fuera de él
                    title = { Text("Editar Serie") }, // Título del diálogo
                    text = {
                        Column {
                            // Campo de texto para editar el peso
                            OutlinedTextField(
                                value = pesoEditado.value,
                                onValueChange = { pesoEditado.value = it },
                                label = { Text("Peso (kg)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Campo de texto para editar las repeticiones
                            OutlinedTextField(
                                value = repeticionesEditadas.value,
                                onValueChange = { repeticionesEditadas.value = it },
                                label = { Text("Repeticiones") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val peso = pesoEditado.value.toIntOrNull()
                            val reps = repeticionesEditadas.value.toIntOrNull()
                            val index = serieEditadaIndex.value// para la posicion en la lista
                            if (peso != null && reps != null && peso > -1 && reps > 0 && index != null) {
                                // Si los valores estan bien, actualizamos la serie en el ViewModel y cerramos el dialogo
                                viewModel.updateSerie(nombreEjercicio, index, Pair(peso, reps))
                                serieEditadaIndex.value = null
                            } else {
                                errorMessage = "Peso y repeticiones deben ser mayores a 0."
                            }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { serieEditadaIndex.value = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}