package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.ui.theme.LocalReadwoodColors
import java.io.ByteArrayOutputStream
import kotlin.math.max

@Composable
fun ImageCropperDialog(
    imageUri: Uri,
    esCircular: Boolean,
    onDismiss: () -> Unit,
    onConfirmarRecorte: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = LocalReadwoodColors.current

    val bitmapOriginal = remember(imageUri) {
        try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    if (bitmapOriginal == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val imageBitmap = remember(bitmapOriginal) { bitmapOriginal.asImageBitmap() }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (esCircular) "Ajustar Foto de Perfil" else "Ajustar Imagen de Fondo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.purple
                )
                Text(
                    text = "Arrastra para mover y pellizca o usa la barra para acercar",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(Color.Black)
                        .clipToBounds()
                        .onSizeChanged { boxSize = it }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val bmpW = bitmapOriginal.width.toFloat()
                        val bmpH = bitmapOriginal.height.toFloat()

                        val baseScale = max(canvasW / bmpW, canvasH / bmpH)
                        val totalScale = baseScale * scale

                        val drawW = bmpW * totalScale
                        val drawH = bmpH * totalScale

                        val left = (canvasW - drawW) / 2f + offset.x
                        val top = (canvasH - drawH) / 2f + offset.y

                        drawImage(
                            image = imageBitmap,
                            dstOffset = IntOffset(left.toInt(), top.toInt()),
                            dstSize = IntSize(drawW.toInt(), drawH.toInt())
                        )

                        if (esCircular) {
                            val radius = minOf(canvasW, canvasH) * 0.44f
                            drawCircle(
                                color = Color.White,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        } else {
                            val rectW = canvasW * 0.96f
                            val rectH = rectW * 0.56f
                            drawRect(
                                color = Color.White,
                                topLeft = Offset((canvasW - rectW) / 2f, (canvasH - rectH) / 2f),
                                size = Size(rectW, rectH),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zoom", fontSize = 12.sp, color = colors.textDark)
                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 1f..4f,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.purple,
                            activeTrackColor = colors.purple
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = colors.textMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = colors.purple),
                        onClick = {
                            val base64Final = generarRecorteBase64(
                                original = bitmapOriginal,
                                boxSize = boxSize,
                                scale = scale,
                                offset = offset,
                                esCircular = esCircular
                            )
                            onConfirmarRecorte(base64Final)
                        }
                    ) {
                        Text("Guardar ajuste", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun generarRecorteBase64(
    original: Bitmap,
    boxSize: IntSize,
    scale: Float,
    offset: Offset,
    esCircular: Boolean
): String {
    if (boxSize.width == 0 || boxSize.height == 0) return ""
    val canvasW = boxSize.width.toFloat()
    val canvasH = boxSize.height.toFloat()

    val outW = if (esCircular) 450 else 960
    val outH = if (esCircular) 450 else 540

    val cropW = if (esCircular) minOf(canvasW, canvasH) * 0.88f else canvasW * 0.96f
    val cropH = if (esCircular) cropW else cropW * 0.56f

    val cropLeft = (canvasW - cropW) / 2f
    val cropTop = (canvasH - cropH) / 2f

    val bmpW = original.width.toFloat()
    val bmpH = original.height.toFloat()
    val baseScale = max(canvasW / bmpW, canvasH / bmpH)
    val totalScale = baseScale * scale

    val drawLeft = (canvasW - bmpW * totalScale) / 2f + offset.x
    val drawTop = (canvasH - bmpH * totalScale) / 2f + offset.y

    val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    canvas.drawColor(android.graphics.Color.BLACK)

    val factorSalida = outW / cropW
    val matrix = Matrix().apply {
        postScale(totalScale * factorSalida, totalScale * factorSalida)
        postTranslate((drawLeft - cropLeft) * factorSalida, (drawTop - cropTop) * factorSalida)
    }

    canvas.drawBitmap(original, matrix, Paint(Paint.FILTER_BITMAP_FLAG))

    val baos = ByteArrayOutputStream()
    result.compress(Bitmap.CompressFormat.JPEG, 82, baos)
    return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
}