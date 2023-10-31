package com.vchetrari.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

object Compass {

    fun flowDegrees(context: Context): Flow<Float> {
        val sensorManager: SensorManager = context.getSystemService()!!
        return combine(
            accelerometerData(sensorManager),
            geomagneticData(sensorManager)
        ) { accelerometerData, geomagneticData ->
            val rotationMatrixR = FloatArray(9)
            val rotationMatrixI = FloatArray(9)
            val orientation = FloatArray(3)
            val success = SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, accelerometerData, geomagneticData)
            if (!success) return@combine null

            SensorManager.getOrientation(rotationMatrixR, orientation)
            return@combine (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
        }.filterNotNull()
    }

    private fun accelerometerData(
        sensorManager: SensorManager,
        alpha: Float = .97f
    ): Flow<FloatArray> = callbackFlow {
        val data = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                data[0] = alpha * data[0] + (1 - alpha) * event.values[0]
                data[1] = alpha * data[1] + (1 - alpha) * event.values[1]
                data[2] = alpha * data[2] + (1 - alpha) * event.values[2]

                trySend(data)
            }
        }
        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private fun geomagneticData(
        sensorManager: SensorManager,
        alpha: Float = .97f
    ): Flow<FloatArray> = callbackFlow {
        val data = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                data[0] = alpha * data[0] + (1 - alpha) * event.values[0]
                data[1] = alpha * data[1] + (1 - alpha) * event.values[1]
                data[2] = alpha * data[2] + (1 - alpha) * event.values[2]

                trySend(data)
            }
        }
        sensorManager.registerListener(
            listener,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_GAME
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}