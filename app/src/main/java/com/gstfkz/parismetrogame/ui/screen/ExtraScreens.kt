package com.gstfkz.parismetrogame.ui.screen
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.model.ScoreEntry
import com.gstfkz.parismetrogame.data.network.UpdateInfo
import java.text.DateFormat
import java.util.Date
@Composable fun ScoresScreen(entries:List<ScoreEntry>,onBack:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp)){Text("Scores",style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(16.dp));if(entries.isEmpty())Text("Aucun score publié pour le moment.") else LazyColumn(Modifier.weight(1f)){items(entries){e->ListItem(headlineContent={Text("${e.score} victoire${if(e.score>1)"s" else ""}")},supportingContent={Text(DateFormat.getDateTimeInstance().format(Date(e.timestamp)))});HorizontalDivider()}};Button(onBack,Modifier.fillMaxWidth()){Text("Retour")}}}
@Composable fun SettingsScreen(update:UpdateInfo?,onCheck:()->Unit,onBack:()->Unit){val ctx=LocalContext.current;Column(Modifier.fillMaxSize().padding(24.dp)){Text("Paramètres",style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(20.dp));Text("Paris Metro Game");Text("Version ${BuildConfig.VERSION_NAME}");Spacer(Modifier.height(24.dp));Button(onCheck){Text("Vérifier les mises à jour")};update?.let{Spacer(Modifier.height(12.dp));Text(if(it.updateAvailable)"Nouvelle version disponible : ${it.latestVersion}" else "Application à jour (${it.latestVersion})");if(it.updateAvailable)OutlinedButton({ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))}){Text("Ouvrir la release GitHub")}};Spacer(Modifier.weight(1f));Button(onBack,Modifier.fillMaxWidth()){Text("Retour")}}}
