package com.universalreplicant.engine

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ReplicantWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = ReplicantEngine()

    inner class ReplicantEngine : Engine(), SensorEventListener {

        private val sensors by lazy {
            getSystemService(SENSOR_SERVICE) as SensorManager
        }

        private val rotation by lazy {
            sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var dx = 0f
        private var dy = 0f
        private var visible = false
        private var surfaceReady = false

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            draw()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceReady = true
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible

            if (visible) {
                rotation?.let {
                    sensors.registerListener(
                        this,
                        it,
                        SensorManager.SENSOR_DELAY_UI
                    )
                }
                draw()
            } else {
                sensors.unregisterListener(this)
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            val matrix = FloatArray(9)

            SensorManager.getRotationMatrixFromVector(
                matrix,
                event.values
            )

            dx = matrix[2].coerceIn(-0.12f, 0.12f)
            dy = matrix[5].coerceIn(-0.12f, 0.12f)

            if (visible) draw()
        }

        override fun onAccuracyChanged(
            sensor: Sensor?,
            accuracy: Int
        ) {}

        override fun onDestroy() {
            visible = false
            surfaceReady = false
            sensors.unregisterListener(this)
            super.onDestroy()
        }

        private fun draw() {
            if (!surfaceReady || !visible) return

            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas() ?: return

                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()

                canvas.drawColor(Color.rgb(10, 15, 28))

                paint.color = Color.rgb(25, 42, 68)
                canvas.drawCircle(
                    w * (0.5f + dx),
                    h * (0.42f + dy),
                    minOf(w, h) * 0.34f,
                    paint
                )

                paint.color = Color.rgb(55, 85, 115)
                canvas.drawCircle(
                    w * (0.48f + dx * 1.5f),
                    h * (0.4f + dy * 1.5f),
                    minOf(w, h) * 0.2f,
                    paint
                )

                paint.color = Color.WHITE
                paint.textSize = maxOf(24f, w * 0.055f)

                canvas.drawText(
                    "UNIVERSAL REPLICANT",
                    36f,
                    h * 0.82f,
                    paint
                )

                paint.textSize = maxOf(16f, w * 0.035f)

                canvas.drawText(
                    "Native wallpaper motion test",
                    36f,
                    h * 0.87f,
                    paint
                )

            } catch (e: Exception) {
                Log.e("ReplicantWallpaper", "Wallpaper drawing failed", e)
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e("ReplicantWallpaper", "Canvas release failed", e)
                    }
                }
            }
        }
    }
}
