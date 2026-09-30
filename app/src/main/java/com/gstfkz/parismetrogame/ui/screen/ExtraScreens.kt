package com.gstfkz.parismetrogame.ui.screen
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.local.*
import com.gstfkz.parismetrogame.data.model.ScoreEntry
import com.gstfkz.parismetrogame.data.network.UpdateInfo
import java.text.DateFormat
import java.util.Date
@Composable fun ScoresScreen(entries:List<ScoreEntry>,txt:UiText,onBack:()->Unit){Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)){Text(txt.t("Scores","Scores"),style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(16.dp));if(entries.isEmpty())Text(txt.t("No published score yet.","Aucun score publié pour le moment.")) else LazyColumn(Modifier.weight(1f)){items(entries){e->ListItem(headlineContent={Text("${e.score} "+txt.t(if(e.score==1)"win" else "wins",if(e.score==1)"victoire" else "victoires"))},supportingContent={Text(DateFormat.getDateTimeInstance().format(Date(e.timestamp)))});HorizontalDivider()}};Button(onBack,Modifier.fillMaxWidth()){Text(txt.t("Back","Retour"))}}}
@Composable fun SettingsScreen(prefs:GamePreferences,update:UpdateInfo?,txt:UiText,onLanguage:(GameLanguage)->Unit,onWalk:(Boolean)->Unit,onRer:(Boolean)->Unit,onCheck:()->Unit,onDismissUpdate:()->Unit,onBack:()->Unit){val ctx=LocalContext.current;Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)){Text(txt.t("Settings","Paramètres"),style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.height(16.dp));Text(txt.t("Game language","Langue du jeu"),style=MaterialTheme.typography.titleMedium);Row{FilterChip(prefs.language==GameLanguage.ENGLISH,{onLanguage(GameLanguage.ENGLISH)},{Text("English")});Spacer(Modifier.width(8.dp));FilterChip(prefs.language==GameLanguage.FRENCH,{onLanguage(GameLanguage.FRENCH)},{Text("Français")})};HorizontalDivider(Modifier.padding(vertical=16.dp));Text(txt.t("Transport modes","Modes de transport"),style=MaterialTheme.typography.titleMedium);SettingSwitch("Metro",true,false){};SettingSwitch(txt.t("Walk","Marche"),prefs.walkEnabled,true,onWalk);SettingSwitch("RER",prefs.rerEnabled,true,onRer);HorizontalDivider(Modifier.padding(vertical=16.dp));Text(txt.t("Game version","Version du jeu"),style=MaterialTheme.typography.titleMedium);Text("Paris Metro Game — ${BuildConfig.VERSION_NAME}");Spacer(Modifier.height(12.dp));Button(onCheck){Text(txt.t("Check for updates","Vérifier les mises à jour"))};Spacer(Modifier.weight(1f));Button(onBack,Modifier.fillMaxWidth()){Text(txt.t("Back","Retour"))}};update?.let{info->AlertDialog(onDismissRequest=onDismissUpdate,title={Text(txt.t("Update","Mise à jour"))},text={Text(info.errorMessage?:if(info.updateAvailable)txt.t("New version available: ","Nouvelle version disponible : ")+info.latestVersion else txt.t("The app is up to date (","L'application est à jour (")+info.latestVersion+").")},confirmButton={if(info.updateAvailable&&info.url.isNotBlank())TextButton({ctx.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(info.url)));onDismissUpdate()}){Text(txt.t("Open GitHub","Ouvrir GitHub"))}else TextButton(onDismissUpdate){Text("OK")}},dismissButton={if(info.updateAvailable)TextButton(onDismissUpdate){Text(txt.t("Later","Plus tard"))}})}}
@Composable private fun SettingSwitch(label:String,checked:Boolean,enabled:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.padding(top=12.dp));Switch(checked,onChange,enabled=enabled)}}
