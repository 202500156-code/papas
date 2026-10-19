package com.example.myapplication

data class Capitulo(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val comentariosCount: Int = 0
)

data class Novela(
    val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val sinopsis: String = "",
    val categoria: String = "",
    val etiquetas: List<String> = emptyList(),
    val esMas18: Boolean = false,
    val imagenUrl: String = "",
    val esOculta: Boolean = false,
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val vistas: Int = 0,
    val capitulos: List<Capitulo> = emptyList()
)