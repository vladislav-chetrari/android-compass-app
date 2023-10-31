package com.vchetrari.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService

class Compass(context: Context) : SensorEventListener {


    private val sensorManager: SensorManager = context.getSystemService()!!
    private val accelerometerSensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val geomagneticSensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val accelerometerData = FloatArray(3)
    private val geomagneticData = FloatArray(3)
    private val rotationMatrixR = FloatArray(9)
    private val rotationMatrixI = FloatArray(9)

    var listener: Listener? = null

    fun start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    //TODO kotlin flows
    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerData[0] = alpha * accelerometerData[0] + (1 - alpha) * event.values[0]
                accelerometerData[1] = alpha * accelerometerData[1] + (1 - alpha) * event.values[1]
                accelerometerData[2] = alpha * accelerometerData[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagneticData[0] = alpha * geomagneticData[0] + (1 - alpha) * event.values[0]
                geomagneticData[1] = alpha * geomagneticData[1] + (1 - alpha) * event.values[1]
                geomagneticData[2] = alpha * geomagneticData[2] + (1 - alpha) * event.values[2]
            }
            if (SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, accelerometerData, geomagneticData)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrixR, orientation)
                listener?.onNewAzimuth(
                    (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    interface Listener {
        fun onNewAzimuth(azimuth: Float)
    }
}