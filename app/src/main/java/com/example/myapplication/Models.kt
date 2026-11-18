package com.example.myapplication

data class UsuarioPerfil(
    val id: String = "",
    val usuario: String = "",
    val correo: String = "",
    val passwordHash: String = "",
    val salt: String = "",
    val ocultarSeguidos: Boolean = false
)

data class SeguimientoRelacion(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val seguidoAutorId: String = "",
    val seguidoAutorNombre: String = "",
    val novelaId: String = "",
    val novelaTitulo: String = "",
    val oculta: Boolean = false,
    val progresoPorcentaje: Int = 0,
    val capituloActual: Int = 1,
    val estadoLectura: String = "Leyendo"
)

data class Capitulo(
    val id: String = "",
    val numero: Int = 1,
    val titulo: String = "",
    val contenido: String = "",
    val esBorrador: Boolean = false,
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val comentariosCount: Int = 0
)

data class Novela(
    val id: String = "",
    val autorId: String = "",
    val autor: String = "",
    val titulo: String = "",
    val sinopsis: String = "",
    val categoria: String = "Fantasía",
    val etiquetas: List<String> = emptyList(),
    val esMas18: Boolean = false,
    val esPublicada: Boolean = true,
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val vistas: Int = 0,
    val capitulos: List<Capitulo> = emptyList()
)

data class ComentarioItem(
    val id: String = "",
    val novelaId: String = "",
    val capituloId: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificacionItem(
    val id: String = "",
    val usuarioId: String = "",
    val mensaje: String = "",
    val fecha: String = "Ahora",
    val leida: Boolean = false,
    val eliminada: Boolean = false
)