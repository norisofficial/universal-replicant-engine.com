package com.universalreplicant.engine

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var statusText: TextView

    private val wallpaperComponent: ComponentName
        get() = ComponentName(
            this,
            ReplicantWallpaperService::class.java
        )

    companion object {
        private const val PREVIEW_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        statusText = TextView(this).apply {
            text = "UNIVERSAL REPLICANT ENGINE\n\nReady."
            textSize = 17f
        }

        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "OPEN WALLPAPER PREVIEW"

            setOnClickListener {
                openPreview()
            }
        })

        layout.addView(Button(this).apply {
            text = "CHECK WALLPAPER STATUS"

            setOnClickListener {
                checkWallpaper("Manual status check")
            }
        })

        setContentView(layout)
    }

    private fun openPreview() {
        try {
            statusText.text =
                "Opening Samsung wallpaper preview..."

            val intent = Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            ).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                wallpaperComponent
            )

            startActivityForResult(
                intent,
                PREVIEW_REQUEST
            )

        } catch (e: Exception) {
            statusText.text =
                "PREVIEW LAUNCH FAILED\n\n${e.javaClass.simpleName}\n${e.message}"
        }
    }

    @Deprecated("Diagnostic result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PREVIEW_REQUEST) {
            val result = when (resultCode) {
                RESULT_OK -> "RESULT_OK"
                RESULT_CANCELED -> "RESULT_CANCELED"
                else -> "RESULT CODE: $resultCode"
            }

            checkWallpaper(
                "Samsung preview returned: $result"
            )
        }
    }

    private fun checkWallpaper(message: String) {
        try {
            val manager = WallpaperManager.getInstance(this)
            val active = manager.wallpaperInfo

            val activeDescription = when {
                active == null ->
                    "No live wallpaper reported."

                active.packageName == wallpaperComponent.packageName &&
                active.serviceName == wallpaperComponent.className ->
                    "UNIVERSAL REPLICANT ENGINE IS ACTIVE."

                else ->
                    "Another wallpaper is active.\n" +
                    "Package: ${active.packageName}\n" +
                    "Service: ${active.serviceName}"
            }

            statusText.text =
                "ACTIVATION DIAGNOSTIC\n\n" +
                "$message\n\n" +
                activeDescription

        } catch (e: Exception) {
            statusText.text =
                "STATUS CHECK FAILED\n\n" +
                "${e.javaClass.simpleName}\n${e.message}"
        }
    }
}
