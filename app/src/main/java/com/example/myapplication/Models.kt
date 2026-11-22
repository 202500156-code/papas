package com.example.myapplication

data class UsuarioPerfil(
    val id: String = "",
    val usuario: String = "",
    val correo: String = "",
    val mostrarCorreoPublico: Boolean = false,
    val passwordHash: String = "",
    val salt: String = "",
    val fotoBase64: String = "",
    val consintioFoto: Boolean = false,
    val bannerBase64: String = "",
    val bannerColorHex: String = "#5E2D40",
    val enlacesRedes: List<String> = emptyList(),
    val ocultarSeguidos: Boolean = false,
    val ocultarNovelasVistas: Boolean = false
)

data class UsuarioSeguimiento(
    val id: String = "",
    val seguidorId: String = "",
    val seguidorNombre: String = "",
    val seguidoId: String = "",
    val seguidoNombre: String = "",
    val oculta: Boolean = false
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
    val corazones: Int = 0,
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val comentariosCount: Int = 0,
    val vistas: Int = 0,
    val usuariosCorazon: List<String> = emptyList(),
    val usuariosLike: List<String> = emptyList(),
    val usuariosDislike: List<String> = emptyList(),
    val usuariosVistos: List<String> = emptyList()
)

data class Novela(
    val id: String = "",
    val autorId: String = "",
    val autor: String = "",
    val titulo: String = "",
    val sinopsis: String = "",
    val categoria: String = "Fantasía",
    val etiquetas: List<String> = emptyList(),
    val portadaBase64: String = "",
    val portadaColorHex: String = "#5E2D40",
    val esPublicada: Boolean = true,
    val fechaPublicacionProgramada: Long = 0L,
    val estaTerminada: Boolean = false,
    val corazones: Int = 0,
    val meGusta: Int = 0,
    val noMeGusta: Int = 0,
    val vistas: Int = 0,
    val totalComentarios: Int = 0,
    val capitulos: List<Capitulo> = emptyList()
)

data class PublicacionComunidad(
    val id: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val autorFotoBase64: String = "",
    val mensaje: String = "",
    val imagenBase64: String = "",
    val novelaProgramadaId: String = "",
    val novelaProgramadaTitulo: String = "",
    val fechaLanzamiento: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

data class ComentarioItem(
    val id: String = "",
    val novelaId: String = "",
    val capituloId: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val usuarioFotoBase64: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ReporteItem(
    val id: String = "",
    val reportadoPorId: String = "",
    val reportadoPorNombre: String = "",
    val tipoObjetivo: String = "",
    val objetivoId: String = "",
    val objetivoNombre: String = "",
    val motivo: String = "",
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