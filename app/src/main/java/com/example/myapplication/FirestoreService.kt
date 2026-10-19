package com.example.myapplication

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val coleccionNovelas = db.collection("novelas")

    fun guardarNovela(
        novela: Novela,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documento = if (novela.id.isEmpty()) {
            coleccionNovelas.document()
        } else {
            coleccionNovelas.document(novela.id)
        }

        val datosNovela = hashMapOf(
            "id" to documento.id,
            "titulo" to novela.titulo,
            "autor" to novela.autor,
            "sinopsis" to novela.sinopsis,
            "categoria" to novela.categoria,
            "etiquetas" to novela.etiquetas,
            "esMas18" to novela.esMas18,
            "imagenUrl" to novela.imagenUrl,
            "esOculta" to novela.esOculta,
            "meGusta" to novela.meGusta,
            "noMeGusta" to novela.noMeGusta,
            "vistas" to novela.vistas,
            "capitulos" to novela.capitulos.map { cap ->
                hashMapOf(
                    "id" to cap.id,
                    "titulo" to cap.titulo,
                    "contenido" to cap.contenido,
                    "meGusta" to cap.meGusta,
                    "noMeGusta" to cap.noMeGusta,
                    "comentariosCount" to cap.comentariosCount
                )
            }
        )

        documento.set(datosNovela)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}