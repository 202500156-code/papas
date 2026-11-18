package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun PerfilScreen(
    contador: Int,
    velocidadMultiplicador: Float,
    onCambiarVelocidad: (Float) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    var ocultarSeguidos by remember { mutableStateOf(false) }

    // --- ESTADOS FÍSICOS (GRAVEDAD / AGITACIÓN Y SALTO POR VOZ) ---
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetYGravedad by remember { mutableFloatStateOf(0f) }
    var saltoAudio by remember { mutableFloatStateOf(0f) }

    // Suavizado de movimiento físico
    val animX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "x"
    )
    val animY by animateFloatAsState(
        targetValue = offsetYGravedad - saltoAudio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "y"
    )

    // --- PERMISO DE MICRÓFONO ---
    var tienePermisoAudio by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoAudio = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoAudio) {
            launcherPermiso.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]

                    val aceleracionTotal = sqrt(x * x + y * y + z * z)
                    val factorAgitacion = if (aceleracionTotal > 14f) 2.5f else 1f

                    offsetX = (-x * 12f * factorAgitacion).coerceIn(-110f, 110f)
                    offsetYGravedad = ((y - 5f) * 6f).coerceIn(-40f, 40f)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (acelerometro != null) {
            sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(tienePermisoAudio) {
        if (tienePermisoAudio) {
            withContext(Dispatchers.IO) {
                val sampleRate = 44100
                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                if (bufferSize > 0) {
                    try {
                        val audioRecord = AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize
                        )

                        val buffer = ShortArray(bufferSize)
                        audioRecord.startRecording()

                        while (isActive) {
                            val leidos = audioRecord.read(buffer, 0, bufferSize)
                            if (leidos > 0) {
                                var maxAmplitud = 0
                                for (i in 0 until leidos) {
                                    val valor = abs(buffer[i].toInt())
                                    if (valor > maxAmplitud) maxAmplitud = valor
                                }

                                withContext(Dispatchers.Main) {
                                    saltoAudio = if (maxAmplitud > 3500) {
                                        ((maxAmplitud - 3500) / 180f).coerceIn(25f, 95f)
                                    } else {
                                        0f
                                    }
                                }
                            }
                            delay(50L)
                        }

                        audioRecord.stop()
                        audioRecord.release()
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    val rotacion = remember { Animatable(0f) }

    LaunchedEffect(velocidadMultiplicador) {
        val duracionVuelta = (2000f / velocidadMultiplicador).toInt().coerceAtLeast(100)
        rotacion.animateTo(
            targetValue = rotacion.value + 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duracionVuelta, easing = LinearEasing)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadwoodDarkBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mi Perfil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ReadwoodDarkTextPrimary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Indicador Interactivo",
                    color = ReadwoodDarkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Agita o inclina para mover • Grita para saltar",
                    color = ReadwoodDarkTextPrimary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val anchoOvalo = 70.dp.toPx()
                    val altoOvalo = 35.dp.toPx()

                    val centroDinamico = Offset(
                        x = (center.x + animX).coerceIn(anchoOvalo, size.width - anchoOvalo),
                        y = (center.y + 20f + animY).coerceIn(altoOvalo, size.height - altoOvalo)
                    )

                    rotate(degrees = rotacion.value % 360f, pivot = centroDinamico) {
                        drawRoundRect(
                            color = ReadwoodDarkPrimary,
                            topLeft = Offset(
                                centroDinamico.x - (anchoOvalo / 2),
                                centroDinamico.y - (altoOvalo / 2)
                            ),
                            size = Size(anchoOvalo, altoOvalo),
                            cornerRadius = CornerRadius(altoOvalo / 2, altoOvalo / 2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Contador: $contador",
                    color = ReadwoodDarkTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Velocidad actual: ${velocidadMultiplicador.toInt()}x",
                    color = ReadwoodDarkTextPrimary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1f, 2f, 4f, 8f).forEach { v ->
                        FilterChip(
                            selected = velocidadMultiplicador == v,
                            onClick = { onCambiarVelocidad(v) },
                            label = { Text("${v.toInt()}x") }
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = ReadwoodDarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Privacidad", color = ReadwoodDarkPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ocultar a quién sigo", color = ReadwoodDarkTextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = ocultarSeguidos,
                        onCheckedChange = { ocultarSeguidos = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCerrarSesion,
            colors = ButtonDefaults.buttonColors(containerColor = ReadwoodDarkButton),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Cerrar Sesión")
        }
    }
}