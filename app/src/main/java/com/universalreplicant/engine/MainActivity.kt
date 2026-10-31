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

    private val wallpaperAction =
        "android.service.wallpaper.WallpaperService"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        layout.addView(TextView(this).apply {
            text = "Universal Replicant Engine\n\nWallpaper service diagnostic."
            textSize = 18f
        })

        layout.addView(Button(this).apply {
            text = "Check and preview wallpaper"

            setOnClickListener {
                val service = ComponentName(
                    this@MainActivity,
                    ReplicantWallpaperService::class.java
                )

                val serviceIntent = Intent(wallpaperAction)
                    .setComponent(service)

                val found = packageManager.resolveService(
                    serviceIntent,
                    0
                )

                if (found == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "ERROR: Android cannot find the wallpaper service.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    try {
                        val previewIntent = Intent(
                            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                        ).putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            service
                        )

                        startActivity(previewIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Preview error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })

        setContentView(layout)
    }
}
