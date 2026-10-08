package com.gstfkz.parismetrogame.ui.viewmodel
import android.app.Application
import androidx.lifecycle.*
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.local.*
import com.gstfkz.parismetrogame.data.model.*
import com.gstfkz.parismetrogame.data.network.*
import com.gstfkz.parismetrogame.data.repository.MetroRepository
import com.gstfkz.parismetrogame.util.ItineraryComparator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameViewModel(app:Application,private val repository:MetroRepository):AndroidViewModel(app){
 private val store=ScoreStore(app);private val settingsStore=GameSettingsStore(app);private val _prefs=MutableStateFlow(settingsStore.get());val prefs=_prefs.asStateFlow()
 private val _screen=MutableStateFlow<GameScreen>(GameScreen.Menu);val screen=_screen.asStateFlow();private var network:MetroNetwork?=null
 private val _score=MutableStateFlow(store.currentScore);val score=_score.asStateFlow();private val _update=MutableStateFlow<UpdateInfo?>(null);val update=_update.asStateFlow()
 private var countdownJob:Job?=null
 val availableLines:List<MetroLine> get(){val p=_prefs.value;val lines=network?.lines.orEmpty().filter{it.mode==TransportMode.METRO||(it.mode==TransportMode.RER&&p.rerEnabled)};return (if(p.walkEnabled)listOf(MetroLine.WALK) else emptyList())+lines}
 fun stationsForLine(line:MetroLine)=if(line.mode==TransportMode.WALK)network?.let{repository.allStations(it,_prefs.value.rerEnabled)}.orEmpty().sortedBy{it.name.lowercase()} else network?.stationsByLine?.get(line.id).orEmpty()
 fun startNewGame(){countdownJob?.cancel();_screen.value=GameScreen.Loading;viewModelScope.launch{try{val n=network?:repository.loadNetwork().also{network=it};val(a,b)=repository.pickRandomStationPair(n,_prefs.value.rerEnabled);val remaining=if(_prefs.value.countdownEnabled)_prefs.value.countdownTotalSeconds else null;_screen.value=GameScreen.Playing(a,b,listOf(UserSegment()),remaining);startCountdownIfNeeded()}catch(e:Exception){_screen.value=GameScreen.Error(e.message?:"Unable to load the network.")}}}
 private fun startCountdownIfNeeded(){val c=_screen.value as? GameScreen.Playing?:return;val initial=c.remainingSeconds?:return;countdownJob?.cancel();countdownJob=viewModelScope.launch{var left=initial;while(left>0){delay(1000);val current=_screen.value as? GameScreen.Playing?:return@launch;left--;_screen.value=current.copy(remainingSeconds=left)};timeoutLoss()}}
 private fun timeoutLoss(){val c=_screen.value as? GameScreen.Playing?:return;countdownJob?.cancel();_screen.value=GameScreen.Loading;viewModelScope.launch{finishGame(c,forcedLoss=true)}}
 fun updateSegment(i:Int,u:UserSegment){val c=_screen.value as? GameScreen.Playing?:return;_screen.value=c.copy(segments=c.segments.toMutableList().apply{this[i]=u})}
 fun addSegmentRow(){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.lastOrNull()?.isComplete==true)_screen.value=c.copy(segments=c.segments+UserSegment())}
 fun removeSegmentRow(i:Int){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.size>1)_screen.value=c.copy(segments=c.segments.toMutableList().apply{removeAt(i)})}
 fun verify(){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.lastOrNull()?.isComplete!=true)return;countdownJob?.cancel();_screen.value=GameScreen.Loading;viewModelScope.launch{finishGame(c,false)}}
 private suspend fun finishGame(c:GameScreen.Playing,forcedLoss:Boolean){try{val p=_prefs.value;val off=repository.fetchOfficialJourneys(c.departureStation.id,c.arrivalStation.id,p.walkEnabled,p.rerEnabled);if(off.isEmpty()){_screen.value=GameScreen.Error(if(p.language==GameLanguage.FRENCH)"Aucun itinéraire trouvé." else "No route found.");return};val won=!forcedLoss&&ItineraryComparator.matches(c.segments,off);val shown=if(won)store.win() else store.currentScore.also{store.publishAndReset()};_score.value=if(won)shown else 0;_screen.value=GameScreen.Result(won,shown,c.segments,off)}catch(e:Exception){_screen.value=GameScreen.Error(e.message?:"Network error.")}}
 fun showLogs(){countdownJob?.cancel();_screen.value=GameScreen.Logs};fun showScores(){countdownJob?.cancel();_screen.value=GameScreen.Scores};fun scoreHistory()=store.history();fun lastPublishedScore()=store.lastPublishedScore();fun bestPublishedScore()=store.bestPublishedScore();fun showSettings(){countdownJob?.cancel();_update.value=null;_screen.value=GameScreen.Settings};fun clearUpdate(){_update.value=null};fun backToMenu(){countdownJob?.cancel();_screen.value=GameScreen.Menu}
 fun setLanguage(v:GameLanguage){settingsStore.setLanguage(v);_prefs.value=settingsStore.get()};fun setWalkEnabled(v:Boolean){settingsStore.setWalkEnabled(v);_prefs.value=settingsStore.get()};fun setRerEnabled(v:Boolean){settingsStore.setRerEnabled(v);_prefs.value=settingsStore.get()}
 fun setCountdownEnabled(v:Boolean){settingsStore.setCountdownEnabled(v);_prefs.value=settingsStore.get()}
 fun setCountdown(minutes:Int,seconds:Int){settingsStore.setCountdown(minutes,seconds);_prefs.value=settingsStore.get()}
 fun normalExit(){countdownJob?.cancel();store.publishAndReset();_score.value=0}
 fun checkUpdate(){viewModelScope.launch(Dispatchers.IO){_update.value=GitHubReleaseClient(BuildConfig.UPDATE_REPOSITORY).check(BuildConfig.VERSION_NAME)}}
 override fun onCleared(){countdownJob?.cancel();super.onCleared()}
 class Factory(private val app:Application,private val repo:MetroRepository):ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST")override fun<T:ViewModel>create(c:Class<T>):T=GameViewModel(app,repo) as T}
}
