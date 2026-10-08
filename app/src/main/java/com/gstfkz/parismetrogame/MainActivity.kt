package com.gstfkz.parismetrogame
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.data.local.GameLanguage
import com.gstfkz.parismetrogame.data.local.PrimLogStore
import com.gstfkz.parismetrogame.data.model.GameScreen
import com.gstfkz.parismetrogame.data.network.PrimApiClient
import com.gstfkz.parismetrogame.data.repository.MetroRepository
import com.gstfkz.parismetrogame.ui.screen.*
import com.gstfkz.parismetrogame.ui.screen.GameScreen as GameScreenView
import com.gstfkz.parismetrogame.ui.viewmodel.GameViewModel
class MainActivity:ComponentActivity(){private val repository by lazy{MetroRepository(PrimApiClient.create(BuildConfig.PRIM_API_KEY, applicationContext))};private val vm:GameViewModel by viewModels{GameViewModel.Factory(application,repository)};private var normalExit=false
 override fun onCreate(b:Bundle?){super.onCreate(b);startService(Intent(this,ExitTrackingService::class.java));setContent{val blue=Color(0xFF1C1CD6);MaterialTheme(colorScheme=lightColorScheme(primary=blue,secondary=blue,tertiary=blue)){Surface(Modifier.fillMaxSize()){val s by vm.screen.collectAsState();val update by vm.update.collectAsState();val prefs by vm.prefs.collectAsState();val txt=UiText.forLanguage(prefs.language);var confirmQuit by remember { mutableStateOf(false) };BackHandler(enabled=s is GameScreen.Playing){confirmQuit=true};if(confirmQuit){AlertDialog(onDismissRequest={confirmQuit=false},title={Text(txt.t("Quit game?","Quitter la partie ?"))},text={Text(txt.t("Do you want to quit the current game and return to the main menu?","Voulez-vous quitter la partie en cours et revenir au menu principal ?"))},confirmButton={TextButton(onClick={confirmQuit=false;vm.backToMenu()}){Text(txt.t("Yes","Oui"))}},dismissButton={TextButton(onClick={confirmQuit=false}){Text(txt.t("No","Non"))}})};when(val x=s){GameScreen.Menu->MenuScreen(vm.lastPublishedScore(),vm.bestPublishedScore(),txt,{vm.startNewGame()},{vm.showScores()},{vm.showSettings()},{normalExit=true;vm.normalExit();finish()});GameScreen.Loading->LoadingView(txt);is GameScreen.Playing->GameScreenView(x.departureStation,x.arrivalStation,x.segments,vm.availableLines,{vm.stationsForLine(it)},txt,vm::updateSegment,vm::addSegmentRow,vm::removeSegmentRow,vm::verify,x.remainingSeconds);is GameScreen.Result->ResultScreen(x.won,x.displayedScore,x.userSegments,x.officialJourneys,txt,{vm.startNewGame()},{normalExit=true;vm.normalExit();finish()},{vm.backToMenu()});GameScreen.Logs->LogsScreen(txt,vm::showSettings);GameScreen.Scores->ScoresScreen(vm.scoreHistory(),txt,vm::backToMenu);GameScreen.Settings->SettingsScreen(prefs,update,txt,vm::setLanguage,vm::setWalkEnabled,vm::setRerEnabled,vm::setCountdownEnabled,vm::setCountdown,vm::checkUpdate,vm::clearUpdate,vm::showLogs,vm::backToMenu);is GameScreen.Error->ErrorView(x.message,txt,vm::backToMenu)}}}}}
 override fun onDestroy(){if(isFinishing&&normalExit.not()){vm.normalExit()};super.onDestroy()}
}
@Composable private fun LoadingView(t:UiText){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){CircularProgressIndicator();Text(t.t("Loading…","Chargement…"),Modifier.padding(top=16.dp))}}
@Composable private fun ErrorView(m:String,t:UiText,back:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(m);Button(back,Modifier.padding(top=16.dp)){Text(t.t("Back to menu","Retour au menu"))}}}
