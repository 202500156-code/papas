package com.example.myapplication

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.MessageDigest
import java.security.SecureRandom

object NativeCryptoHelper {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun generateBcryptStyleHash(text: String, salt: String): String {
        if (text.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest((text + salt).toByteArray())
        val base64Like = bytes.joinToString("") { "%02x".format(it) }.take(31)
        val salt22 = salt.take(22)
        return "\$2b\$10\$$salt22$base64Like"
    }

    fun verifyPassword(plainPassword: String, salt: String, storedHash: String): Boolean {
        return generateBcryptStyleHash(plainPassword, salt) == storedHash
    }
}

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val colUsuarios = db.collection("usuarios")
    private val colNovelas = db.collection("novelas")
    private val colSeguimientos = db.collection("seguimientos")
    private val colUsuariosSeguidos = db.collection("usuarios_seguidos")
    private val colComentarios = db.collection("comentarios")
    private val colNotificaciones = db.collection("notificaciones")
    private val colReportes = db.collection("reportes")

    fun registrarUsuario(
        usuario: String,
        correo: String,
        passwordPlano: String,
        fotoBase64: String = "",
        consintioFoto: Boolean = false,
        onSuccess: (UsuarioPerfil) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioLimpio = ProfanityFilter.censurar(usuario.trim())
        val correoLimpio = correo.trim().lowercase()

        colUsuarios.whereEqualTo("usuario", usuarioLimpio).get()
            .addOnSuccessListener { snapUser ->
                if (!snapUser.isEmpty) {
                    onError("Ese nombre de usuario ya está registrado. Elige otro.")
                    return@addOnSuccessListener
                }
                colUsuarios.whereEqualTo("correo", correoLimpio).get()
                    .addOnSuccessListener { snapMail ->
                        if (!snapMail.isEmpty) {
                            onError("Este correo ya está vinculado a otra cuenta.")
                            return@addOnSuccessListener
                        }

                        val doc = colUsuarios.document()
                        val salt = NativeCryptoHelper.generateSalt()
                        val hash = NativeCryptoHelper.generateBcryptStyleHash(passwordPlano.trim(), salt)
                        val perfil = UsuarioPerfil(
                            id = doc.id,
                            usuario = usuarioLimpio,
                            correo = correoLimpio,
                            passwordHash = hash,
                            salt = salt,
                            fotoBase64 = if (consintioFoto) fotoBase64 else "",
                            consintioFoto = consintioFoto,
                            ocultarSeguidos = false,
                            ocultarNovelasVistas = false
                        )
                        doc.set(perfil)
                            .addOnSuccessListener {
                                crearNotificacion(doc.id, "Bienvenido a Readwood, ${perfil.usuario}.")
                                onSuccess(perfil)
                            }
                            .addOnFailureListener { onError(it.message ?: "Error al registrar") }
                    }
                    .addOnFailureListener { onError(it.message ?: "Error al validar correo") }
            }
            .addOnFailureListener { onError(it.message ?: "Error de red") }
    }

    fun iniciarSesion(
        usuario: String,
        passwordPlano: String,
        onSuccess: (UsuarioPerfil) -> Unit,
        onError: (String) -> Unit
    ) {
        colUsuarios.whereEqualTo("usuario", usuario.trim()).get()
            .addOnSuccessListener { snap ->
                val doc = snap.documents.firstOrNull()
                if (doc == null) {
                    onError("Usuario no encontrado en Firebase.")
                    return@addOnSuccessListener
                }
                val perfil = doc.toObject(UsuarioPerfil::class.java)?.copy(id = doc.id)
                if (perfil != null && NativeCryptoHelper.verifyPassword(passwordPlano.trim(), perfil.salt, perfil.passwordHash)) {
                    onSuccess(perfil)
                } else {
                    onError("Contraseña incorrecta.")
                }
            }
            .addOnFailureListener { onError(it.message ?: "Error de conexión") }
    }

    fun escucharPerfilUsuario(
        usuarioId: String,
        onUpdate: (UsuarioPerfil) -> Unit
    ): ListenerRegistration {
        return colUsuarios.document(usuarioId).addSnapshotListener { doc, _ ->
            val perfil = doc?.toObject(UsuarioPerfil::class.java)?.copy(id = doc.id)
            if (perfil != null) {
                onUpdate(perfil)
            }
        }
    }

