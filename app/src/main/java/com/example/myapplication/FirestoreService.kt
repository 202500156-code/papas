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
    private val colComentarios = db.collection("comentarios")
    private val colNotificaciones = db.collection("notificaciones")

    fun registrarUsuario(
        usuario: String,
        correo: String,
        passwordPlano: String,
        fotoBase64: String = "",
        consintioFoto: Boolean = false,
        onSuccess: (UsuarioPerfil) -> Unit,
        onError: (String) -> Unit
    ) {
        colUsuarios.whereEqualTo("usuario", usuario.trim()).get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    onError("El nombre de usuario ya está en uso.")
                    return@addOnSuccessListener
                }
                val doc = colUsuarios.document()
                val salt = NativeCryptoHelper.generateSalt()
                val hash = NativeCryptoHelper.generateBcryptStyleHash(passwordPlano.trim(), salt)
                val perfil = UsuarioPerfil(
                    id = doc.id,
                    usuario = usuario.trim(),
                    correo = correo.trim(),
                    passwordHash = hash,
                    salt = salt,
                    fotoBase64 = if (consintioFoto) fotoBase64 else "",
                    consintioFoto = consintioFoto,
                    ocultarSeguidos = false
                )
                doc.set(perfil)
                    .addOnSuccessListener {
                        crearNotificacion(doc.id, "Bienvenido a Readwood, ${perfil.usuario}.")
                        onSuccess(perfil)
                    }
                    .addOnFailureListener { onError(it.message ?: "Error al registrar") }
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

    fun actualizarFotoPerfil(usuarioId: String, fotoBase64: String, consintio: Boolean) {
        colUsuarios.document(usuarioId).update(
            mapOf("fotoBase64" to fotoBase64, "consintioFoto" to consintio)
        )
    }

    fun actualizarRedesUsuario(usuarioId: String, instagram: String, twitter: String, consintio: Boolean) {
        colUsuarios.document(usuarioId).update(
            mapOf(
                "redInstagram" to instagram.trim(),
                "redTwitter" to twitter.trim(),
                "consintioRedes" to consintio
            )
        )
    }

    fun actualizarPrivacidadSeguidos(usuarioId: String, ocultar: Boolean) {
        colUsuarios.document(usuarioId).update("ocultarSeguidos", ocultar)
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
        val novelaConId = novela.copy(id = doc.id)
        doc.set(novelaConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun eliminarNovela(novelaId: String, onSuccess: () -> Unit = {}) {
        colNovelas.document(novelaId).delete().addOnSuccessListener { onSuccess() }
    }

    fun registrarVotoNovela(novela: Novela, esLike: Boolean) {
        val campo = if (esLike) "meGusta" else "noMeGusta"
        colNovelas.document(novela.id).update(campo, FieldValue.increment(1))
    }

    fun incrementarVistaNovela(novela: Novela) {
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
        val doc = colComentarios.document()
        val item = ComentarioItem(
            id = doc.id,
            novelaId = novela.id,
            capituloId = capituloId,
            usuarioId = usuario.id,
            usuarioNombre = usuario.usuario,
            texto = texto.trim()
        )
        doc.set(item).addOnSuccessListener {
            colNovelas.document(novela.id).update("totalComentarios", FieldValue.increment(1))
            if (novela.autorId.isNotEmpty() && novela.autorId != usuario.id) {
                crearNotificacion(novela.autorId, "${usuario.usuario} comentó en '${novela.titulo}': \"${texto.take(30)}...\"")
            }
        }
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