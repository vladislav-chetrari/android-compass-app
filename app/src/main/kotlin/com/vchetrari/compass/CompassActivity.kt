package com.vchetrari.compass

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest

class CompassActivity : AppCompatActivity() {

    private val compass by lazy { Compass(this) }
    private val cardinalDirection by lazy { CardinalDirection(resources) }
    private val compassNeedle: ImageView by lazy { findViewById(R.id.compassNeedle) }
    private val cardinalDirectionLabel: TextView by lazy { findViewById(R.id.cardinalDirectionLabel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)
        lifecycleScope.launchWhenStarted {
            compass.azimuth().collectLatest { azimuth ->
                cardinalDirectionLabel.text = cardinalDirection.labelOf(azimuth)
                adjustArrow(azimuth)
            }
        }
    }

    private fun adjustArrow(azimuth: Float) {
        val animation = RotateAnimation(
            -azimuth,
            -azimuth,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        animation.duration = 500
        animation.repeatCount = 0
        animation.fillAfter = true
        compassNeedle.startAnimation(animation)
    }
}