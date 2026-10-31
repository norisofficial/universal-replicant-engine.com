package com.universalreplicant.engine

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

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
            text = "Universal Replicant Engine\n\nChoose how to open your wallpaper."
            textSize = 18f
        }

        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "Open my wallpaper preview"

            setOnClickListener {
                val intent = Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                ).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    wallpaperComponent
                )

                openIntent(intent)
            }
        })

        layout.addView(Button(this).apply {
            text = "Open system live wallpaper chooser"

            setOnClickListener {
                val intent = Intent(
                    WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
                )

                openIntent(intent)
            }
        })

        layout.addView(Button(this).apply {
            text = "Check active wallpaper"

            setOnClickListener {
                try {
                    val active =
                        WallpaperManager.getInstance(this@MainActivity)
                            .wallpaperInfo

                    statusText.text = when {
                        active == null ->
                            "No live wallpaper is currently active."

                        active.packageName == wallpaperComponent.packageName &&
                        active.serviceName == wallpaperComponent.className ->
                            "Universal Replicant Engine is ACTIVE!"

                        else ->
                            "Another live wallpaper is active.\nPackage: ${active.packageName}\nService: ${active.serviceName}"
                    }

                } catch (e: Exception) {
                    statusText.text =
                        "Check failed: ${e.message}"
                }
            }
        })

        setContentView(layout)
    }

    private fun openIntent(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not open wallpaper screen: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
