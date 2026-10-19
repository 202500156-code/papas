package com.example.myapplication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARCO Y ENCABEZADO DE PÁGINA
@Composable
fun EncabezadoOrnamental(
    genero: String,
    tituloNovela: String,
    tituloCapitulo: String,
    colorAcento: Color,
    colorTexto: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, colorAcento.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "✧ ——— $genero ——— ✧",
                color = colorAcento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tituloNovela,
                style = MaterialTheme.typography.titleMedium,
                color = colorTexto,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = tituloCapitulo,
                style = MaterialTheme.typography.bodySmall,
                color = colorTexto.copy(alpha = 0.7f)
            )
        }
    }
}

// LETRA CAPITULAR Y CUERPO DE TEXTO DE LA PÁGINA
@Composable
fun TextoCapitularDecorado(
    contenido: String,
    colorAcento: Color,
    colorTexto: Color,
    tamanoTexto: Float
) {
    val textoAnnotated = buildAnnotatedString {
        if (contenido.isNotEmpty()) {
            // Primera letra gigante y estilizada
            withStyle(
                style = SpanStyle(
                    color = colorAcento,
                    fontSize = (tamanoTexto * 2.2).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            ) {
                append(contenido.take(1))
            }
            // Resto del texto del capítulo
            withStyle(
                style = SpanStyle(
                    color = colorTexto,
                    fontSize = tamanoTexto.sp,
                    fontFamily = FontFamily.Serif
                )
            ) {
                append(contenido.drop(1))
            }
        }
    }

    Text(
        text = textoAnnotated,
        lineHeight = (tamanoTexto * 1.6).sp,
        modifier = Modifier.fillMaxWidth()
    )
}