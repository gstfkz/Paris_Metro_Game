package com.gstfkz.parismetrogame
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.gstfkz.parismetrogame.data.local.ScoreStore
class ExitTrackingService:Service(){override fun onBind(intent:Intent?):IBinder?=null;override fun onStartCommand(intent:Intent?,flags:Int,startId:Int)=START_NOT_STICKY;override fun onTaskRemoved(rootIntent:Intent?){ScoreStore(applicationContext).publishAndReset();stopSelf();super.onTaskRemoved(rootIntent)}}