    fun buscarUsuarios(
        query: String,
        onResult: (List<UsuarioPerfil>) -> Unit
    ) {
        colUsuarios.get().addOnSuccessListener { snap ->
            val q = query.trim().lowercase()
            val lista = snap.documents.mapNotNull { doc ->
                doc.toObject(UsuarioPerfil::class.java)?.copy(id = doc.id)
            }.filter { u ->
                q.isEmpty() || u.usuario.lowercase().contains(q)
            }
            onResult(lista)
        }
    }

    fun seguirODejarDeSeguirUsuario(
        seguidor: UsuarioPerfil,
        objetivo: UsuarioPerfil,
        yaLoSigue: Boolean,
        onDone: () -> Unit = {}
    ) {
        if (seguidor.id == objetivo.id) return
        val docId = "${seguidor.id}_user_${objetivo.id}"
        if (yaLoSigue) {
            colUsuariosSeguidos.document(docId).delete().addOnSuccessListener { onDone() }
        } else {
            val rel = UsuarioSeguimiento(
                id = docId,
                seguidorId = seguidor.id,
                seguidorNombre = seguidor.usuario,
                seguidoId = objetivo.id,
                seguidoNombre = objetivo.usuario
            )
            colUsuariosSeguidos.document(docId).set(rel).addOnSuccessListener {
                crearNotificacion(objetivo.id, "${seguidor.usuario} comenzó a seguir tu perfil.")
                onDone()
            }
        }
    }

    fun escucharUsuariosQueSigo(
        usuarioId: String,
        onUpdate: (List<UsuarioSeguimiento>) -> Unit
    ): ListenerRegistration {
        return colUsuariosSeguidos.whereEqualTo("seguidorId", usuarioId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull {
                    it.toObject(UsuarioSeguimiento::class.java)?.copy(id = it.id)
                }
                onUpdate(lista)
            }
    }

    fun obtenerActividadPublicaUsuario(
        usuarioObjetivoId: String,
        onResult: (usuariosSeguidos: List<UsuarioSeguimiento>, novelasVistas: List<SeguimientoRelacion>) -> Unit
    ) {
        colUsuariosSeguidos.whereEqualTo("seguidorId", usuarioObjetivoId).get()
            .addOnSuccessListener { snapUsers ->
                val usersList = snapUsers.documents.mapNotNull {
                    it.toObject(UsuarioSeguimiento::class.java)?.copy(id = it.id)
                }
                colSeguimientos.whereEqualTo("usuarioId", usuarioObjetivoId).get()
                    .addOnSuccessListener { snapNovelas ->
                        val novList = snapNovelas.documents.mapNotNull {
                            it.toObject(SeguimientoRelacion::class.java)?.copy(id = it.id)
                        }.filter { !it.oculta } // Respeta novelas ocultas individualmente
                        onResult(usersList, novList)
                    }
            }
    }

    fun actualizarFotoPerfil(usuarioId: String, fotoBase64: String, consintio: Boolean) {
        colUsuarios.document(usuarioId).update(
            mapOf("fotoBase64" to fotoBase64, "consintioFoto" to consintio)
        )
    }

    fun actualizarRedesUsuario(usuarioId: String, instagram: String, twitter: String, consintio: Boolean) {
        colUsuarios.document(usuarioId).update(
            mapOf(
                "redInstagram" to ProfanityFilter.censurar(instagram.trim()),
                "redTwitter" to ProfanityFilter.censurar(twitter.trim()),
                "consintioRedes" to consintio
            )
        )
    }

    fun actualizarPrivacidadSeguidos(usuarioId: String, ocultarUsuarios: Boolean, ocultarNovelas: Boolean) {
        colUsuarios.document(usuarioId).update(
            mapOf(
                "ocultarSeguidos" to ocultarUsuarios,
                "ocultarNovelasVistas" to ocultarNovelas
            )
        )
    }

