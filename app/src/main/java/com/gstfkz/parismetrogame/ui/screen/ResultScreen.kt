package com.gstfkz.parismetrogame.ui.screen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.data.model.*

@Composable fun ResultScreen(won:Boolean,displayedScore:Int,userSegments:List<UserSegment>,officialJourneys:List<OfficialJourney>,txt:UiText,onContinue:()->Unit,onQuit:()->Unit,onHome:()->Unit){
 Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)){
  Text(if(won)txt.t("You won!","Gagné !") else txt.t("You lost","Perdu"),style=MaterialTheme.typography.headlineLarge)
  Text(txt.t("Streak: ","Série : ")+displayedScore,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=8.dp))
  if(!won){
   HorizontalDivider(Modifier.padding(vertical=16.dp));Text(txt.t("Your route:","Ton itinéraire :"),style=MaterialTheme.typography.titleMedium)
   Column(verticalArrangement=Arrangement.spacedBy(8.dp)){userSegments.filter{it.isComplete}.forEach{u->Row(verticalAlignment=Alignment.CenterVertically){u.line?.let{LineBadge(it)};Spacer(Modifier.width(12.dp));Text("${u.departureStation?.name} → ${u.arrivalStation?.name}",fontWeight=FontWeight.Bold)}}}
   HorizontalDivider(Modifier.padding(vertical=16.dp));Text(if(officialJourneys.size>1)txt.t("Fastest routes (tie):","Itinéraires les plus rapides ex æquo :") else txt.t("Fastest route:","Itinéraire le plus rapide :"),style=MaterialTheme.typography.titleMedium)
   officialJourneys.forEachIndexed{idx,j->if(officialJourneys.size>1)Text(txt.t("Option ","Option ")+"${idx+1}",style=MaterialTheme.typography.titleSmall,modifier=Modifier.padding(top=8.dp));Column(verticalArrangement=Arrangement.spacedBy(6.dp)){j.segments.forEach{s->Row(verticalAlignment=Alignment.CenterVertically){OfficialLineBadge(s);Spacer(Modifier.width(12.dp));Text("${s.fromStationName} → ${s.toStationName}",fontWeight=FontWeight.Bold)}}};Text(txt.t("Duration: ","Durée : ")+"${j.totalDurationSeconds/60} min")}
  }
  Spacer(Modifier.weight(1f))
  if(won){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){Button(onContinue){Text(txt.t("Continue","Continuer"))};OutlinedButton(onQuit){Text(txt.t("Quit","Quitter"))}}}
  else{Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){Button(onContinue,Modifier.weight(1f)){Text(txt.t("Play again","Rejouer"))};OutlinedButton(onHome,Modifier.weight(1f)){Text(txt.t("Home","Accueil"))}};OutlinedButton(onQuit,Modifier.fillMaxWidth()){Text(txt.t("Quit","Quitter"))}}}
 }
}
