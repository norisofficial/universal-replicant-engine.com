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

    private val wallpaperComponent
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
            text = "Universal Replicant Engine\n\n"
            textSize = 18f
        }

        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "Preview / set wallpaper"
            setOnClickListener {
                val intent = Intent(
                    WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                ).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    wallpaperComponent
                )

                startActivity(intent)
            }
        })

        layout.addView(Button(this).apply {
            text = "Check active wallpaper"
            setOnClickListener {
                try {
                    val manager = WallpaperManager.getInstance(this@MainActivity)
                    val active = manager.wallpaperInfo

                    statusText.text = if (active == null) {
                        "No live wallpaper is currently active."
                    } else if (active.component == wallpaperComponent) {
                        "Universal Replicant Engine is ACTIVE."
                    } else {
                        "Another live wallpaper is active:\n${active.serviceName}"
                    }

                    Toast.makeText(
                        this@MainActivity,
                        statusText.text,
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: Exception) {
                    statusText.text =
                        "Could not check wallpaper: ${e.message}"
                }
            }
        })

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        if (::statusText.isInitialized) {
            statusText.text = "Universal Replicant Engine\n\nTap Check active wallpaper."
        }
    }
}
