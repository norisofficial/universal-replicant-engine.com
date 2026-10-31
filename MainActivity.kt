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
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val layout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,48,32,32) }
  layout.addView(TextView(this).apply { text="Universal Replicant Engine\n\nNative Android wallpaper starter. This build verifies the wallpaper and motion pipeline; AI photo/video reconstruction is not implemented yet."; textSize=18f })
  layout.addView(Button(this).apply { text="Preview / set wallpaper"; setOnClickListener {
   startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@MainActivity, ReplicantWallpaperService::class.java)))
  }})
  setContentView(layout)
 }
}