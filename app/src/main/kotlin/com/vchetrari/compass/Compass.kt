package com.vchetrari.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.runningFold

class Compass(context: Context) {

    private val sensorManager = context.getSystemService<SensorManager>()!!
    fun azimuth(): Flow<Float> = combine(
        sensorFoldedData(Sensor.TYPE_ACCELEROMETER),
        sensorFoldedData(Sensor.TYPE_MAGNETIC_FIELD)
    ) { accelerometerData, geomagneticData ->
        val rotationMatrixR = FloatArray(9)
        val rotationMatrixI = FloatArray(9)
        val orientation = FloatArray(3)
        val success = SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, accelerometerData, geomagneticData)
        if (!success) return@combine null

        SensorManager.getOrientation(rotationMatrixR, orientation)
        return@combine (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
    }.filterNotNull()

    private fun sensorFoldedData(sensorType: Int): Flow<FloatArray> = sensorEvents(sensorType)
        .runningFold(FloatArray(3)) { data, event ->
            data[0] = ALPHA * data[0] + (1 - ALPHA) * event.values[0]
            data[1] = ALPHA * data[1] + (1 - ALPHA) * event.values[1]
            data[2] = ALPHA * data[2] + (1 - ALPHA) * event.values[2]
            data
        }
        .flowOn(Dispatchers.Default)

    private fun sensorEvents(sensorType: Int): Flow<SensorEvent> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                trySend(event)
            }
        }
        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(sensorType),
            SensorManager.SENSOR_DELAY_GAME
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val ALPHA = .97f
    }
}