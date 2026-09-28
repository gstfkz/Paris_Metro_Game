package com.gstfkz.parismetrogame
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.data.model.GameScreen
import com.gstfkz.parismetrogame.data.network.PrimApiClient
import com.gstfkz.parismetrogame.data.repository.MetroRepository
import com.gstfkz.parismetrogame.ui.screen.*
import com.gstfkz.parismetrogame.ui.screen.GameScreen as GameScreenView
import com.gstfkz.parismetrogame.ui.viewmodel.GameViewModel
class MainActivity:ComponentActivity(){private val repository by lazy{MetroRepository(PrimApiClient.create(BuildConfig.PRIM_API_KEY))};private val vm:GameViewModel by viewModels{GameViewModel.Factory(application,repository)};private var normalExit=false
 override fun onCreate(b:Bundle?){super.onCreate(b);startService(Intent(this,ExitTrackingService::class.java));setContent{MaterialTheme{Surface(Modifier.fillMaxSize()){val s by vm.screen.collectAsState();val score by vm.score.collectAsState();val update by vm.update.collectAsState();when(val x=s){GameScreen.Menu->MenuScreen(vm.lastPublishedScore(),vm.bestPublishedScore(),{vm.startNewGame()},{vm.showScores()},{vm.showSettings()},{normalExit=true;vm.normalExit();finish()});GameScreen.Loading->LoadingView();is GameScreen.Playing->GameScreenView(x.departureStation,x.arrivalStation,x.segments,vm.availableLines,{vm.stationsForLine(it)},vm::updateSegment,vm::addSegmentRow,vm::removeSegmentRow,vm::verify);is GameScreen.Result->ResultScreen(x.won,score,x.userSegments,x.officialJourneys,{vm.startNewGame()},{normalExit=true;vm.normalExit();finish()});GameScreen.Scores->ScoresScreen(vm.scoreHistory(),vm::backToMenu);GameScreen.Settings->SettingsScreen(update,vm::checkUpdate,vm::clearUpdate,vm::backToMenu);is GameScreen.Error->ErrorView(x.message,vm::backToMenu)}}}}}
 override fun onDestroy(){if(isFinishing&&normalExit.not()){vm.normalExit()};super.onDestroy()}
}
@Composable private fun LoadingView(){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){CircularProgressIndicator();Text("Chargement…",Modifier.padding(top=16.dp))}}
@Composable private fun ErrorView(m:String,back:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(m);Button(back,Modifier.padding(top=16.dp)){Text("Retour au menu")}}}