    fun escucharNovelas(
        usuarioActualId: String,
        onUpdate: (List<Novela>) -> Unit
    ): ListenerRegistration {
        return colNovelas.addSnapshotListener { snap, _ ->
            if (snap == null) return@addSnapshotListener
            val lista = snap.documents.mapNotNull { doc ->
                val n = doc.toObject(Novela::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                if (!n.esPublicada && n.autorId != usuarioActualId) return@mapNotNull null
                val capsVisibles = if (n.autorId == usuarioActualId) {
                    n.capitulos
                } else {
                    n.capitulos.filter { !it.esBorrador }
                }
                n.copy(capitulos = capsVisibles)
            }
            onUpdate(lista)
        }
    }

    fun guardarNovela(novela: Novela, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val doc = if (novela.id.isEmpty()) colNovelas.document() else colNovelas.document(novela.id)
        val novelaCensurada = novela.copy(
            id = doc.id,
            titulo = ProfanityFilter.censurar(novela.titulo),
            sinopsis = ProfanityFilter.censurar(novela.sinopsis),
            categoria = ProfanityFilter.censurar(novela.categoria),
            etiquetas = novela.etiquetas.map { ProfanityFilter.censurar(it) },
            capitulos = novela.capitulos.map { cap ->
                cap.copy(
                    titulo = ProfanityFilter.censurar(cap.titulo),
                    contenido = ProfanityFilter.censurar(cap.contenido)
                )
            }
        )
        doc.set(novelaCensurada)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun eliminarNovela(novelaId: String, onSuccess: () -> Unit = {}) {
        colNovelas.document(novelaId).delete().addOnSuccessListener { onSuccess() }
    }

    fun registrarReaccionCapitulo(
        novela: Novela,
        capituloId: String,
        tipo: String
    ) {
        val nuevosCapitulos = novela.capitulos.map { cap ->
            if (cap.id == capituloId) {
                when (tipo) {
                    "CORAZON" -> cap.copy(corazones = cap.corazones + 1)
                    "LIKE" -> cap.copy(meGusta = cap.meGusta + 1)
                    "DISLIKE" -> cap.copy(noMeGusta = cap.noMeGusta + 1)
                    else -> cap
                }
            } else cap
        }

        val nuevoCorazones = novela.corazones + if (tipo == "CORAZON") 1 else 0
        val nuevoLikes = novela.meGusta + if (tipo == "LIKE") 1 else 0
        val nuevoDislikes = novela.noMeGusta + if (tipo == "DISLIKE") 1 else 0

        colNovelas.document(novela.id).update(
            mapOf(
                "capitulos" to nuevosCapitulos,
                "corazones" to nuevoCorazones,
                "meGusta" to nuevoLikes,
                "noMeGusta" to nuevoDislikes
            )
        )
    }

    fun incrementarVistaNovela(novela: Novela, lectorId: String) {
        if (novela.autorId == lectorId) return
        colNovelas.document(novela.id).update("vistas", FieldValue.increment(1))
    }

    fun escucharSeguimientosUsuario(
        usuarioId: String,
        onUpdate: (List<SeguimientoRelacion>) -> Unit
    ): ListenerRegistration {
        return colSeguimientos.whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull {
                    it.toObject(SeguimientoRelacion::class.java)?.copy(id = it.id)
                }
                onUpdate(lista)
            }
    }

    fun seguirNovelaYAutor(
        usuario: UsuarioPerfil,
        novela: Novela,
        ocultarDeOtros: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        if (usuario.id == novela.autorId) return

        val docId = "${usuario.id}_${novela.id}"
        val rel = SeguimientoRelacion(
            id = docId,
            usuarioId = usuario.id,
            usuarioNombre = usuario.usuario,
            seguidoAutorId = novela.autorId,
            seguidoAutorNombre = novela.autor,
            novelaId = novela.id,
            novelaTitulo = novela.titulo,
            oculta = ocultarDeOtros,
            progresoPorcentaje = 0,
            capituloActual = 1,
            estadoLectura = "Leyendo"
        )
        colSeguimientos.document(docId).set(rel).addOnSuccessListener {
            if (novela.autorId.isNotEmpty() && novela.autorId != usuario.id) {
                crearNotificacion(novela.autorId, "${usuario.usuario} comenzó a seguir tu obra '${novela.titulo}'.")
            }
            onSuccess()
        }
    }

    fun alternarOcultarSeguimiento(relacionId: String, oculta: Boolean) {
        colSeguimientos.document(relacionId).update("oculta", oculta)
    }

    fun eliminarSeguimiento(relacionId: String) {
        colSeguimientos.document(relacionId).delete()
    }

    fun actualizarProgresoLectura(relacionId: String, capitulo: Int, porcentaje: Int) {
        val estado = if (porcentaje >= 100) "Terminada" else "Leyendo"
        colSeguimientos.document(relacionId).update(
            mapOf(
                "capituloActual" to capitulo,
                "progresoPorcentaje" to porcentaje,
                "estadoLectura" to estado
            )
        )
    }

    fun escucharComentarios(
        novelaId: String,
        capituloId: String,
        onUpdate: (List<ComentarioItem>) -> Unit
    ): ListenerRegistration {
        return colComentarios
            .whereEqualTo("novelaId", novelaId)
            .whereEqualTo("capituloId", capituloId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull {
                    it.toObject(ComentarioItem::class.java)?.copy(id = it.id)
                }.sortedByDescending { it.timestamp }
                onUpdate(lista)
            }
    }

    fun publicarComentario(
        novela: Novela,
        capituloId: String,
        usuario: UsuarioPerfil,
        texto: String
    ) {
        if (texto.isBlank()) return
        val textoCensurado = ProfanityFilter.censurar(texto.trim())
        val doc = colComentarios.document()
        val item = ComentarioItem(
            id = doc.id,
            novelaId = novela.id,
            capituloId = capituloId,
            usuarioId = usuario.id,
            usuarioNombre = usuario.usuario,
            texto = textoCensurado
        )
        doc.set(item).addOnSuccessListener {
            val nuevosCaps = novela.capitulos.map { cap ->
                if (cap.id == capituloId) cap.copy(comentariosCount = cap.comentariosCount + 1) else cap
            }
            colNovelas.document(novela.id).update(
                mapOf(
                    "totalComentarios" to FieldValue.increment(1),
                    "capitulos" to nuevosCaps
                )
            )
            if (novela.autorId.isNotEmpty() && novela.autorId != usuario.id) {
                crearNotificacion(novela.autorId, "${usuario.usuario} comentó en '${novela.titulo}': \"${textoCensurado.take(30)}...\"")
            }
        }
    }

    fun enviarReporte(
        reportante: UsuarioPerfil,
        tipoObjetivo: String,
        objetivoId: String,
        objetivoNombre: String,
        motivo: String,
        onSuccess: () -> Unit = {}
    ) {
        val doc = colReportes.document()
        val reporte = ReporteItem(
            id = doc.id,
            reportadoPorId = reportante.id,
            reportadoPorNombre = reportante.usuario,
            tipoObjetivo = tipoObjetivo,
            objetivoId = objetivoId,
            objetivoNombre = objetivoNombre,
            motivo = ProfanityFilter.censurar(motivo.trim().ifEmpty { "Contenido inapropiado" })
        )
        doc.set(reporte).addOnSuccessListener { onSuccess() }
    }

    fun crearNotificacion(usuarioId: String, mensaje: String) {
        val doc = colNotificaciones.document()
        val notif = NotificacionItem(
            id = doc.id,
            usuarioId = usuarioId,
            mensaje = mensaje,
            fecha = "Reciente",
            leida = false,
            eliminada = false
        )
        doc.set(notif)
    }

    fun escucharNotificacionesActivas(
        usuarioId: String,
        onUpdate: (List<NotificacionItem>) -> Unit
    ): ListenerRegistration {
        return colNotificaciones
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("eliminada", false)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull {
                    it.toObject(NotificacionItem::class.java)?.copy(id = it.id)
                }
                onUpdate(lista)
            }
    }

    fun marcarNotificacionLeida(notificacionId: String) {
        colNotificaciones.document(notificacionId).update("leida", true)
    }

    fun borradoLogicoNotificacion(notificacionId: String) {
        colNotificaciones.document(notificacionId).update("eliminada", true)
    }
}