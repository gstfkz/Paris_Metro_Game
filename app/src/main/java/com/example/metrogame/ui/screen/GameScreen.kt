package com.example.metrogame.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.metrogame.data.model.MetroLine
import com.example.metrogame.data.model.MetroStation
import com.example.metrogame.data.model.UserSegment

@Composable
fun GameScreen(
    departureStation: MetroStation,
    arrivalStation: MetroStation,
    segments: List<UserSegment>,
    availableLines: List<MetroLine>,
    stationsForLine: (MetroLine) -> List<MetroStation>,
    onSegmentChange: (index: Int, UserSegment) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: (index: Int) -> Unit,
    onVerify: () -> Unit
) {
    val lastComplete = segments.lastOrNull()?.isComplete == true

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(departureStation.name, style = MaterialTheme.typography.titleMedium)
            Text("→", style = MaterialTheme.typography.titleMedium)
            Text(arrivalStation.name, style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(segments.size) { index ->
                SegmentRow(
                    segment = segments[index],
                    availableLines = availableLines,
                    stationsForLine = stationsForLine,
                    onChange = { onSegmentChange(index, it) },
                    onRemove = if (segments.size > 1) ({ onRemoveRow(index) }) else null
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (lastComplete) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onAddRow) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter une ligne")
                }
                Button(onClick = onVerify) {
                    Text("Vérifier")
                }
            }
        }
    }
}

@Composable
private fun SegmentRow(
    segment: UserSegment,
    availableLines: List<MetroLine>,
    stationsForLine: (MetroLine) -> List<MetroStation>,
    onChange: (UserSegment) -> Unit,
    onRemove: (() -> Unit)?
) {
    val stations = segment.line?.let(stationsForLine).orEmpty()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dropdown(
            modifier = Modifier.weight(1f),
            label = "Ligne",
            options = availableLines,
            optionLabel = { it.displayName },
            selected = segment.line,
            enabled = true,
            onSelected = { newLine ->
                // Changer de ligne invalide les stations déjà choisies (autre périmètre).
                onChange(UserSegment(line = newLine))
            }
        )
        Dropdown(
            modifier = Modifier.weight(1f),
            label = "Départ",
            options = stations,
            optionLabel = { it.name },
            selected = segment.departureStation,
            enabled = segment.line != null,
            onSelected = { onChange(segment.copy(departureStation = it)) }
        )
        Dropdown(
            modifier = Modifier.weight(1f),
            label = "Arrivée",
            options = stations,
            optionLabel = { it.name },
            selected = segment.arrivalStation,
            enabled = segment.line != null,
            onSelected = { onChange(segment.copy(arrivalStation = it)) }
        )
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Supprimer cette ligne")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Dropdown(
    modifier: Modifier = Modifier,
    label: String,
    options: List<T>,
    optionLabel: (T) -> String,
    selected: T?,
    enabled: Boolean,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            enabled = enabled,
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) }
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
