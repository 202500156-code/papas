package com.example.myapplication

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object OfflineStorage {
    private const val FILE_NAME = "readwood_offline_vault.json"

    // Guarda novela completa o un capítulo específico en el almacenamiento interno privado de la app
    fun guardarOfflineEnApp(context: Context, novela: Novela, capituloSolo: Capitulo? = null) {
        val file = File(context.filesDir, FILE_NAME)
        val root = if (file.exists()) JSONArray(file.readText()) else JSONArray()

        // Buscar si la novela ya existe en la bóveda interna
        var novelaJson: JSONObject? = null
        for (i in 0 until root.length()) {
            val obj = root.getJSONObject(i)
            if (obj.getString("id") == novela.id) {
                novelaJson = obj
                break
            }
        }

        val capsAGuardar = if (capituloSolo != null) listOf(capituloSolo) else novela.capitulos.filter { !it.esBorrador }

        if (novelaJson == null) {
            novelaJson = JSONObject().apply {
                put("id", novela.id)
                put("titulo", novela.titulo)
                put("autor", novela.autor)
                put("categoria", novela.categoria)
                put("sinopsis", novela.sinopsis)
                put("capitulos", JSONArray())
            }
            root.put(novelaJson)
        }

        val capsArray = novelaJson.getJSONArray("capitulos")
        for (cap in capsAGuardar) {
            var existe = false
            for (j in 0 until capsArray.length()) {
                if (capsArray.getJSONObject(j).getString("id") == cap.id) {
                    capsArray.getJSONObject(j).put("contenido", cap.contenido)
                    capsArray.getJSONObject(j).put("titulo", cap.titulo)
                    existe = true
                    break
                }
            }
            if (!existe) {
                capsArray.put(JSONObject().apply {
                    put("id", cap.id)
                    put("numero", cap.numero)
                    put("titulo", cap.titulo)
                    put("contenido", cap.contenido)
                })
            }
        }

        file.writeText(root.toString())
    }

    fun obtenerNovelasOffline(context: Context): List<Novela> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val arr = JSONArray(file.readText())
        val resultado = mutableListOf<Novela>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val capsArr = obj.getJSONArray("capitulos")
            val caps = mutableListOf<Capitulo>()
            for (j in 0 until capsArr.length()) {
                val c = capsArr.getJSONObject(j)
                caps.add(
                    Capitulo(
                        id = c.getString("id"),
                        numero = c.optInt("numero", j + 1),
                        titulo = c.getString("titulo"),
                        contenido = c.getString("contenido")
                    )
                )
            }
            resultado.add(
                Novela(
                    id = obj.getString("id"),
                    titulo = obj.getString("titulo"),
                    autor = obj.getString("autor"),
                    categoria = obj.getString("categoria"),
                    sinopsis = obj.getString("sinopsis"),
                    capitulos = caps
                )
            )
        }
        return resultado
    }

    fun eliminarNovelaOffline(context: Context, novelaId: String) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return
        val arr = JSONArray(file.readText())
        val nuevo = JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getString("id") != novelaId) nuevo.put(obj)
        }
        file.writeText(nuevo.toString())
    }
}