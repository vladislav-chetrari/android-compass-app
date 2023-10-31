package com.vchetrari.compass

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CompassActivity : AppCompatActivity(), Compass.Listener {

    private val compass by lazy { Compass(this) }

    private val compassNeedle: ImageView by lazy { findViewById(R.id.compassNeedle) }
    private val cardinalDirectionLabel: TextView by lazy { findViewById(R.id.cardinalDirectionLabel) }
    private var currentAzimuth = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        compass.listener = this
    }

    override fun onStart() {
        super.onStart()
        compass.start()
    }

    override fun onStop() {
        compass.stop()
        super.onStop()
    }

    override fun onDestroy() {
        compass.listener = null
        super.onDestroy()
    }

    private fun adjustArrow(azimuth: Float) {
        val animation = RotateAnimation(
            -currentAzimuth,
            -azimuth,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        animation.duration = 500
        animation.repeatCount = 0
        animation.fillAfter = true
        currentAzimuth = azimuth
        compassNeedle.startAnimation(animation)
    }

    override fun onNewAzimuth(azimuth: Float) {
        runOnUiThread {
            cardinalDirectionLabel.text = GetCardinalDirectionLabelUseCase(this, azimuth)
            adjustArrow(azimuth)
        }
    }
}