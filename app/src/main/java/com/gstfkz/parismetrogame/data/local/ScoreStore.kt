package com.gstfkz.parismetrogame.data.local
import android.content.Context
import com.gstfkz.parismetrogame.data.model.ScoreEntry
class ScoreStore(context:Context){
 private val prefs=context.getSharedPreferences("scores",Context.MODE_PRIVATE)
 var currentScore:Int get()=prefs.getInt("current",0); private set(v){prefs.edit().putInt("current",v).apply()}
 fun win():Int{currentScore+=1;return currentScore}
 fun publishAndReset(){val s=currentScore;if(s>0){val raw=prefs.getStringSet("history",emptySet())!!.toMutableSet();raw+=System.currentTimeMillis().toString()+":"+s;prefs.edit().putStringSet("history",raw).putInt("current",0).apply()}else currentScore=0}
 fun discardCurrent(){currentScore=0}
 fun history():List<ScoreEntry> = prefs.getStringSet("history",emptySet()).orEmpty().mapNotNull{v->val p=v.split(":");if(p.size==2)ScoreEntry(p[1].toIntOrNull()?:return@mapNotNull null,p[0].toLongOrNull()?:return@mapNotNull null)else null}.sortedByDescending{it.timestamp}
 fun lastPublishedScore():Int?=history().firstOrNull()?.score
 fun bestPublishedScore():Int?=history().maxOfOrNull{it.score}
}
