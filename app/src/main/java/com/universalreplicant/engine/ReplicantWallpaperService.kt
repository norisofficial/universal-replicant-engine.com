package com.universalreplicant.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import android.util.Log
import kotlin.math.max
import kotlin.math.min

class ReplicantWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return ReplicantEngine()
    }

    private fun loadPhoto(): Bitmap? {
        val savedUri = getSharedPreferences(
            "replicant_settings",
            Context.MODE_PRIVATE
        ).getString("photo_uri", null) ?: return null

        return try {
            val uri = Uri.parse(savedUri)

            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sampleSize = 1
            while (
                bounds.outWidth / sampleSize > 2048 ||
                bounds.outHeight / sampleSize > 2048
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            Log.e("ReplicantEngine", "Could not load imported photo", e)
            null
        }
    }

    private inner class ReplicantEngine :
        Engine(), SensorEventListener {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val accelerometer =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        private var photo: Bitmap? = loadPhoto()
        private var surfaceReady = false
        private var visibleNow = false

        private var tiltX = 0f
        private var tiltY = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            Log.i("ReplicantEngine", "Wallpaper engine created")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceReady = true
            drawFrame()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceReady = true
            drawFrame()
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            visibleNow = visible

            if (visible) {
                accelerometer?.let {
                    sensorManager.registerListener(
                        this,
                        it,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }
                drawFrame()
            } else {
                sensorManager.unregisterListener(this)
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

            tiltX = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
            tiltY = (event.values[1] / 9.81f).coerceIn(-1f, 1f)

            drawFrame()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // No action needed.
        }

        private fun drawFrame() {
            if (!surfaceReady || !visibleNow) return

            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas() ?: return

                val width = canvas.width.toFloat()
                val height = canvas.height.toFloat()

                canvas.drawColor(Color.rgb(8, 10, 18))

                val bitmap = photo

                if (bitmap == null || bitmap.isRecycled) {
                    paint.color = Color.WHITE
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = 38f

                    canvas.drawText(
                        "UNIVERSAL REPLICANT",
                        width / 2f,
                        height / 2f,
                        paint
                    )

                    paint.textSize = 22f
                    canvas.drawText(
                        "Import a photograph in the app",
                        width / 2f,
                        height / 2f + 45f,
                        paint
                    )
                } else {
                    val bitmapWidth = bitmap.width.toFloat()
                    val bitmapHeight = bitmap.height.toFloat()

                    val canvasRatio = width / height
                    val bitmapRatio = bitmapWidth / bitmapHeight

                    val sourceWidth: Float
                    val sourceHeight: Float

                    if (bitmapRatio > canvasRatio) {
                        sourceHeight = bitmapHeight
                        sourceWidth = bitmapHeight * canvasRatio
                    } else {
                        sourceWidth = bitmapWidth
                        sourceHeight = bitmapWidth / canvasRatio
                    }

                    val extraX = bitmapWidth - sourceWidth
                    val extraY = bitmapHeight - sourceHeight

                    val maxShiftX = min(extraX / 2f, sourceWidth * 0.025f)
                    val maxShiftY = min(extraY / 2f, sourceHeight * 0.025f)

                    val shiftX = -tiltX * maxShiftX
                    val shiftY = tiltY * maxShiftY

                    val left = ((bitmapWidth - sourceWidth) / 2f + shiftX)
                        .coerceIn(0f, extraX)

                    val top = ((bitmapHeight - sourceHeight) / 2f + shiftY)
                        .coerceIn(0f, extraY)

                    val source = Rect(
                        left.toInt(),
                        top.toInt(),
                        (left + sourceWidth).toInt().coerceAtMost(bitmap.width),
                        (top + sourceHeight).toInt().coerceAtMost(bitmap.height)
                    )

                    val destination = RectF(0f, 0f, width, height)

                    paint.color = Color.WHITE
                    canvas.drawBitmap(
                        bitmap,
                        source,
                        destination,
                        paint
                    )
                }
            } catch (e: Exception) {
                Log.e("ReplicantEngine", "Wallpaper drawing failed", e)
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        override fun onDestroy() {
            sensorManager.unregisterListener(this)
            super.onDestroy()
        }
    }
}
