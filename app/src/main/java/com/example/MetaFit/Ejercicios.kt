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

@Composable
fun Ejercicios(
    nombreBloque: String,
    navController: NavHostController,
    viewModel: EntrenamientosViewModel = viewModel()
) {
    val mostrarDialogo = rememberSaveable { mutableStateOf(false) }

    val bloquesDiaEntrenamiento = remember { mutableStateListOf<String>() }
    val bloqueDiaEntrenamientoSeleccionado = rememberSaveable { mutableStateOf<String?>(null) }
    val menuAbiertoParaBloqueDiaEntrenamiento = rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Inicializar bloques de día de entrenamiento con manejo de errores
    LaunchedEffect(Unit) {
        try {
            bloquesDiaEntrenamiento.clear()
            bloquesDiaEntrenamiento.addAll(viewModel.getSubBlocks(nombreBloque))
        } catch (e: Exception) {
            e.printStackTrace() // Imprimir el error en el logcat para depuración
            snackbarMessage = "Error al cargar los días de entrenamiento."
        }
    }

    // Mostrar Snackbar cuando haya un mensaje
    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null // Limpiar el mensaje después de mostrarlo
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
            // Barra superior
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

            // Lista de bloques de día de entrenamiento
            bloquesDiaEntrenamiento.forEach { bloqueDiaEntrenamiento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("Series/$nombreBloque/$bloqueDiaEntrenamiento/$bloqueDiaEntrenamiento")
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
                            text = bloqueDiaEntrenamiento,
                            style = TextStyle(fontSize = 16.sp, color = Color.White)
                        )

                        Box {
                            IconButton(onClick = { menuAbiertoParaBloqueDiaEntrenamiento.value = bloqueDiaEntrenamiento }) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.editar2),
                                    contentDescription = "Editar",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = menuAbiertoParaBloqueDiaEntrenamiento.value == bloqueDiaEntrenamiento,
                                onDismissRequest = { menuAbiertoParaBloqueDiaEntrenamiento.value = null }
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        bloqueDiaEntrenamientoSeleccionado.value = bloqueDiaEntrenamiento
                                        mostrarDialogo.value = true
                                        menuAbiertoParaBloqueDiaEntrenamiento.value = null
                                    },
                                    text = { Text("Cambiar nombre") }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.removeSubBlock(nombreBloque, bloqueDiaEntrenamiento)
                                        bloquesDiaEntrenamiento.remove(bloqueDiaEntrenamiento)
                                        menuAbiertoParaBloqueDiaEntrenamiento.value = null
                                    },
                                    text = { Text("Eliminar", color = Color.Red) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para agregar o editar bloques de día de entrenamiento
        if (mostrarDialogo.value || bloqueDiaEntrenamientoSeleccionado.value != null) {
            VentanaDialogo(
                onCancel = {
                    mostrarDialogo.value = false
                    bloqueDiaEntrenamientoSeleccionado.value = null
                },
                onSave = { nuevoBloqueDiaEntrenamiento ->
                    if (nuevoBloqueDiaEntrenamiento.isNotBlank()) {
                        if (bloquesDiaEntrenamiento.contains(nuevoBloqueDiaEntrenamiento)) {
                            snackbarMessage = "El día de entrenamiento ya existe."
                        } else {
                            if (bloqueDiaEntrenamientoSeleccionado.value != null) {
                                val oldBloqueDiaEntrenamiento = bloqueDiaEntrenamientoSeleccionado.value!!
                                viewModel.updateSubBlock(
                                    nombreBloque,
                                    oldBloqueDiaEntrenamiento,
                                    nuevoBloqueDiaEntrenamiento
                                )
                                val index = bloquesDiaEntrenamiento.indexOf(oldBloqueDiaEntrenamiento)
                                if (index != -1) {
                                    bloquesDiaEntrenamiento[index] = nuevoBloqueDiaEntrenamiento
                                }
                            } else {
                                bloquesDiaEntrenamiento.add(nuevoBloqueDiaEntrenamiento)
                                viewModel.addSubBlock(nombreBloque, nuevoBloqueDiaEntrenamiento)
                            }
                        }
                    }
                    mostrarDialogo.value = false
                    bloqueDiaEntrenamientoSeleccionado.value = null
                },
                contexto = "Ejercicios"
            )
        }

        // Mostrar Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
