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
class ReplicantWallpaperService : WallpaperService() {
 override fun onCreateEngine(): Engine = ReplicantEngine()
 inner class ReplicantEngine : Engine(), SensorEventListener {
  private val sensors by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
  private val rotation by lazy { sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var dx=0f; private var dy=0f; private var visible=false
  override fun onVisibilityChanged(v:Boolean) {
   visible=v
   if(v) { rotation?.let { sensors.registerListener(this,it,SensorManager.SENSOR_DELAY_UI) }; draw() }
   else sensors.unregisterListener(this)
  }
  override fun onSensorChanged(e:SensorEvent) {
   val m=FloatArray(9); SensorManager.getRotationMatrixFromVector(m,e.values)
   dx=m[2].coerceIn(-0.12f,0.12f); dy=m[5].coerceIn(-0.12f,0.12f)
   if(visible) draw()
  }
  override fun onAccuracyChanged(s:Sensor?,a:Int) {}
  override fun onSurfaceChanged(h:SurfaceHolder,f:Int,w:Int,hg:Int) { super.onSurfaceChanged(h,f,w,hg); draw() }
  override fun onDestroy() { sensors.unregisterListener(this); super.onDestroy() }
  private fun draw() {
   var c:Canvas?=null
   try { c=surfaceHolder.lockCanvas(); if(c!=null) {
    val w=c.width.toFloat(); val h=c.height.toFloat()
    c.drawColor(Color.rgb(10,15,28))
    paint.color=Color.rgb(25,42,68); c.drawCircle(w*(.5f+dx),h*(.42f+dy),kotlin.math.min(w,h)*.34f,paint)
    paint.color=Color.rgb(55,85,115); c.drawCircle(w*(.48f+dx*1.5f),h*(.4f+dy*1.5f),kotlin.math.min(w,h)*.2f,paint)
    paint.color=Color.WHITE; paint.textSize=kotlin.math.max(24f,w*.055f)
    c.drawText("UNIVERSAL REPLICANT",36f,h*.82f,paint)
    paint.textSize=kotlin.math.max(16f,w*.035f); c.drawText("Native wallpaper motion test",36f,h*.87f,paint)
   }} finally { if(c!=null) surfaceHolder.unlockCanvasAndPost(c) }
  }
 }
}