package com.gstfkz.parismetrogame.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.NumberPicker
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.BuildConfig
import com.gstfkz.parismetrogame.data.local.*
import com.gstfkz.parismetrogame.data.model.ScoreEntry
import com.gstfkz.parismetrogame.data.network.UpdateDownloader
import com.gstfkz.parismetrogame.data.network.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScoresScreen(entries: List<ScoreEntry>, txt: UiText, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        Text(txt.t("Scores", "Scores"), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        if (entries.isEmpty()) Text(txt.t("No published score yet.", "Aucun score publié pour le moment."))
        else LazyColumn(Modifier.weight(1f)) {
            items(entries) { e ->
                ListItem(
                    headlineContent = { Text("${e.score} " + txt.t(if (e.score == 1) "win" else "wins", if (e.score == 1) "victoire" else "victoires")) },
                    supportingContent = { Text(DateFormat.getDateTimeInstance().format(Date(e.timestamp))) }
                )
                HorizontalDivider()
            }
        }
        Button(onBack, Modifier.fillMaxWidth()) { Text(txt.t("Back", "Retour")) }
    }
}

@Composable
fun SettingsScreen(
    prefs: GamePreferences,
    update: UpdateInfo?,
    txt: UiText,
    onLanguage: (GameLanguage) -> Unit,
    onWalk: (Boolean) -> Unit,
    onRer: (Boolean) -> Unit,
    onCountdownEnabled: (Boolean) -> Unit,
    onCountdown: (Int, Int) -> Unit,
    onCheck: () -> Unit,
    onDismissUpdate: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var downloading by remember { mutableStateOf(false) }
    var downloaded by remember { mutableLongStateOf(0L) }
    var total by remember { mutableLongStateOf(0L) }
    var downloadError by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
        Text(txt.t("Settings", "Paramètres"), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(txt.t("Game language", "Langue du jeu"), style = MaterialTheme.typography.titleMedium)
        Row {
            FilterChip(prefs.language == GameLanguage.ENGLISH, { onLanguage(GameLanguage.ENGLISH) }, { Text("English") })
            Spacer(Modifier.width(8.dp))
            FilterChip(prefs.language == GameLanguage.FRENCH, { onLanguage(GameLanguage.FRENCH) }, { Text("Français") })
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        Text(txt.t("Transport modes", "Modes de transport"), style = MaterialTheme.typography.titleMedium)
        SettingSwitch("Metro", true, false) {}
        SettingSwitch(txt.t("Walk", "Marche"), prefs.walkEnabled, true, onWalk)
        SettingSwitch("RER", prefs.rerEnabled, true, onRer)
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        Text(txt.t("Countdown", "Compte à rebours"), style = MaterialTheme.typography.titleMedium)
        SettingSwitch(txt.t("Enable countdown", "Activer le compte à rebours"), prefs.countdownEnabled, true, onCountdownEnabled)
        if (prefs.countdownEnabled) {
            CountdownSetting(prefs.countdownMinutes, prefs.countdownSeconds, txt, onCountdown)
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        Text(txt.t("Game version", "Version du jeu"), style = MaterialTheme.typography.titleMedium)
        Text("Paris Metro Game — ${BuildConfig.VERSION_NAME}")
        Spacer(Modifier.height(12.dp))
        Button(onCheck) { Text(txt.t("Check for updates", "Vérifier les mises à jour")) }
        Spacer(Modifier.weight(1f))
        Button(onBack, Modifier.fillMaxWidth()) { Text(txt.t("Back", "Retour")) }
    }

    update?.let { info ->
        AlertDialog(
            onDismissRequest = { if (!downloading) onDismissUpdate() },
            title = { Text(txt.t("Update", "Mise à jour")) },
            text = {
                Column {
                    when {
                        info.errorMessage != null -> Text(info.errorMessage)
                        downloading -> {
                            Text(txt.t("Downloading version ", "Téléchargement de la version ") + info.latestVersion)
                            Spacer(Modifier.height(14.dp))
                            val progress = if (total > 0) (downloaded.toFloat() / total).coerceIn(0f, 1f) else 0f
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (total > 0) "${(progress * 100).toInt()} %" else "…")
                                Text("${formatBytes(downloaded)} / ${if (total > 0) formatBytes(total) else "?"}")
                            }
                            downloadError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                        }
                        info.updateAvailable -> {
                            Text(txt.t("New version available: ", "Nouvelle version disponible : ") + info.latestVersion)
                            if (info.changelog.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Text(txt.t("Changelog", "Changelog"), style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(info.changelog)
                            }
                            if (info.downloadUrl.isBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text(txt.t("No APK is attached to this GitHub release.", "Aucun APK n'est joint à cette release GitHub."), color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> Text(txt.t("The app is up to date (", "L'application est à jour (") + info.latestVersion + ").")
                    }
                }
            },
            confirmButton = {
                if (info.updateAvailable && info.downloadUrl.isNotBlank() && !downloading) {
                    TextButton(onClick = {
                        downloading = true
                        downloadError = null
                        downloaded = 0
                        total = info.downloadSize
                        scope.launch {
                            try {
                                val apk = UpdateDownloader.download(ctx, info.downloadUrl, info.downloadSize) { done, size ->
                                    withContext(Dispatchers.Main) {
                                        downloaded = done
                                        if (size > 0) total = size
                                    }
                                }
                                downloading = false
                                onDismissUpdate()
                                UpdateDownloader.launchInstaller(ctx, apk)
                            } catch (e: Exception) {
                                downloading = false
                                downloadError = e.message ?: txt.t("Download failed.", "Échec du téléchargement.")
                            }
                        }
                    }) { Text(txt.t("Download", "Télécharger")) }
                } else if (!downloading) {
                    TextButton(onDismissUpdate) { Text("OK") }
                }
            },
            dismissButton = {
                if (info.updateAvailable && !downloading) TextButton(onDismissUpdate) { Text(txt.t("Later", "Plus tard")) }
            }
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "?"
    val mb = bytes / (1024.0 * 1024.0)
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, Modifier.padding(top = 12.dp))
        Switch(checked, onChange, enabled = enabled)
    }
}

@Composable
private fun CountdownSetting(minutes:Int, seconds:Int, txt:UiText, onChange:(Int,Int)->Unit){
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(txt.t("Time: ", "Durée : ") + "%02d:%02d".format(minutes, seconds))
    }
    if(showPicker){
        var pickedMinutes by remember { mutableIntStateOf(minutes) }
        var pickedSeconds by remember { mutableIntStateOf(seconds) }
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(txt.t("Select countdown", "Choisir le compte à rebours")) },
            text = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(txt.t("Minutes", "Minutes"), color = MaterialTheme.colorScheme.primary)
                        AndroidView(
                            factory = { context -> NumberPicker(context).apply {
                                minValue = 0; maxValue = 99; value = pickedMinutes
                                wrapSelectorWheel = true
                                setOnValueChangedListener { _,_,new -> pickedMinutes = new }
                            }},
                            update = { it.value = pickedMinutes },
                            modifier = Modifier.width(110.dp).height(150.dp)
                        )
                    }
                    Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 64.dp))
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(txt.t("Seconds", "Secondes"), color = MaterialTheme.colorScheme.primary)
                        AndroidView(
                            factory = { context -> NumberPicker(context).apply {
                                minValue = 0; maxValue = 59; value = pickedSeconds
                                wrapSelectorWheel = true
                                setOnValueChangedListener { _,_,new -> pickedSeconds = new }
                            }},
                            update = { it.value = pickedSeconds },
                            modifier = Modifier.width(110.dp).height(150.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if(pickedMinutes == 0 && pickedSeconds == 0) pickedSeconds = 1
                    onChange(pickedMinutes, pickedSeconds)
                    showPicker = false
                }) { Text(txt.t("OK", "OK"), color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(txt.t("Cancel", "Annuler"), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}
