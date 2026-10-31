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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        statusText = TextView(this).apply {
            text = "UNIVERSAL REPLICANT ENGINE\n\nRun the wallpaper diagnostic."
            textSize = 17f
        }

        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "RUN WALLPAPER DIAGNOSTIC"

            setOnClickListener {
                runDiagnostic()
            }
        })

        layout.addView(Button(this).apply {
            text = "OPEN MY WALLPAPER PREVIEW"

            setOnClickListener {
                try {
                    val intent = Intent(
                        WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                    ).putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        wallpaperComponent
                    )

                    startActivity(intent)

                } catch (e: Exception) {
                    statusText.text =
                        "Could not open preview:\n${e.message}"
                }
            }
        })

        setContentView(layout)
    }

    private fun runDiagnostic() {
        try {
            val manager = WallpaperManager.getInstance(this)

            val supported = manager.isWallpaperSupported()
            val allowed = manager.isSetWallpaperAllowed()
            val active = manager.wallpaperInfo

            val activeText = if (active == null) {
                "No live wallpaper reported."
            } else {
                "Package: ${active.packageName}\n" +
                "Service: ${active.serviceName}"
            }

            statusText.text =
                "WALLPAPER DIAGNOSTIC\n\n" +
                "Wallpaper supported: $supported\n\n" +
                "Wallpaper changes allowed: $allowed\n\n" +
                "Currently active:\n$activeText"

        } catch (e: Exception) {
            statusText.text =
                "DIAGNOSTIC FAILED\n\n${e.javaClass.simpleName}\n${e.message}"
        }
    }
}
