package com.example.myapplication

object ProfanityFilter {
    private val palabrasProhibidas = listOf(
        "puto", "puta", "pendejo", "pendeja", "cabron", "cabrón", "chingar",
        "chingada", "verga", "culero", "culera", "mierda", "joder", "coño",
        "maricon", "maricón", "perra", "bastardo", "idiota", "estupido",
        "estúpido", "malparido", "gonorrea", "fuck", "shit", "bitch", "asshole"
    )

    fun censurar(texto: String): String {
        if (texto.isBlank()) return texto
        var resultado = texto
        for (palabra in palabrasProhibidas) {
            val regex = Regex("\\b${Regex.escape(palabra)}\\b", RegexOption.IGNORE_CASE)
            resultado = regex.replace(resultado) { match ->
                "*".repeat(match.value.length)
            }
        }
        return resultado
    }
}