package com.gstfkz.parismetrogame.ui.viewmodel
import android.app.Application
import androidx.lifecycle.*
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.local.ScoreStore
import com.gstfkz.parismetrogame.data.model.*
import com.gstfkz.parismetrogame.data.network.GitHubReleaseClient
import com.gstfkz.parismetrogame.data.network.UpdateInfo
import com.gstfkz.parismetrogame.data.repository.MetroRepository
import com.gstfkz.parismetrogame.util.ItineraryComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class GameViewModel(app:Application,private val repository:MetroRepository):AndroidViewModel(app){
 private val store=ScoreStore(app); private val _screen=MutableStateFlow<GameScreen>(GameScreen.Menu);val screen=_screen.asStateFlow();private var network:MetroNetwork?=null
 private val _score=MutableStateFlow(store.currentScore);val score=_score.asStateFlow();private val _update=MutableStateFlow<UpdateInfo?>(null);val update=_update.asStateFlow()
 val availableLines:List<MetroLine> get()=listOf(MetroLine.WALK)+network?.lines.orEmpty()
 fun stationsForLine(line:MetroLine)=if(line.mode==TransportMode.WALK)network?.let{repository.allStations(it)}.orEmpty() else network?.stationsByLine?.get(line.id).orEmpty()
 fun startNewGame(){_screen.value=GameScreen.Loading;viewModelScope.launch{try{val n=network?:repository.loadNetwork().also{network=it};val(a,b)=repository.pickRandomStationPair(n);_screen.value=GameScreen.Playing(a,b,listOf(UserSegment()))}catch(e:Exception){_screen.value=GameScreen.Error(e.message?:"Impossible de charger le réseau.")}}}
 fun updateSegment(i:Int,u:UserSegment){val c=_screen.value as? GameScreen.Playing?:return;_screen.value=c.copy(segments=c.segments.toMutableList().apply{this[i]=u})}
 fun addSegmentRow(){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.lastOrNull()?.isComplete==true)_screen.value=c.copy(segments=c.segments+UserSegment())}
 fun removeSegmentRow(i:Int){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.size>1)_screen.value=c.copy(segments=c.segments.toMutableList().apply{removeAt(i)})}
 fun verify(){val c=_screen.value as? GameScreen.Playing?:return;if(c.segments.lastOrNull()?.isComplete!=true)return;_screen.value=GameScreen.Loading;viewModelScope.launch{try{val off=repository.fetchOfficialJourneys(c.departureStation.id,c.arrivalStation.id);if(off.isEmpty()){_screen.value=GameScreen.Error("Aucun itinéraire trouvé.");return@launch};val won=ItineraryComparator.matches(c.segments,off);if(won)_score.value=store.win() else {store.publishAndReset();_score.value=0};_screen.value=GameScreen.Result(won,c.segments,off)}catch(e:Exception){_screen.value=GameScreen.Error(e.message?:"Erreur réseau.")}}}
 fun showScores(){_screen.value=GameScreen.Scores};fun scoreHistory()=store.history();fun showSettings(){_screen.value=GameScreen.Settings};fun backToMenu(){_screen.value=GameScreen.Menu}
 fun normalExit(){store.publishAndReset();_score.value=0}
 fun checkUpdate(){viewModelScope.launch(Dispatchers.IO){_update.value=GitHubReleaseClient(BuildConfig.UPDATE_REPOSITORY).check(BuildConfig.VERSION_NAME)}}
 class Factory(private val app:Application,private val repo:MetroRepository):ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST")override fun<T:ViewModel>create(c:Class<T>):T=GameViewModel(app,repo) as T}
}
