package com.universalreplicant.engine

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class ReplicantWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        Log.i("ReplicantWallpaper", "ENGINE CREATED")
        return ReplicantEngine()
    }

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

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            Log.i(
                "ReplicantWallpaper",
                "ENGINE ONCREATE preview=${isPreview}"
            )
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            Log.i("ReplicantWallpaper", "SURFACE CREATED")
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
            Log.i(
                "ReplicantWallpaper",
                "SURFACE CHANGED ${width}x${height}"
            )
            draw()
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            super.onSurfaceRedrawNeeded(holder)
            surfaceReady = true
            Log.i("ReplicantWallpaper", "REDRAW REQUESTED")
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            Log.i("ReplicantWallpaper", "SURFACE DESTROYED")
            surfaceReady = false
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            super.onVisibilityChanged(isVisible)

            visible = isVisible

            Log.i(
                "ReplicantWallpaper",
                "VISIBILITY=$isVisible preview=${isPreview}"
            )

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
            Log.i("ReplicantWallpaper", "ENGINE DESTROYED")
            visible = false
            surfaceReady = false
            sensors.unregisterListener(this)
            super.onDestroy()
        }

        private fun draw() {
            if (!surfaceReady || !visible) {
                Log.i(
                    "ReplicantWallpaper",
                    "DRAW SKIPPED surface=$surfaceReady visible=$visible"
                )
                return
            }

            var canvas: Canvas? = null

            try {
                canvas = surfaceHolder.lockCanvas() ?: return

                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()

                canvas.drawColor(Color.rgb(8, 12, 35))

                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(0, 180, 255)

                canvas.drawCircle(
                    w * (0.5f + dx),
                    h * (0.40f + dy),
                    minOf(w, h) * 0.28f,
                    paint
                )

                paint.color = Color.rgb(255, 40, 180)

                canvas.drawCircle(
                    w * (0.48f + dx * 1.5f),
                    h * (0.42f + dy * 1.5f),
                    minOf(w, h) * 0.15f,
                    paint
                )

                paint.color = Color.WHITE
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
                paint.textSize = maxOf(28f, w * 0.065f)

                canvas.drawText(
                    "UNIVERSAL REPLICANT",
                    w / 2f,
                    h * 0.78f,
                    paint
                )

                paint.textSize = maxOf(20f, w * 0.04f)

                canvas.drawText(
                    if (isPreview) "PREVIEW MODE" else "ACTIVE WALLPAPER",
                    w / 2f,
                    h * 0.84f,
                    paint
                )

                Log.i(
                    "ReplicantWallpaper",
                    "FRAME DRAWN preview=$isPreview"
                )

            } catch (e: Exception) {
                Log.e(
                    "ReplicantWallpaper",
                    "DRAW FAILED",
                    e
                )
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        Log.e(
                            "ReplicantWallpaper",
                            "CANVAS RELEASE FAILED",
                            e
                        )
                    }
                }
            }
        }
    }
}
