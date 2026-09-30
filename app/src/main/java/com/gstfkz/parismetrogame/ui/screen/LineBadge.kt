package com.gstfkz.parismetrogame.ui.screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gstfkz.parismetrogame.data.model.*

private fun parseColor(hex:String?):Color=try{Color(android.graphics.Color.parseColor(if(hex?.startsWith("#")==true)hex else "#$hex"))}catch(_:Exception){Color(0xFF1C1CD6)}
@Composable fun LineBadge(line:MetroLine){when(line.mode){TransportMode.WALK->Text("🚶",style=MaterialTheme.typography.titleLarge);TransportMode.METRO->Box(Modifier.size(34.dp).background(parseColor(line.colorHex),CircleShape),contentAlignment=Alignment.Center){Text(line.code,color=Color.White,fontWeight=FontWeight.Bold)};TransportMode.RER->Box(Modifier.height(32.dp).background(parseColor(line.colorHex),RoundedCornerShape(7.dp)).padding(horizontal=8.dp),contentAlignment=Alignment.Center){Text("RER ${line.code}",color=Color.White,fontWeight=FontWeight.Bold)}}}
@Composable fun OfficialLineBadge(s:OfficialSegment){LineBadge(MetroLine(s.lineId,s.lineCode,s.lineCode,s.mode,s.colorHex))}
