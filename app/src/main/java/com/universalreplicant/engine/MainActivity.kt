package com.universalreplicant.engine

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
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
        private const val PHOTO_REQUEST = 2001
        private const val PREFS = "replicant_settings"
        private const val PHOTO_URI = "photo_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        statusText = TextView(this).apply {
            textSize = 18f
            text = "UNIVERSAL REPLICANT ENGINE\n\n" +
                "Import a photograph to begin."
        }

        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "IMPORT PHOTOGRAPH"

            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }

                startActivityForResult(intent, PHOTO_REQUEST)
            }
        })

        layout.addView(Button(this).apply {
            text = "OPEN WALLPAPER PREVIEW"

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

        layout.addView(Button(this).apply {
            text = "CHECK IMPORTED PHOTOGRAPH"

            setOnClickListener {
                val uri = getSharedPreferences(
                    PREFS,
                    MODE_PRIVATE
                ).getString(PHOTO_URI, null)

                statusText.text = if (uri == null) {
                    "No photograph imported yet."
                } else {
                    "Photograph imported successfully.\n\n" +
                    "Ready for the scene-rendering stage."
                }
            }
        })

        setContentView(layout)

        val saved = getSharedPreferences(
            PREFS,
            MODE_PRIVATE
        ).getString(PHOTO_URI, null)

        if (saved != null) {
            statusText.text =
                "UNIVERSAL REPLICANT ENGINE\n\n" +
                "A photograph is already imported."
        }
    }

    @Deprecated("Photo picker result callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PHOTO_REQUEST) {
            if (resultCode != RESULT_OK) {
                statusText.text = "Photo selection cancelled."
                return
            }

            val uri: Uri = data?.data ?: run {
                statusText.text = "No photograph was selected."
                return
            }

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                getSharedPreferences(
                    PREFS,
                    MODE_PRIVATE
                ).edit()
                    .putString(PHOTO_URI, uri.toString())
                    .apply()

                statusText.text =
                    "PHOTOGRAPH IMPORTED SUCCESSFULLY!\n\n" +
                    "Your photograph is saved for the next " +
                    "scene-rendering development stage."

            } catch (e: Exception) {
                statusText.text =
                    "IMPORT FAILED\n\n${e.message}"
            }
        }
    }
}
