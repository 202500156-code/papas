package com.example.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

private typealias CapituloMap = Map<String, Any>

object FirestoreService {
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun generarSal(): String = UUID.randomUUID().toString().replace("-", "").take(16)

    private fun hashPassword(passwordPlano: String, sal: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$sal:$passwordPlano".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun fechaActualTexto(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // =========================================================================
    // AUTENTICACIÓN Y RECUPERACIÓN OFICIAL CON FIREBASE AUTH
    // =========================================================================

    fun registrarUsuario(
        usuario: String,
        correo: String,
        passwordPlano: String,
        fotoBase64: String,
        consintioFoto: Boolean,
        onSuccess: (UsuarioPerfil) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioLimpio = usuario.trim()
        val correoLimpio = correo.trim().lowercase()

        if (passwordPlano.length < 6) {
            onError("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        db.collection("usuarios")
            .whereEqualTo("usuario", usuarioLimpio)
            .get()
            .addOnSuccessListener { docsUsuario ->
                if (!docsUsuario.isEmpty) {
                    onError("Ese nombre de usuario ya está en uso.")
                    return@addOnSuccessListener
                }

                db.collection("usuarios")
                    .whereEqualTo("correo", correoLimpio)
                    .get()
                    .addOnSuccessListener { docsCorreo ->
                        if (!docsCorreo.isEmpty) {
                            onError("Ese correo ya está registrado.")
                            return@addOnSuccessListener
                        }

                        auth.createUserWithEmailAndPassword(correoLimpio, passwordPlano)
                            .addOnSuccessListener { authResult ->
                                val sal = generarSal()
                                val hash = hashPassword(passwordPlano, sal)
                                val docRef = db.collection("usuarios").document()

                                val nuevoPerfil = UsuarioPerfil(
                                    id = docRef.id,
                                    usuario = usuarioLimpio,
                                    correo = correoLimpio,
                                    mostrarCorreoPublico = false,
                                    fotoBase64 = fotoBase64,
                                    consintioFoto = consintioFoto,
                                    bannerBase64 = "",
                                    bannerColorHex = "#5E2D40",
                                    enlacesRedes = emptyList()
                                )

                                val datos = hashMapOf(
                                    "id" to nuevoPerfil.id,
                                    "authUid" to (authResult.user?.uid ?: ""),
                                    "usuario" to nuevoPerfil.usuario,
                                    "correo" to nuevoPerfil.correo,
                                    "passwordHash" to hash,
                                    "passwordSal" to sal,
                                    "mostrarCorreoPublico" to false,
                                    "fotoBase64" to fotoBase64,
                                    "consintioFoto" to consintioFoto,
                                    "bannerBase64" to "",
                                    "bannerColorHex" to "#5E2D40",
                                    "enlacesRedes" to emptyList<String>()
                                )

                                docRef.set(datos)
                                    .addOnSuccessListener { onSuccess(nuevoPerfil) }
                                    .addOnFailureListener { onError("Error al guardar perfil: ${it.message}") }
                            }
                            .addOnFailureListener { e ->
                                onError("Error en Firebase Auth: ${e.localizedMessage ?: "No se pudo crear la cuenta"}")
                            }
                    }
                    .addOnFailureListener { onError("Error al verificar correo.") }
            }
            .addOnFailureListener { onError("Error al verificar usuario.") }
    }

    fun iniciarSesion(
        usuario: String,
        passwordPlano: String,
        onSuccess: (UsuarioPerfil) -> Unit,
        onError: (String) -> Unit
    ) {
        val usuarioLimpio = usuario.trim()
        db.collection("usuarios")
            .whereEqualTo("usuario", usuarioLimpio)
            .get()
            .addOnSuccessListener { docs ->
                val doc = docs.documents.firstOrNull()
                if (doc == null) {
                    onError("Usuario no encontrado.")
                    return@addOnSuccessListener
                }

                val correoRegistrado = doc.getString("correo")?.trim()?.lowercase() ?: ""
                @Suppress("UNCHECKED_CAST")
                val redes = doc.get("enlacesRedes") as? List<String> ?: emptyList()

                val perfil = UsuarioPerfil(
                    id = doc.id,
                    usuario = doc.getString("usuario") ?: usuarioLimpio,
                    correo = correoRegistrado,
                    mostrarCorreoPublico = doc.getBoolean("mostrarCorreoPublico") ?: false,
                    fotoBase64 = doc.getString("fotoBase64") ?: "",
                    consintioFoto = doc.getBoolean("consintioFoto") ?: false,
                    bannerBase64 = doc.getString("bannerBase64") ?: "",
                    bannerColorHex = doc.getString("bannerColorHex") ?: "#5E2D40",
                    enlacesRedes = redes
                )

                if (correoRegistrado.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(correoRegistrado, passwordPlano)
                        .addOnSuccessListener {
                            val nuevaSal = generarSal()
                            val nuevoHash = hashPassword(passwordPlano, nuevaSal)
                            doc.reference.update(
                                mapOf(
                                    "passwordHash" to nuevoHash,
                                    "passwordSal" to nuevaSal
                                )
                            )
                            onSuccess(perfil)
                        }
                        .addOnFailureListener {
                            val sal = doc.getString("passwordSal") ?: ""
                            val hashGuardado = doc.getString("passwordHash") ?: ""
                            if (hashGuardado.isNotEmpty() && hashGuardado == hashPassword(passwordPlano, sal)) {
                                auth.createUserWithEmailAndPassword(correoRegistrado, passwordPlano)
                                onSuccess(perfil)
                            } else {
                                onError("Contraseña incorrecta.")
                            }
                        }
                } else {
                    onError("Esta cuenta no tiene un correo vinculado.")
                }
            }
            .addOnFailureListener { onError("Error de conexión: ${it.message}") }
    }

    fun enviarCorreoRecuperacionFirebase(
        correo: String,
        onSuccess: (usuarioEncontrado: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val correoLimpio = correo.trim().lowercase()
        db.collection("usuarios")
            .whereEqualTo("correo", correoLimpio)
            .get()
            .addOnSuccessListener { docs ->
                val doc = docs.documents.firstOrNull()
                if (doc == null) {
                    onError("No existe ninguna cuenta registrada con ese correo.")
                    return@addOnSuccessListener
                }

                val nombreUsuario = doc.getString("usuario") ?: ""
                val claveTemporal = UUID.randomUUID().toString().replace("-", "").take(12) + "A1!"

                auth.createUserWithEmailAndPassword(correoLimpio, claveTemporal)
                    .addOnCompleteListener {
                        auth.sendPasswordResetEmail(correoLimpio)
                            .addOnSuccessListener {
                                onSuccess(nombreUsuario)
                            }
                            .addOnFailureListener { e ->
                                onError("Firebase no pudo enviar el correo: ${e.localizedMessage}")
                            }
                    }
            }
            .addOnFailureListener {
                onError("Error de conexión al verificar el correo.")
            }
    }

    // =========================================================================
    // PERFIL Y BÚSQUEDA DE USUARIOS
    // =========================================================================

    fun escucharPerfilUsuario(usuarioId: String, onCambio: (UsuarioPerfil) -> Unit): ListenerRegistration {
        return db.collection("usuarios").document(usuarioId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val redes = doc.get("enlacesRedes") as? List<String> ?: emptyList()
                    onCambio(
                        UsuarioPerfil(
                            id = doc.id,
                            usuario = doc.getString("usuario") ?: "",
                            correo = doc.getString("correo") ?: "",
                            mostrarCorreoPublico = doc.getBoolean("mostrarCorreoPublico") ?: false,
                            fotoBase64 = doc.getString("fotoBase64") ?: "",
                            consintioFoto = doc.getBoolean("consintioFoto") ?: false,
                            bannerBase64 = doc.getString("bannerBase64") ?: "",
                            bannerColorHex = doc.getString("bannerColorHex") ?: "#5E2D40",
                            enlacesRedes = redes
                        )
                    )
                }
            }
    }

    fun obtenerPerfilPorId(usuarioId: String, onResult: (UsuarioPerfil?) -> Unit) {
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val redes = doc.get("enlacesRedes") as? List<String> ?: emptyList()
                    onResult(
                        UsuarioPerfil(
                            id = doc.id,
                            usuario = doc.getString("usuario") ?: "",
                            correo = doc.getString("correo") ?: "",
                            mostrarCorreoPublico = doc.getBoolean("mostrarCorreoPublico") ?: false,
                            fotoBase64 = doc.getString("fotoBase64") ?: "",
                            consintioFoto = doc.getBoolean("consintioFoto") ?: false,
                            bannerBase64 = doc.getString("bannerBase64") ?: "",
                            bannerColorHex = doc.getString("bannerColorHex") ?: "#5E2D40",
                            enlacesRedes = redes
                        )
                    )
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    fun buscarUsuarios(query: String, onResult: (List<UsuarioPerfil>) -> Unit) {
        val q = query.trim()
        db.collection("usuarios").get()
            .addOnSuccessListener { snap ->
                val lista = snap.documents.mapNotNull { doc ->
                    val nombre = doc.getString("usuario") ?: return@mapNotNull null
                    if (q.isEmpty() || nombre.contains(q, ignoreCase = true)) {
                        @Suppress("UNCHECKED_CAST")
                        val redes = doc.get("enlacesRedes") as? List<String> ?: emptyList()
                        UsuarioPerfil(
                            id = doc.id,
                            usuario = nombre,
                            correo = doc.getString("correo") ?: "",
                            mostrarCorreoPublico = doc.getBoolean("mostrarCorreoPublico") ?: false,
                            fotoBase64 = doc.getString("fotoBase64") ?: "",
                            consintioFoto = doc.getBoolean("consintioFoto") ?: false,
                            bannerBase64 = doc.getString("bannerBase64") ?: "",
                            bannerColorHex = doc.getString("bannerColorHex") ?: "#5E2D40",
                            enlacesRedes = redes
                        )
                    } else null
                }
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun actualizarFotoPerfil(usuarioId: String, fotoBase64: String, consintio: Boolean) {
        db.collection("usuarios").document(usuarioId).update(
            mapOf(
                "fotoBase64" to fotoBase64,
                "consintioFoto" to consintio
            )
        )
    }

    fun actualizarBannerPerfil(usuarioId: String, bannerBase64: String, bannerColorHex: String) {
        db.collection("usuarios").document(usuarioId).update(
            mapOf(
                "bannerBase64" to bannerBase64,
                "bannerColorHex" to bannerColorHex
            )
        )
    }

    fun actualizarEnlacesYCorreoPublico(usuarioId: String, enlaces: List<String>, mostrarCorreo: Boolean) {
        db.collection("usuarios").document(usuarioId).update(
            mapOf(
                "enlacesRedes" to enlaces,
                "mostrarCorreoPublico" to mostrarCorreo
            )
        )
    }

    // =========================================================================
    // NOVELAS, CAPÍTULOS, VISTAS Y REACCIONES
    // =========================================================================

    fun escucharNovelas(usuarioActualId: String, onCambio: (List<Novela>) -> Unit): ListenerRegistration {
        return db.collection("novelas")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val ahora = System.currentTimeMillis()
                val lista = snap.documents.mapNotNull { doc ->
                    if (!doc.exists()) return@mapNotNull null
                    val titulo = doc.getString("titulo") ?: ""
                    if (titulo.isBlank()) return@mapNotNull null

                    val autorId = doc.getString("autorId") ?: ""
                    var esPublicada = doc.getBoolean("esPublicada") ?: false
                    val fechaProg = doc.getLong("fechaPublicacionProgramada") ?: 0L

                    if (!esPublicada && fechaProg > 0L && fechaProg <= ahora) {
                        esPublicada = true
                        doc.reference.update("esPublicada", true)
                    }

                    @Suppress("UNCHECKED_CAST")
                    val etiquetas = doc.get("etiquetas") as? List<String> ?: emptyList()

                    @Suppress("UNCHECKED_CAST")
                    val rawCaps = doc.get("capitulos") as? List<CapituloMap> ?: emptyList()
                    val capitulos = rawCaps.map { c ->
                        @Suppress("UNCHECKED_CAST")
                        val uCorazon = c["usuariosCorazon"] as? List<String> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val uLike = c["usuariosLike"] as? List<String> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val uDislike = c["usuariosDislike"] as? List<String> ?: emptyList()
                        @Suppress("UNCHECKED_CAST")
                        val uVistos = c["usuariosVistos"] as? List<String> ?: emptyList()

                        Capitulo(
                            id = c["id"] as? String ?: UUID.randomUUID().toString(),
                            numero = (c["numero"] as? Long)?.toInt() ?: 1,
                            titulo = c["titulo"] as? String ?: "",
                            contenido = c["contenido"] as? String ?: "",
                            esBorrador = c["esBorrador"] as? Boolean ?: false,
                            corazones = (c["corazones"] as? Long)?.toInt() ?: 0,
                            meGusta = (c["meGusta"] as? Long)?.toInt() ?: 0,
                            noMeGusta = (c["noMeGusta"] as? Long)?.toInt() ?: 0,
                            vistas = (c["vistas"] as? Long)?.toInt() ?: 0,
                            comentariosCount = (c["comentariosCount"] as? Long)?.toInt() ?: 0,
                            usuariosCorazon = uCorazon,
                            usuariosLike = uLike,
                            usuariosDislike = uDislike,
                            usuariosVistos = uVistos
                        )
                    }.filter { !it.esBorrador || autorId == usuarioActualId }

                    Novela(
                        id = doc.id,
                        autorId = autorId,
                        autor = doc.getString("autor") ?: "",
                        titulo = titulo,
                        sinopsis = doc.getString("sinopsis") ?: "",
                        categoria = doc.getString("categoria") ?: "Fantasía",
                        etiquetas = etiquetas,
                        portadaBase64 = doc.getString("portadaBase64") ?: "",
                        portadaColorHex = doc.getString("portadaColorHex") ?: "#5E2D40",
                        esPublicada = esPublicada,
                        fechaPublicacionProgramada = fechaProg,
                        corazones = capitulos.sumOf { it.corazones },
                        meGusta = capitulos.sumOf { it.meGusta },
                        noMeGusta = capitulos.sumOf { it.noMeGusta },
                        vistas = capitulos.sumOf { it.vistas },
                        totalComentarios = capitulos.sumOf { it.comentariosCount },
                        capitulos = capitulos
                    )
                }
                onCambio(lista)
            }
    }

    fun guardarNovela(
        novela: Novela,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = if (novela.id.isBlank()) db.collection("novelas").document()
        else db.collection("novelas").document(novela.id)

        val capsMap = novela.capitulos.map { c ->
            hashMapOf(
                "id" to c.id,
                "numero" to c.numero,
                "titulo" to c.titulo,
                "contenido" to c.contenido,
                "esBorrador" to c.esBorrador,
                "corazones" to c.corazones,
                "meGusta" to c.meGusta,
                "noMeGusta" to c.noMeGusta,
                "vistas" to c.vistas,
                "comentariosCount" to c.comentariosCount,
                "usuariosCorazon" to c.usuariosCorazon,
                "usuariosLike" to c.usuariosLike,
                "usuariosDislike" to c.usuariosDislike,
                "usuariosVistos" to c.usuariosVistos
            )
        }

        val data = hashMapOf(
            "id" to docRef.id,
            "autorId" to novela.autorId,
            "autor" to novela.autor,
            "titulo" to novela.titulo,
            "sinopsis" to novela.sinopsis,
            "categoria" to novela.categoria,
            "etiquetas" to novela.etiquetas,
            "portadaBase64" to novela.portadaBase64,
            "portadaColorHex" to novela.portadaColorHex,
            "esPublicada" to novela.esPublicada,
            "fechaPublicacionProgramada" to novela.fechaPublicacionProgramada,
            "capitulos" to capsMap
        )

        docRef.set(data)
            .addOnSuccessListener { onSuccess(docRef.id) }
            .addOnFailureListener { onFailure(it) }
    }

    fun eliminarNovela(novelaId: String, onSuccess: () -> Unit = {}) {
        val novRef = db.collection("novelas").document(novelaId)

        novRef.collection("comentarios").get().addOnSuccessListener { coms ->
            coms.documents.forEach { it.reference.delete() }
        }

        db.collection("seguimientos")
            .whereEqualTo("novelaId", novelaId)
            .get()
            .addOnSuccessListener { segs ->
                segs.documents.forEach { it.reference.delete() }
            }

        db.collection("comunidad")
            .whereEqualTo("novelaProgramadaId", novelaId)
            .get()
            .addOnSuccessListener { pubs ->
                pubs.documents.forEach { it.reference.delete() }
            }

        novRef.delete()
            .addOnSuccessListener { onSuccess() }
    }

    fun incrementarVistaUnicaCapitulo(novela: Novela, capituloId: String, usuarioId: String) {
        val docRef = db.collection("novelas").document(novela.id)
        db.runTransaction { transaction ->
            val snap = transaction.get(docRef)
            @Suppress("UNCHECKED_CAST")
            val rawCaps = snap.get("capitulos") as? List<CapituloMap> ?: return@runTransaction
            val nuevosCaps = rawCaps.map { c ->
                val mutable = c.toMutableMap()
                if (c["id"] == capituloId) {
                    @Suppress("UNCHECKED_CAST")
                    val vistos = (c["usuariosVistos"] as? List<String> ?: emptyList()).toMutableList()
                    if (usuarioId !in vistos) {
                        vistos.add(usuarioId)
                        mutable["usuariosVistos"] = vistos
                        mutable["vistas"] = vistos.size
                    }
                }
                mutable
            }
            transaction.update(docRef, "capitulos", nuevosCaps)
        }
    }

    fun registrarReaccionCapitulo(
        novela: Novela,
        capituloId: String,
        usuarioId: String,
        tipo: String,
        onAviso: (String) -> Unit
    ) {
        val docRef = db.collection("novelas").document(novela.id)
        db.runTransaction { transaction ->
            val snap = transaction.get(docRef)
            @Suppress("UNCHECKED_CAST")
            val rawCaps = snap.get("capitulos") as? List<CapituloMap> ?: return@runTransaction
            val nuevosCaps = rawCaps.map { c ->
                val mutable = c.toMutableMap()
                if (c["id"] == capituloId) {
                    @Suppress("UNCHECKED_CAST")
                    val corazones = (c["usuariosCorazon"] as? List<String> ?: emptyList()).toMutableList()
                    @Suppress("UNCHECKED_CAST")
                    val likes = (c["usuariosLike"] as? List<String> ?: emptyList()).toMutableList()
                    @Suppress("UNCHECKED_CAST")
                    val dislikes = (c["usuariosDislike"] as? List<String> ?: emptyList()).toMutableList()

                    when (tipo) {
                        "CORAZON" -> {
                            if (usuarioId in corazones) corazones.remove(usuarioId) else corazones.add(usuarioId)
                        }
                        "LIKE" -> {
                            if (usuarioId in likes) likes.remove(usuarioId)
                            else {
                                likes.add(usuarioId)
                                dislikes.remove(usuarioId)
                            }
                        }
                        "DISLIKE" -> {
                            if (usuarioId in dislikes) dislikes.remove(usuarioId)
                            else {
                                dislikes.add(usuarioId)
                                likes.remove(usuarioId)
                            }
                        }
                    }

                    mutable["usuariosCorazon"] = corazones
                    mutable["corazones"] = corazones.size
                    mutable["usuariosLike"] = likes
                    mutable["meGusta"] = likes.size
                    mutable["usuariosDislike"] = dislikes
                    mutable["noMeGusta"] = dislikes.size
                }
                mutable
            }
            transaction.update(docRef, "capitulos", nuevosCaps)
        }.addOnSuccessListener {
            onAviso("Reacción actualizada")
        }
    }

    // =========================================================================
    // COMENTARIOS EN CAPÍTULOS
    // =========================================================================

    fun escucharComentarios(
        novelaId: String,
        capituloId: String,
        onCambio: (List<ComentarioItem>) -> Unit
    ): ListenerRegistration {
        return db.collection("novelas").document(novelaId)
            .collection("comentarios")
            .whereEqualTo("capituloId", capituloId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull { doc ->
                    ComentarioItem(
                        id = doc.id,
                        novelaId = novelaId,
                        capituloId = capituloId,
                        usuarioId = doc.getString("usuarioId") ?: "",
                        usuarioNombre = doc.getString("usuarioNombre") ?: "",
                        usuarioFotoBase64 = doc.getString("usuarioFotoBase64") ?: "",
                        texto = doc.getString("texto") ?: ""
                    )
                }
                onCambio(lista)
            }
    }

    fun publicarComentario(
        novela: Novela,
        capituloId: String,
        usuario: UsuarioPerfil,
        texto: String
    ) {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return

        val docCom = db.collection("novelas").document(novela.id)
            .collection("comentarios").document()

        val data = hashMapOf(
            "id" to docCom.id,
            "novelaId" to novela.id,
            "capituloId" to capituloId,
            "usuarioId" to usuario.id,
            "usuarioNombre" to usuario.usuario,
            "usuarioFotoBase64" to usuario.fotoBase64,
            "texto" to limpio,
            "fecha" to fechaActualTexto()
        )

        docCom.set(data).addOnSuccessListener {
            val docNov = db.collection("novelas").document(novela.id)
            db.runTransaction { tx ->
                val snap = tx.get(docNov)
                @Suppress("UNCHECKED_CAST")
                val rawCaps = snap.get("capitulos") as? List<CapituloMap> ?: return@runTransaction
                val nuevosCaps = rawCaps.map { c ->
                    val mutable = c.toMutableMap()
                    if (c["id"] == capituloId) {
                        val actual = (c["comentariosCount"] as? Long)?.toInt() ?: 0
                        mutable["comentariosCount"] = actual + 1
                    }
                    mutable
                }
                tx.update(docNov, "capitulos", nuevosCaps)
            }

            if (novela.autorId != usuario.id) {
                crearNotificacion(
                    usuarioDestinoId = novela.autorId,
                    mensaje = "${usuario.usuario} comentó en '${novela.titulo}': \"${limpio.take(40)}\""
                )
            }
        }
    }

    // =========================================================================
    // SEGUIMIENTOS DE NOVELAS Y USUARIOS
    // =========================================================================

    fun escucharSeguimientosUsuario(
        usuarioId: String,
        onCambio: (List<SeguimientoRelacion>) -> Unit
    ): ListenerRegistration {
        return db.collection("seguimientos")
            .whereEqualTo("seguidorId", usuarioId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.map { doc ->
                    SeguimientoRelacion(
                        id = doc.id,
                        novelaId = doc.getString("novelaId") ?: "",
                        novelaTitulo = doc.getString("novelaTitulo") ?: "",
                        seguidoAutorId = doc.getString("seguidoAutorId") ?: "",
                        seguidoAutorNombre = doc.getString("seguidoAutorNombre") ?: "",
                        capituloActual = (doc.getLong("capituloActual") ?: 1L).toInt(),
                        progresoPorcentaje = (doc.getLong("progresoPorcentaje") ?: 0L).toInt(),
                        oculta = doc.getBoolean("oculta") ?: false
                    )
                }
                onCambio(lista)
            }
    }

    fun seguirNovelaYAutor(
        usuario: UsuarioPerfil,
        novela: Novela,
        ocultarDeOtros: Boolean,
        onSuccess: () -> Unit
    ) {
        val docId = "${usuario.id}_${novela.id}"
        val data = hashMapOf(
            "id" to docId,
            "seguidorId" to usuario.id,
            "novelaId" to novela.id,
            "novelaTitulo" to novela.titulo,
            "seguidoAutorId" to novela.autorId,
            "seguidoAutorNombre" to novela.autor,
            "capituloActual" to 1,
            "progresoPorcentaje" to 0,
            "oculta" to ocultarDeOtros
        )
        db.collection("seguimientos").document(docId).set(data)
            .addOnSuccessListener {
                if (novela.autorId != usuario.id) {
                    crearNotificacion(
                        usuarioDestinoId = novela.autorId,
                        mensaje = "${usuario.usuario} comenzó a seguir tu obra '${novela.titulo}'."
                    )
                }
                onSuccess()
            }
    }

    fun actualizarProgresoLectura(relacionId: String, capituloActual: Int, porcentaje: Int) {
        db.collection("seguimientos").document(relacionId).update(
            mapOf(
                "capituloActual" to capituloActual,
                "progresoPorcentaje" to porcentaje
            )
        )
    }

    fun alternarOcultarSeguimiento(relacionId: String, ocultar: Boolean) {
        db.collection("seguimientos").document(relacionId).update("oculta", ocultar)
    }

    fun eliminarSeguimiento(relacionId: String) {
        db.collection("seguimientos").document(relacionId).delete()
    }

    fun escucharUsuariosQueSigo(
        usuarioId: String,
        onCambio: (List<UsuarioSeguimiento>) -> Unit
    ): ListenerRegistration {
        return db.collection("usuarios_seguidos")
            .whereEqualTo("seguidorId", usuarioId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.map { doc ->
                    UsuarioSeguimiento(
                        id = doc.id,
                        seguidorId = doc.getString("seguidorId") ?: "",
                        seguidorNombre = doc.getString("seguidorNombre") ?: "",
                        seguidoId = doc.getString("seguidoId") ?: "",
                        seguidoNombre = doc.getString("seguidoNombre") ?: "",
                        oculta = doc.getBoolean("oculta") ?: false
                    )
                }
                onCambio(lista)
            }
    }

    fun obtenerSeguidosPublicosDeUsuario(
        usuarioId: String,
        onResult: (List<UsuarioSeguimiento>) -> Unit
    ) {
        db.collection("usuarios_seguidos")
            .whereEqualTo("seguidorId", usuarioId)
            .get()
            .addOnSuccessListener { snap ->
                val lista = snap.documents.mapNotNull { doc ->
                    val oculta = doc.getBoolean("oculta") ?: false
                    if (oculta) null
                    else UsuarioSeguimiento(
                        id = doc.id,
                        seguidorId = doc.getString("seguidorId") ?: "",
                        seguidorNombre = doc.getString("seguidorNombre") ?: "",
                        seguidoId = doc.getString("seguidoId") ?: "",
                        seguidoNombre = doc.getString("seguidoNombre") ?: "",
                        oculta = false
                    )
                }
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun seguirODejarDeSeguirUsuario(
        seguidor: UsuarioPerfil,
        objetivo: UsuarioPerfil,
        yaLoSigue: Boolean
    ) {
        if (seguidor.id == objetivo.id) return
        val docId = "${seguidor.id}_${objetivo.id}"
        val ref = db.collection("usuarios_seguidos").document(docId)
        if (yaLoSigue) {
            ref.delete()
        } else {
            val data = hashMapOf(
                "id" to docId,
                "seguidorId" to seguidor.id,
                "seguidorNombre" to seguidor.usuario,
                "seguidoId" to objetivo.id,
                "seguidoNombre" to objetivo.usuario,
                "oculta" to false
            )
            ref.set(data)
            crearNotificacion(
                usuarioDestinoId = objetivo.id,
                mensaje = "${seguidor.usuario} comenzó a seguirte."
            )
        }
    }

    fun alternarOcultarUsuarioSeguido(relacionId: String, ocultar: Boolean) {
        db.collection("usuarios_seguidos").document(relacionId).update("oculta", ocultar)
    }

    // =========================================================================
    // COMUNIDAD Y NOTIFICACIONES
    // =========================================================================

    fun escucharComunidad(
        usuarioActualId: String = "",
        onCambio: (List<PublicacionComunidad>) -> Unit
    ): ListenerRegistration {
        return db.collection("comunidad")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val ahora = System.currentTimeMillis()
                val lista = snap.documents.mapNotNull { doc ->
                    val autorId = doc.getString("autorId") ?: ""
                    val timestampPub = doc.getLong("timestamp") ?: 0L
                    if (timestampPub > ahora && autorId != usuarioActualId) return@mapNotNull null

                    PublicacionComunidad(
                        id = doc.id,
                        autorId = autorId,
                        autorNombre = doc.getString("autorNombre") ?: "",
                        autorFotoBase64 = doc.getString("autorFotoBase64") ?: "",
                        mensaje = doc.getString("mensaje") ?: "",
                        imagenBase64 = doc.getString("imagenBase64") ?: "",
                        novelaProgramadaId = doc.getString("novelaProgramadaId") ?: "",
                        novelaProgramadaTitulo = doc.getString("novelaProgramadaTitulo") ?: "",
                        fechaLanzamiento = doc.getLong("fechaLanzamiento") ?: 0L,
                        timestamp = timestampPub
                    )
                }
                onCambio(lista)
            }
    }

    fun publicarEnComunidad(
        autor: UsuarioPerfil,
        mensaje: String,
        imagenBase64: String = "",
        timestampProgramado: Long = System.currentTimeMillis(),
        novelaProgramadaId: String = "",
        novelaProgramadaTitulo: String = "",
        fechaLanzamiento: Long = 0L,
        onSuccess: () -> Unit = {}
    ) {
        val docRef = db.collection("comunidad").document()
        val data = hashMapOf(
            "id" to docRef.id,
            "autorId" to autor.id,
            "autorNombre" to autor.usuario,
            "autorFotoBase64" to autor.fotoBase64,
            "mensaje" to mensaje.trim(),
            "imagenBase64" to imagenBase64,
            "novelaProgramadaId" to novelaProgramadaId,
            "novelaProgramadaTitulo" to novelaProgramadaTitulo,
            "fechaLanzamiento" to fechaLanzamiento,
            "timestamp" to timestampProgramado
        )
        docRef.set(data).addOnSuccessListener { onSuccess() }
    }

    private fun crearNotificacion(usuarioDestinoId: String, mensaje: String) {
        val docRef = db.collection("notificaciones").document()
        val data = hashMapOf(
            "id" to docRef.id,
            "usuarioDestinoId" to usuarioDestinoId,
            "mensaje" to mensaje,
            "fecha" to fechaActualTexto(),
            "leida" to false,
            "oculta" to false
        )
        docRef.set(data)
    }

    fun escucharNotificacionesActivas(
        usuarioId: String,
        onCambio: (List<NotificacionItem>) -> Unit
    ): ListenerRegistration {
        return db.collection("notificaciones")
            .whereEqualTo("usuarioDestinoId", usuarioId)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val lista = snap.documents.mapNotNull { doc ->
                    val oculta = doc.getBoolean("oculta") ?: false
                    if (oculta) null
                    else NotificacionItem(
                        id = doc.id,
                        mensaje = doc.getString("mensaje") ?: "",
                        fecha = doc.getString("fecha") ?: "",
                        leida = doc.getBoolean("leida") ?: false
                    )
                }
                onCambio(lista)
            }
    }

    fun marcarNotificacionLeida(notificacionId: String) {
        db.collection("notificaciones").document(notificacionId).update("leida", true)
    }

    fun borradoLogicoNotificacion(notificacionId: String) {
        db.collection("notificaciones").document(notificacionId).update("oculta", true)
    }

    // =========================================================================
    // REPORTES
    // =========================================================================

    fun enviarReporte(
        reportante: UsuarioPerfil,
        tipoObjetivo: String,
        objetivoId: String,
        objetivoNombre: String,
        motivo: String,
        onSuccess: () -> Unit
    ) {
        val docRef = db.collection("reportes").document()
        val data = hashMapOf(
            "id" to docRef.id,
            "reportanteId" to reportante.id,
            "reportanteNombre" to reportante.usuario,
            "tipoObjetivo" to tipoObjetivo,
            "objetivoId" to objetivoId,
            "objetivoNombre" to objetivoNombre,
            "motivo" to motivo,
            "fecha" to fechaActualTexto()
        )
        docRef.set(data).addOnSuccessListener { onSuccess() }
    }
}