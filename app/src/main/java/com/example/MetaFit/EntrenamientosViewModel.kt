package com.example.MetaFit

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.google.common.reflect.TypeToken
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

/**
 * ViewModel para manejar los bloques de entrenamiento y los sub-bloques (días y ejercicios).
 * Almacena los datos usando SharedPreferences para persistencia, lo que permite que la información
 * persista entre sesiones de la aplicación.
 */
class EntrenamientosViewModel(application: Application) : AndroidViewModel(application) {

    // Contexto de la aplicación ViewModel interactúe con el
    // entorno de la aplicación, como guardar o cargar datos desde SharedPreferences

    private val contexto = application.applicationContext

    // Creamos una conexión con Firebase para saber qué usuario está usando la app
    private val auth = FirebaseAuth.getInstance()

    // Obtiene el ID único del usuario que inició sesión, si hay uno activo
    private val userId: String? get() = auth.currentUser?.uid

    val bloques = mutableStateListOf<String>()

    // Al iniciar el ViewModel, si hay un usuario autenticado,
    // cargo sus bloques desde SharedPreferences y los añado a la lista bloques
    init {
        userId?.let {
            bloques.addAll(cargarBloques(it))
        }
    }

    // Método para guardar bloques usando el UID del usuario
    private fun guardarBloques() {
        userId?.let { uid ->
            val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)
            val editor = preferencias.edit()
            val json = Gson().toJson(bloques)
            editor.putString("blocks_key_$uid", json)  // Usar UID como parte de la clave
            editor.apply()
        }
    }

    // Este método carga los bloques guardados para el usuario actual usando su UID.
    // si encuentra los bloques guardados en formato JSON, los convierte en una lista y los devuelve.
    // Si no hay bloques guardados, devuelve una lista vacía.

    // Método que carga los bloques de entrenamiento guardados para el usuario actual, usando su UID como clave
    private fun cargarBloques(uid: String): MutableList<String> {
        // Accedemos a las preferencias compartidas donde se guardaron los bloques
        val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)

        // Recuperamos los bloques guardados como una cadena en formato JSON, usando la clave específica del usuario
        val json = preferencias.getString("blocks_key_$uid", null)

        // Si el JSON no está vacío o nulo, lo convertimos a una lista de bloques usando Gson
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<String>>() {}.type  // Definimos el tipo de dato que esperamos (lista de cadenas)
            Gson().fromJson(json, type) ?: mutableListOf()  // Convertimos el JSON a lista; si falla, devolvemos una lista vacía
        } else {
            mutableListOf()  // Si no había datos guardados, devolvemos una lista vacía
        }
    }

    // Método para agregar un nuevo bloque de entrenamiento a la lista
    fun addBlock(bloque: String) {
        bloques.add(bloque) // Añadir el bloque a la lista de bloques
        guardarBloques()  // Guardar la lista actualizada en SharedPreferences
    }

    // Método para eliminar un bloque de entrenamiento de la lista
    fun removeBlock(bloque: String) {
        // Obtenemos las preferencias compartidas (SharedPreferences) usando una clave específica
        val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)
        // Iniciamos un editor para realizar cambios en las preferencias
        val editor = preferencias.edit()

        // Eliminar los sub-bloques que esten dentro de este bloque
        editor.remove("bloques_dias_entrenamiento_$bloque").apply()

        // Eliminar el bloque principal de la lista de bloques
        bloques.remove(bloque)

        // Guardar la lista de bloques actualizada
        guardarBloques()
    }

    // Método para actualizar el nombre de un bloque de entrenamiento
    fun updateBlock(oldBlock: String, newBlock: String) {
        // Buscamos la posición del bloque que queremos actualizar en la lista
        val indice = bloques.indexOf(oldBlock)
        //Si existe continuamos
        if (indice != -1) {
            bloques[indice] = newBlock  // Actualizar el bloque en la lista
            guardarBloques()  // Guardar la lista actualizada
            updateSubBlockKey(oldBlock, newBlock)  // Actualizar los sub-bloques relacionados con este bloque
        }
    }

    // Guardar los sub-bloques (días de entrenamiento o ejercicios) como JSON en SharedPreferences
    // Esto nos ayuda a guardar el orden de los sub-bloques cuando cerramos y volvemos a abrir la app
    private fun guardarSubBloques(bloque: String, subBloques: List<String>) {

        //Comprueba si esta autenticado el usuario
        userId?.let { uid ->

            // Accede a las preferencias compartidas con el nombre "blocks_pref"
            val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)

            val editor = preferencias.edit()
            // Inicia un editor para realizar cambios en las preferencias compartidas

            // Convierte la lista de sub-bloques en JSON para guardarla como texto
            val json = Gson().toJson(subBloques)

            // Guarda el JSON en las preferencias, usando una clave unica que combina el UID y el nombre del bloque
            editor.putString("sub_blocks_${uid}_$bloque", json)

            // Aplica los cambios realizados en las preferencias compartidas.
            editor.apply()
        }
    }

    private fun cargarSubBloques(bloque: String): MutableList<String> {
        // Comprueba si hay un usuario autenticado y obtiene su UID.
        userId?.let { uid ->
            // Accede a las preferencias compartidas usando el nombre "blocks_pref".
            val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)

            // Recupera los datos guardados en JSON correspondientes al bloque específico y al UID.
            val json = preferencias.getString("sub_blocks_${uid}_$bloque", null)

            // Si el JSON no es nulo ni está vacío, lo convierte a una lista mutable de cadenas.
            return if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<MutableList<String>>() {}.type  // Define el tipo esperado (lista mutable de cadenas).
                Gson().fromJson(json, type) ?: mutableListOf()  // Convierte el JSON a la lista o devuelve una lista vacía si hay un problema.
            } else {
                mutableListOf()  // Si no hay datos guardados, devuelve una lista vacía.
            }
        } ?: return mutableListOf()  // Si no hay UID (usuario no autenticado), devuelve una lista vacía.
    }


    // estos metodos tenian nombre para dia entrenamiento pero es para ejercicios.kt
    // Método para agregar un sub-bloque (Ejercicios) a un bloque específico
    fun addSubBlock(bloque: String, bloqueDiasEntrenamiento: String) {
        val bloquesDiasEntrenamiento = cargarSubBloques(bloque)  // Cargar los sub-bloques existentes
        bloquesDiasEntrenamiento.add(bloqueDiasEntrenamiento)  // Añadir el nuevo sub-bloque
        guardarSubBloques(bloque, bloquesDiasEntrenamiento)  // Guardar los sub-bloques actualizados
    }

    // Método para actualizar el nombre de un sub-bloque (Ejercicios)
    fun updateSubBlock(
        bloque: String,
        bloqueDiasEntrenamientoAnterior: String,
        bloqueDiasEntrenamientoNuevo: String
    ) {
        val bloquesDiasEntrenamiento = cargarSubBloques(bloque)  // Cargar los sub-bloques actuales
        val indice = bloquesDiasEntrenamiento.indexOf(bloqueDiasEntrenamientoAnterior)

        if (indice != -1) {
            bloquesDiasEntrenamiento[indice] = bloqueDiasEntrenamientoNuevo  // Actualizar el nombre del sub-bloque
            guardarSubBloques(bloque, bloquesDiasEntrenamiento)  // Guardar la lista actualizada

            // Copiar los datos del sub-bloque antiguo al nuevo
            val datosAntiguos = cargarSubBloques(bloqueDiasEntrenamientoAnterior)
            guardarSubBloques(bloqueDiasEntrenamientoNuevo, datosAntiguos)

            // Eliminar los datos del sub-bloque antiguo
            guardarSubBloques(bloqueDiasEntrenamientoAnterior, emptyList())

            // enviar las series asociadas
            val seriesAntiguas = cargarSeries(bloqueDiasEntrenamientoAnterior)  // Cargar las series del día antiguo
            guardarSeries(bloqueDiasEntrenamientoNuevo, seriesAntiguas)  // Guardar las series con el nuevo nombre
            guardarSeries(bloqueDiasEntrenamientoAnterior, emptyList())  // Limpiar las series del nombre antiguo
        }
    }

    // Método para obtener la lista de sub-bloques (Ejercicios) de un bloque específico
    fun getSubBlocks(bloque: String): MutableList<String> {
        return cargarSubBloques(bloque)  // Cargar y devolver los sub-bloques del bloque
    }


    // Eliminar un sub-bloque (Ejercicios) específico
    fun removeSubBlock(bloque: String, bloqueDiasEntrenamiento: String) {
        val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)
        val editor = preferencias.edit()

        // Eliminar los sub-bloques de ejercicios asociados al sub-bloque (día de entrenamiento)
        editor.remove("series_$bloqueDiasEntrenamiento")  // Eliminar las series del día de entrenamiento
        editor.remove("bloques_dias_entrenamiento_$bloqueDiasEntrenamiento")  // Eliminar otros datos relacionados

        // Cargar la lista actual de sub-bloques y eliminar el sub-bloque
        val bloquesDiasEntrenamientoList = cargarSubBloques(bloque)
        if (bloquesDiasEntrenamientoList.remove(bloqueDiasEntrenamiento)) {
            guardarSubBloques(bloque, bloquesDiasEntrenamientoList)  // Guardar la lista actualizada de sub-bloques
        }

        editor.apply()  // Aplicar los cambios en SharedPreferences
    }

    //puede servir para guardar cualquier bloque en cualquier kt
    // Actualizar la clave de un sub-bloque (Ejercicios) en SharedPreferences
    private fun updateSubBlockKey(bloqueDiasEntrenamientoAnterior: String, bloqueDiasEntrenamientoNuevo: String) {
        val datosRelacionados = cargarSubBloques(bloqueDiasEntrenamientoAnterior)  // Cargar los datos asociados
        guardarSubBloques(bloqueDiasEntrenamientoNuevo, datosRelacionados)  // Guardar con el nuevo nombre
        guardarSubBloques(bloqueDiasEntrenamientoAnterior, emptyList())  // Limpiar los datos del sub-bloque anterior
    }

    // Lista mutable que almacena las series en el orden original
    private val _seriesOrdenadas = mutableStateListOf<Pair<Int, Int>>()  // Series con orden
    // las series como una lista inmutable de pares (peso, repeticiones) ordenados.
    val seriesOrdenadas: List<Pair<Int, Int>> get() = _seriesOrdenadas

    // Método para cargar las series desde SharedPreferences
    fun getSeries(bloqueDiaEntrenamiento: String) {
        _seriesOrdenadas.clear()
        _seriesOrdenadas.addAll(cargarSeries(bloqueDiaEntrenamiento))
    }

    // Método para agregar una serie y mantener el orden
    // Añade una nueva serie a la lista y la guarda en SharedPreferences.
    fun addSerie(bloqueDiaEntrenamiento: String, serie: Pair<Int, Int>) {
        _seriesOrdenadas.add(serie)  // Añadir serie a la lista
        guardarSeries(bloqueDiaEntrenamiento, _seriesOrdenadas)  // Guardar lista actualizada
    }

    // Actualiza una serie existente y la guarda en SharedPreferences.
    fun updateSerie(bloqueDiaEntrenamiento: String, index: Int, serieNueva: Pair<Int, Int>) {
        if (index in _seriesOrdenadas.indices) {
            _seriesOrdenadas[index] = serieNueva  // Actualizar serie
            guardarSeries(bloqueDiaEntrenamiento, _seriesOrdenadas)  // Guardar lista actualizada
        }
    }

    // Elimina una serie por índice y guarda la lista actualizada.
    fun removeSerie(bloqueDiaEntrenamiento: String, index: Int) {
        if (index in _seriesOrdenadas.indices) {
            _seriesOrdenadas.removeAt(index)  // Eliminar serie
            guardarSeries(bloqueDiaEntrenamiento, _seriesOrdenadas)  // Guardar lista actualizada
        }
    }

    // Convierte la lista de series a JSON y la guarda en SharedPreferences.
    private fun guardarSeries(bloqueDiaEntrenamiento: String, series: List<Pair<Int, Int>>) {
        val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)
        val editor = preferencias.edit()
        val json = Gson().toJson(series)  // Convertir lista a JSON
        editor.putString("series_$bloqueDiaEntrenamiento", json)  // Guardar JSON con clave específica
        editor.apply()  // Aplicar cambios
    }

    // Método para cargar las series guardadas en SharedPreferences, devolviendo una lista de pares (peso, repeticiones).
    private fun cargarSeries(bloqueDiaEntrenamiento: String): MutableList<Pair<Int, Int>> {

        // Accede a las preferencias compartidas usando el nombre "blocks_pref".
        val preferencias = contexto.getSharedPreferences("blocks_pref", Context.MODE_PRIVATE)

        // Recupera los datos almacenados en formato JSON para el bloque o día de entrenamiento indicado.
        val json = preferencias.getString("series_$bloqueDiaEntrenamiento", null)

        // Si el JSON no es nulo, lo convierte a una lista mutable de pares (peso, repeticiones).
        return if (json != null) {
            val type = object : TypeToken<MutableList<Pair<Int, Int>>>() {}.type  // Define el tipo esperado como lista de pares (Int, Int).
            Gson().fromJson(json, type) ?: mutableListOf()  // Convierte el JSON a lista o devuelve una lista vacía si algo falla.
        } else {
            mutableListOf()  // Si no hay datos guardados, devuelve una lista vacía.
        }
    }


}
