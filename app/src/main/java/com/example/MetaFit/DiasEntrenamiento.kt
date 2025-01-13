package com.example.MetaFit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun DiasEntrenamiento(
    nombreBloque: String, // Nombre del bloque de entrenamiento actual

    // Controlador para manejar la navegación entre pantallas
    navController: NavHostController,

    // Instancia del ViewModel para manejar los datos de entrenamiento
    viewModel: EntrenamientosViewModel = viewModel()
    ){

    val mostrarDialogo = rememberSaveable { mutableStateOf(false) }
    val bloqueSeleccionado = rememberSaveable { mutableStateOf<String?>(null) } // Guarda el bloque seleccionado
    val menuAbiertoParaBloque = rememberSaveable { mutableStateOf<String?>(null) } // Estado del menú para un bloque específico

    // Estado para la lista de bloques cargada desde el ViewModel
    val bloques = remember { mutableStateListOf<String>() }

    // Estado para el Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para la coroutine scope
    //para poder hacer tareas en segundo plano, como mostrar un
    //mensaje o cargar datos, sin que la app se bloquee
    val coroutineScope = rememberCoroutineScope()

    // Efecto lanzado una vez para cargar los bloques desde el ViewModel
    LaunchedEffect(Unit) {
        try {
            bloques.clear()
            bloques.addAll(viewModel.getSubBlocks(nombreBloque))
        } catch (e: Exception) {
            e.printStackTrace()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error al cargar los días de entrenamiento.")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra superior con título y botón "+"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }

                Text(
                    text = nombreBloque,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                IconButton(onClick = { mostrarDialogo.value = true }) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.mas),
                        contentDescription = "Añadir",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar bloques
            bloques.forEach { bloque ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("Ejercicios/$bloque")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bloque,
                            style = TextStyle(fontSize = 16.sp, color = Color.White)
                        )

                        Box {
                            IconButton(onClick = { menuAbiertoParaBloque.value = bloque }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.editar2),
                                    contentDescription = "Editar",
                                    tint = Color.White
                                )
                            }

                            //Una vez pinchado se abre el menu el para ese bloque
                            DropdownMenu(
                                expanded = menuAbiertoParaBloque.value == bloque, // Verifica si el menú está abierto para este bloque
                                onDismissRequest = { menuAbiertoParaBloque.value = null } // Cierra el menú al hacer clic fuera
                            ) {
                                // Opción para cambiar el nombre del bloque
                                DropdownMenuItem(
                                    onClick = {
                                        menuAbiertoParaBloque.value = null // Cierra el menú
                                        bloqueSeleccionado.value = bloque // Selecciona el bloque actual para editar
                                    },
                                    text = { Text("Cambiar nombre") }
                                )
                                // Opción para eliminar el bloque
                                DropdownMenuItem(
                                    onClick = {
                                        menuAbiertoParaBloque.value = null // Cierra el menú
                                        viewModel.removeSubBlock(nombreBloque, bloque) // Elimina el bloque del ViewModel
                                        bloques.remove(bloque) // Elimina el bloque de la lista local
                                    },
                                    text = { Text("Eliminar", color = Color.Red) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para agregar nuevo bloque
        if (mostrarDialogo.value) {
            VentanaDialogo(
                onCancel = { mostrarDialogo.value = false },
                onSave = { nuevoBloque ->
                    if (nuevoBloque.isNotBlank()) {
                        if (bloques.contains(nuevoBloque)) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("El bloque '$nuevoBloque' ya existe.")
                            }
                        } else {
                            viewModel.addSubBlock(nombreBloque, nuevoBloque) // Agregar bloque al ViewModel
                            bloques.add(nuevoBloque) // Agregar bloque a la lista local
                        }
                    }
                    mostrarDialogo.value = false
                },
                contexto = "DiasEntrenamiento"
            )
        }

        // Diálogo para cambiar nombre
        // Si hay un bloque seleccionado, muestra el diálogo para cambiar el nombre
        bloqueSeleccionado.value?.let { bloque ->
            VentanaDialogo(
                onCancel = { bloqueSeleccionado.value = null }, // Cierra el diálogo al cancelar
                onSave = { nuevoNombre -> // Acción al guardar el nuevo nombre
                    if (nuevoNombre.isNotBlank()) { // Verifica que el nuevo nombre no esté vacío
                        if (bloques.contains(nuevoNombre)) { // Verifica si el nuevo nombre ya existe
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("El bloque '$nuevoNombre' ya existe.") // Muestra un mensaje de error si el nombre ya existe
                            }
                        } else {
                            viewModel.updateSubBlock(nombreBloque, bloque, nuevoNombre) // Cambia el nombre del bloque en el ViewModel
                            val index = bloques.indexOf(bloque)
                            if (index != -1) {
                                bloques[index] = nuevoNombre // Actualiza el nombre del bloque en la lista local
                            }
                        }
                    }
                    bloqueSeleccionado.value = null // Limpia el bloque seleccionado y cierra el diálogo
                },
                contexto = "DiasEntrenamiento"
            )
        }



        // Muestra el Snackbar en la parte inferior de la pantalla
        SnackbarHost(
            hostState = snackbarHostState, // Controla el estado del Snackbar (mostrar u ocultar mensajes)
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
