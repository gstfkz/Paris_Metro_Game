package com.gstfkz.parismetrogame.ui.screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.R
@Composable fun MenuScreen(lastScore:Int?,bestScore:Int?,onPlay:()->Unit,onScores:()->Unit,onSettings:()->Unit,onQuit:()->Unit){Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Image(painterResource(R.drawable.app_icon),"Logo Paris Metro Game",Modifier.size(132.dp),contentScale=ContentScale.Fit);Spacer(Modifier.height(8.dp));Text("Paris Metro Game",style=MaterialTheme.typography.headlineLarge);Text("Retrouve l'itinéraire le plus rapide entre 2 stations",Modifier.padding(top=8.dp,bottom=16.dp));Text("Dernière série : ${lastScore?.toString()?:"-"}",style=MaterialTheme.typography.titleMedium);Text("Meilleure série : ${bestScore?.toString()?:"-"}",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(bottom=24.dp));Button(onPlay,Modifier.fillMaxWidth()){Text("Jouer")};Spacer(Modifier.height(10.dp));OutlinedButton(onScores,Modifier.fillMaxWidth()){Text("Scores")};Spacer(Modifier.height(10.dp));OutlinedButton(onSettings,Modifier.fillMaxWidth()){Text("Paramètres")};Spacer(Modifier.height(10.dp));TextButton(onQuit){Text("Quitter")}}}
