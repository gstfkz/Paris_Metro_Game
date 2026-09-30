package com.gstfkz.parismetrogame.ui.screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.R
@Composable fun MenuScreen(lastScore:Int?,bestScore:Int?,txt:UiText,onPlay:()->Unit,onScores:()->Unit,onSettings:()->Unit,onQuit:()->Unit){Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Image(painterResource(R.drawable.app_icon),"Paris Metro Game",Modifier.size(176.dp),contentScale=ContentScale.Crop);Text("Paris Metro Game",style=MaterialTheme.typography.headlineLarge);Text(txt.t("Find the fastest route between 2 stations","Trouve l'itinéraire le plus rapide entre 2 stations"),Modifier.fillMaxWidth().padding(top=8.dp,bottom=16.dp),maxLines=1,textAlign=TextAlign.Center,style=MaterialTheme.typography.bodySmall);Text(txt.t("Last streak: ","Dernière série : ")+(lastScore?.toString()?:"-"),style=MaterialTheme.typography.titleMedium);Text(txt.t("Best streak: ","Meilleure série : ")+(bestScore?.toString()?:"-"),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(bottom=24.dp));Button(onPlay,Modifier.fillMaxWidth()){Text(txt.t("Play","Jouer"))};Spacer(Modifier.height(10.dp));OutlinedButton(onScores,Modifier.fillMaxWidth()){Text(txt.t("Scores","Scores"))};Spacer(Modifier.height(10.dp));OutlinedButton(onSettings,Modifier.fillMaxWidth()){Text(txt.t("Settings","Paramètres"))};Spacer(Modifier.height(10.dp));TextButton(onQuit){Text(txt.t("Quit","Quitter"))}}}
