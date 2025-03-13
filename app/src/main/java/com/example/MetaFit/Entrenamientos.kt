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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun Entrenamientos(

    // Controlador para manejar la navegación entre pantallas
    navController: NavHostController,

    // Instancia del ViewModel para manejar los datos de entrenamiento
    viewModel: EntrenamientosViewModel = viewModel() // Obtener el ViewModel
) {

    //Estado para mostrar u ocultar la ventana dialogo y conservarlo
    val mostrarDialogo = rememberSaveable { mutableStateOf(false) }
    val bloqueSeleccionado = rememberSaveable { mutableStateOf<String?>(null) } // Guarda el bloque seleccionado
    val menuAbiertoParaBloque = rememberSaveable { mutableStateOf<String?>(null) } // Estado del menú para un bloque específico

    // Estado para la lista de bloques cargada desde el ViewModel
    val bloques = remember { mutableStateListOf<String>() }

    // Estado para el Snackbar inferior
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para la coroutine scope
    //para poder hacer tareas en segundo plano, como mostrar un
    //mensaje o cargar datos, sin que la app se bloquee
    val coroutineScope = rememberCoroutineScope()

    // Efecto lanzado una vez para cargar los bloques desde el ViewModel
    LaunchedEffect(Unit) {
        bloques.clear()
        bloques.addAll(viewModel.bloques)
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
                // Scroll en la pantalla
                .verticalScroll(rememberScrollState())
        ) {
            // Barra superior con título y botón "+"
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Entrenamientos",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                )
                //Muestra el icono
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
                            //Navegacion a dias entrenamiento en función del bloque seleccionado
                            navController.navigate("DiasEntrenamiento/$bloque")
                        },
                    //Color de cada bloque
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
                                        viewModel.removeBlock(bloque) // Eliminar bloque
                                        bloques.remove(bloque) // Eliminar bloque de la lista local
                                    },
                                    text = { Text("Eliminar", color = Color.Red) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Si el usuario ha hecho clic en el botón para agregar un nuevo bloque,
        // se muestra el diálogo emergente (VentanaDialogo)
        if (mostrarDialogo.value) {
            VentanaDialogo(
                onCancel = { mostrarDialogo.value = false }, // Si cancela, cierra el diálogo
                onSave = { nuevoBloque -> // Si guarda un bloque nuevo
                    if (nuevoBloque.isNotBlank()) { // Verifico que el nombre no esté vacío
                        if (bloques.contains(nuevoBloque)) { // Si el bloque ya existe, muestro un mensaje de error
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("El bloque '$nuevoBloque' ya existe.")
                            }
                        } else {
                            // Si el nombre es válido y no existe, lo agrego al ViewModel y a la lista local
                            viewModel.addBlock(nuevoBloque)
                            bloques.add(nuevoBloque)
                        }
                    }
                    mostrarDialogo.value = false // Cierro el diálogo después de guardar
                },
                contexto = "Entrenamientos"
            )
        }

        // Si el usuario ha seleccionado un bloque para cambiar el nombre, muestro el diálogo
        bloqueSeleccionado.value?.let { bloque ->
            VentanaDialogo(
                onCancel = { bloqueSeleccionado.value = null }, // Cierra el dialogo si cancela
                onSave = { nuevoNombre -> // Si guarda un nuevo nombre para el bloque
                    if (nuevoNombre.isNotBlank()) { // Verifico que no esté vacío
                        if (bloques.contains(nuevoNombre)) { // Si el nuevo nombre ya existe, muestro un mensaje de error
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("El bloque '$nuevoNombre' ya existe.")
                            }
                        } else {
                            // Si el nuevo nombre es valido y no existe, actualizo el bloque en el ViewModel y en la lista local
                            viewModel.updateBlock(bloque, nuevoNombre)
                            val index = bloques.indexOf(bloque)
                            if (index != -1) {
                                bloques[index] = nuevoNombre // Actualizo el nombre en la lista local
                            }
                        }
                    }
                    bloqueSeleccionado.value = null // Limpio el bloque seleccionado y cierro el diálogo
                },
                contexto = "Entrenamientos"
            )
        }

        // Componente Snackbar que se muestra en la parte inferior de la pantalla
        SnackbarHost(
            hostState = snackbarHostState, // Controla los mensajes que se muestran
            modifier = Modifier.align(Alignment.BottomCenter) // Alineado en la parte inferior
        )

    }
}
