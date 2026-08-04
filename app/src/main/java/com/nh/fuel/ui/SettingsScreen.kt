package com.nh.fuel.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.nh.fuel.data.ActivityLogger
import com.nh.fuel.data.AppUserSession
import com.nh.fuel.data.DailyFuelRecord
import com.nh.fuel.data.DayShift
import com.nh.fuel.data.DispenserShift
import com.nh.fuel.data.KeyStatus
import com.nh.fuel.data.NozzleShift
import com.nh.fuel.data.Role
import com.nh.fuel.data.StaffAccessKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: AppUserSession,
    currentRecord: DailyFuelRecord = DailyFuelRecord(),
    allRecords: List<DailyFuelRecord> = emptyList(),
    currentOpacity: Float,
    currentThemeMode: ThemeMode,
    onOpacityChanged: (Float) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onRecordChanged: (DailyFuelRecord) -> Unit = {},
    onLogout: () -> Unit = {},
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp
) {
    var sliderValue by remember(currentOpacity) { mutableFloatStateOf(currentOpacity) }
    var showStaffManagementPage by remember { mutableStateOf(false) }
    var showMaintenancePage by remember { mutableStateOf(false) }
    var showActivityLogPage by remember { mutableStateOf(false) }
    var showLocalBackupPage by remember { mutableStateOf(false) }

    val canAccessAdminPanel = session.isOwnerLogin || session.role == Role.SUPER_ADMIN || session.role == Role.ADMIN
    val isSuperAdmin = session.isOwnerLogin || session.role == Role.SUPER_ADMIN

    if (showStaffManagementPage) {
        BackHandler { showStaffManagementPage = false }
        StaffManagementScreen(
            onBack = { showStaffManagementPage = false },
            topInset = topInset,
            bottomInset = bottomInset
        )
    } else if (showMaintenancePage && isSuperAdmin) {
        BackHandler { showMaintenancePage = false }
        HardwareMaintenanceScreen(
            session = session,
            currentRecord = currentRecord,
            onBack = { showMaintenancePage = false },
            onRecordChanged = onRecordChanged,
            topInset = topInset,
            bottomInset = bottomInset
        )
    } else if (showActivityLogPage) {
        BackHandler { showActivityLogPage = false }
        ActivityLogScreen(
            session = session,
            onBack = { showActivityLogPage = false },
            topInset = topInset,
            bottomInset = bottomInset
        )
    } else if (showLocalBackupPage && isSuperAdmin) {
        BackHandler { showLocalBackupPage = false }
        LocalBackupScreen(
            session = session,
            allRecords = allRecords,
            onBack = { showLocalBackupPage = false },
            topInset = topInset,
            bottomInset = bottomInset
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(topInset + 4.dp))

                Text(
                    text = "App Settings & Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Profile Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(session.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Role: ${session.role.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Permissions: ${if (session.isReadOnly) "Read-Only Mode" else "Full Access"} | Past Edit: ${if (session.canEditPastDates) "Allowed" else "Locked"}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Log Out", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Log Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Staff Access Card
                if (canAccessAdminPanel) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { showStaffManagementPage = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Staff Access & Roles", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tap to manage staff keys, roles & privileges", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Local Emergency Backup & Restore Tile (Super Admin Only)
                if (isSuperAdmin) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { showLocalBackupPage = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Local Emergency Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Export or restore offline station records locally", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Activity & Audit Logs Tile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .clickable { showActivityLogPage = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Activity & Audit Logs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("View history of changes for the last 90 days", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Super Admin Hardware Maintenance Mode
                if (isSuperAdmin) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { showMaintenancePage = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Column {
                                    Text("Super Admin Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Text("Reset hardware meter readings for pump recalibration", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // DEVELOPER ANIMATED CREDIT SECTION
                Spacer(Modifier.height(4.dp))
                DeveloperCreditLine()
                Spacer(Modifier.height(2.dp))
                DeveloperInfoSection()
            }

            // Compact Bottom Nav Bar Opacity Bar & App Version Footer
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nav Bar Opacity", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                onOpacityChanged(it)
                            },
                            valueRange = 0.2f..1.0f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("${(sliderValue * 100).roundToInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // CENTER BOTTOM VERSION NUMBER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Version: 1.9.6",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(bottomInset + 8.dp))
            }
        }
    }
}

// ============================================================================
// DEDICATED LOCAL EMERGENCY BACKUP & RESTORE SCREEN (SUPER ADMIN ONLY)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalBackupScreen(
    session: AppUserSession,
    allRecords: List<DailyFuelRecord>,
    onBack: () -> Unit,
    topInset: Dp,
    bottomInset: Dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            statusMessage = "Restoring emergency local data..."
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader().use { it.readText() }
                    } ?: throw Exception("Could not read backup file.")

                    val restoredRecords = Gson().fromJson(jsonString, Array<DailyFuelRecord>::class.java)
                    val db = FirebaseFirestore.getInstance()

                    restoredRecords.forEach { rec ->
                        db.collection("daily_fuel_records").document(rec.date).set(rec)
                    }

                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        statusMessage = "Emergency restore complete (${restoredRecords.size} records restored)!"
                        Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_LONG).show()
                        ActivityLogger.log(session, "restored ${restoredRecords.size} records via local emergency backup")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        statusMessage = "Restore failed: ${e.message}"
                        Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(topInset + 4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Local Emergency Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Offline Emergency Tools", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Text(
                    text = "• Local Backup: Serializes all station fuel, shift, and variation records into a JSON file saved directly to your phone's 'Downloads' directory.\n• Local Restore: Select an emergency backup file from phone storage to restore station database states.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                statusMessage?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = {
                        if (allRecords.isNotEmpty()) {
                            isProcessing = true
                            statusMessage = "Creating local emergency backup..."
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val jsonString = Gson().toJson(allRecords)
                                    val fileName = "NHFuel_Emergency_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
                                    val downloadFolder = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                    val destFile = File(downloadFolder, fileName)
                                    destFile.writeText(jsonString)

                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        statusMessage = "Backup created: Downloads/$fileName"
                                        Toast.makeText(context, "Backup saved to Downloads folder!", Toast.LENGTH_LONG).show()
                                        ActivityLogger.log(session, "created local emergency backup file ($fileName)")
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        statusMessage = "Backup failed: ${e.message}"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isProcessing && allRecords.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Create & Export Local Backup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { restoreFilePickerLauncher.launch(arrayOf("*/*", "application/json")) },
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Restore Data From Backup File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.height(bottomInset + 8.dp))
    }
}

// ============================================================================
// DEVELOPER CREDIT LINE & TOWING TRUCK ANIMATION COMPONENT
// ============================================================================
@Composable
private fun DeveloperCreditLine() {
    val progress = remember { Animatable(0f) }
    val truckExit = remember { Animatable(0f) }
    val truckExitAlpha = remember { Animatable(1f) }
    var showTruck by remember { mutableStateOf(true) }

    val fxTransition = rememberInfiniteTransition(label = "creditFx")
    val smokePhase by fxTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1500, easing = LinearEasing)),
        label = "smokePhase"
    )
    val firePhase by fxTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 180, easing = LinearEasing)),
        label = "firePhase"
    )

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 3200
                0f at 0 using LinearOutSlowInEasing
                1.08f at 2200 using FastOutSlowInEasing
                0.96f at 2650 using FastOutSlowInEasing
                1f at 3200 using FastOutSlowInEasing
            }
        )
        coroutineScope {
            launch {
                truckExit.animateTo(
                    targetValue = 650f,
                    animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
                )
            }
            launch {
                truckExitAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 2200
                        1f at 0
                        1f at 1600
                        0f at 2200
                    }
                )
            }
        }
        showTruck = false
    }

    val textWidthDp = 150f
    val truckWidthDp = 46f
    val ropeSpanDp = 40f
    val towLeadGapDp = truckWidthDp + ropeSpanDp
    val towStartX = 1000f
    val textLeftX = towStartX * (1f - progress.value)
    val truckLeftX = textLeftX - towLeadGapDp - truckExit.value
    val isDragging = progress.value < 0.98f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        if (showTruck) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val ropeAlpha = (1f - (truckExit.value / 70f)).coerceIn(0f, 1f)
                val groundY = size.height * 0.86f
                if (ropeAlpha > 0f) {
                    drawTowRope(
                        fromX = (truckLeftX + truckWidthDp).dp.toPx(),
                        toX = textLeftX.dp.toPx(),
                        y = groundY,
                        alpha = ropeAlpha
                    )
                }
                drawExhaustSmoke(
                    anchorX = (truckLeftX + truckWidthDp).dp.toPx(),
                    anchorY = size.height * 0.40f,
                    phase = smokePhase
                )
                if (isDragging) {
                    drawFrictionFire(
                        fromX = textLeftX.dp.toPx(),
                        toX = (textLeftX + textWidthDp).dp.toPx(),
                        y = groundY,
                        phase = firePhase
                    )
                }
            }
            Text(
                text = "🚛",
                fontSize = 42.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = truckLeftX.dp)
                    .alpha(truckExitAlpha.value)
            )
        }
        Extruded3DText(
            text = "Bony Biswas",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = textLeftX.dp, y = (-4).dp)
        )
    }
}

private fun DrawScope.drawTowRope(fromX: Float, toX: Float, y: Float, alpha: Float) {
    if (toX <= fromX) return
    val midX = (fromX + toX) / 2f
    val path = Path().apply {
        moveTo(fromX, y)
        quadraticTo(midX, y + 10f, toX, y)
    }
    drawPath(
        path = path,
        color = Color(0xFF5B4636).copy(alpha = alpha),
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )
    val linkCount = 4
    for (i in 1 until linkCount) {
        val t = i / linkCount.toFloat()
        val x = fromX + (toX - fromX) * t
        drawCircle(
            color = Color(0xFF3A2E24).copy(alpha = alpha),
            radius = 4f,
            center = Offset(x, y + 10f * (4f * t * (1f - t)))
        )
    }
}

private fun DrawScope.drawExhaustSmoke(anchorX: Float, anchorY: Float, phase: Float) {
    repeat(3) { i ->
        val t = (phase + i / 3f) % 1f
        val rise = t * 40f
        val drift = t * 26f
        val alpha = (1f - t) * 0.55f
        val radius = 6f + t * 10f
        drawCircle(
            color = Color(0xFF8C8C8C).copy(alpha = alpha),
            radius = radius,
            center = Offset(anchorX + drift, anchorY - rise)
        )
    }
}

private fun DrawScope.drawFrictionFire(fromX: Float, toX: Float, y: Float, phase: Float) {
    if (toX <= fromX) return
    val tuftCount = 4
    for (i in 0 until tuftCount) {
        val baseT = i / (tuftCount - 1).toFloat()
        val x = fromX + (toX - fromX) * baseT
        val flicker = (phase + baseT * 0.6f) % 1f
        val height = 10f + flicker * 14f
        val sway = (flicker - 0.5f) * 8f
        val flamePath = Path().apply {
            moveTo(x - 6f, y)
            quadraticTo(x + sway, y - height, x, y - height * 1.6f)
            quadraticTo(x - sway, y - height, x + 6f, y)
            close()
        }
        val flameColor = if (flicker < 0.5f) Color(0xFFFF7A18) else Color(0xFFFFC93C)
        drawPath(flamePath, color = flameColor.copy(alpha = 0.85f))
        if (flicker > 0.7f) {
            drawLine(
                color = Color(0xFFFFE9A8),
                start = Offset(x, y - 2f),
                end = Offset(x + sway * 2f, y - 10f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun Extruded3DText(text: String, modifier: Modifier = Modifier) {
    val depth = 6
    Box(modifier = modifier) {
        for (i in depth downTo 1) {
            Text(
                text = text,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 22.sp,
                color = GoldShadeDark.copy(alpha = 1f - (i * 0.06f)),
                modifier = Modifier.offset(x = i.dp, y = i.dp)
            )
        }
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = 22.sp,
            color = GoldColor,
            style = LocalTextStyle.current.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.35f),
                    offset = Offset(2f, 2f),
                    blurRadius = 3f
                )
            )
        )
    }
}

private val GoldColor = Color(0xFFCC9A06)
private val GoldShadeDark = Color(0xFF7A5C04)

@Composable
private fun DeveloperInfoSection() {
    val uriHandler = LocalUriHandler.current
    Row(modifier = Modifier.fillMaxWidth()) {
        DeveloperContactCard(
            icon = { TelegramIcon() },
            name = "Telegram",
            handle = "t.me/ibyb007",
            onClick = { uriHandler.openUri("https://t.me/ibyb007") },
            modifier = Modifier.weight(1f)
        )
        DeveloperContactCard(
            icon = { GitHubIcon() },
            name = "GitHub",
            handle = "@ibyb007",
            onClick = { uriHandler.openUri("https://github.com/ibyb007") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DeveloperContactCard(
    icon: @Composable () -> Unit,
    name: String,
    handle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        icon()
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                handle,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open $name",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TelegramIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val radius = size.minDimension / 2f
        drawCircle(color = Color(0xFF29A9EB), radius = radius)
        val plane = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.52f)
            lineTo(size.width * 0.80f, size.height * 0.22f)
            lineTo(size.width * 0.64f, size.height * 0.80f)
            lineTo(size.width * 0.46f, size.height * 0.60f)
            lineTo(size.width * 0.34f, size.height * 0.70f)
            lineTo(size.width * 0.38f, size.height * 0.50f)
            close()
        }
        drawPath(plane, color = Color.White)
    }
}

@Composable
private fun GitHubIcon() {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(28.dp)) {
            drawCircle(color = Color(0xFF181717), radius = size.minDimension / 2f)
        }
        Text(
            ">_",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================================
// HARDWARE MAINTENANCE & STAFF MANAGEMENT SCREENS
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HardwareMaintenanceScreen(
    session: AppUserSession,
    currentRecord: DailyFuelRecord,
    onBack: () -> Unit,
    onRecordChanged: (DailyFuelRecord) -> Unit,
    topInset: Dp,
    bottomInset: Dp
) {
    var selectedShift by remember { mutableIntStateOf(1) }
    var selectedMpd by remember { mutableStateOf("MPD 1") }
    var selectedNozzle by remember { mutableStateOf("Petrol N2") }

    val activeShiftObj = when (selectedShift) {
        1 -> currentRecord.shift1
        2 -> currentRecord.shift2
        else -> currentRecord.shift3
    }
    val activeDispenser = if (selectedMpd == "MPD 1") activeShiftObj.mpd1 else activeShiftObj.mpd2
    val selectedNozzleObj = when (selectedNozzle) {
        "Petrol N2" -> activeDispenser.petrolN2
        "Petrol N3" -> activeDispenser.petrolN3
        "Diesel N1" -> activeDispenser.dieselN1
        else -> activeDispenser.dieselN4
    }

    val currentReading = selectedNozzleObj.open
    var newOpenValueInput by remember(currentReading, selectedShift, selectedMpd, selectedNozzle) {
        mutableStateOf(currentReading.toString())
    }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }

    LaunchedEffect(showConfirmDialog) {
        if (showConfirmDialog) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(topInset + 4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Hardware Meter Maintenance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Shift:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 3).forEach { shiftNum ->
                        FilterChip(
                            selected = selectedShift == shiftNum,
                            onClick = { selectedShift = shiftNum },
                            label = { Text("Shift $shiftNum", fontSize = 11.sp) }
                        )
                    }
                }

                Text("Select Dispenser Unit:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MPD 1", "MPD 2").forEach { mpd ->
                        FilterChip(
                            selected = selectedMpd == mpd,
                            onClick = { selectedMpd = mpd },
                            label = { Text(mpd, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Select Nozzle:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Petrol N2", "Petrol N3", "Diesel N1", "Diesel N4").forEach { nozzle ->
                        FilterChip(
                            selected = selectedNozzle == nozzle,
                            onClick = { selectedNozzle = nozzle },
                            label = { Text(nozzle, fontSize = 9.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Recorded Open Reading:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$currentReading L", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        if (selectedNozzleObj.isReset) {
                            Text("•R", color = Color(0xFFC62828), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = newOpenValueInput,
                    onValueChange = { input -> if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) newOpenValueInput = input },
                    label = { Text("New Hardware Open Reading (L)", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Apply Hardware Nozzle Reset", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (selectedNozzleObj.isReset) {
                    OutlinedButton(
                        onClick = {
                            val updatedRecord = applyNozzleUndoReset(currentRecord, selectedShift, selectedMpd, selectedNozzle)
                            onRecordChanged(updatedRecord)
                            ActivityLogger.log(session, "undid nozzle reset for $selectedMpd $selectedNozzle (Shift $selectedShift)")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Undo Last Reset (Restore ${selectedNozzleObj.originalOpenBeforeReset} L)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(bottomInset + 8.dp))
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("⚠️ Confirm Hardware Reset", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Resetting $selectedMpd $selectedNozzle (Shift $selectedShift) on ${currentRecord.date} to $newOpenValueInput L.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Previous shift/day readings will NOT be altered.\n• A RED '•R' indicator will mark this nozzle's open box.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    enabled = countdown == 0,
                    onClick = {
                        showConfirmDialog = false
                        val parsedVal = newOpenValueInput.toDoubleOrNull() ?: 0.0
                        val updatedRecord = applyNozzleReset(currentRecord, selectedShift, selectedMpd, selectedNozzle, parsedVal)
                        onRecordChanged(updatedRecord)
                        ActivityLogger.log(session, "reset $selectedMpd $selectedNozzle to $parsedVal L")
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (countdown > 0) "Confirm ($countdown s)" else "CONFIRM RESET", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

fun applyNozzleReset(
    record: DailyFuelRecord,
    shiftNumber: Int,
    mpdName: String,
    nozzleKey: String,
    newOpenValue: Double
): DailyFuelRecord {
    fun updateNozzle(nozzle: NozzleShift): NozzleShift {
        return nozzle.copy(
            open = newOpenValue,
            isReset = true,
            originalOpenBeforeReset = if (nozzle.originalOpenBeforeReset > 0.0) nozzle.originalOpenBeforeReset else nozzle.open
        )
    }

    fun updateDispenser(dispenser: DispenserShift): DispenserShift {
        return when (nozzleKey) {
            "Petrol N2" -> dispenser.copy(petrolN2 = updateNozzle(dispenser.petrolN2))
            "Petrol N3" -> dispenser.copy(petrolN3 = updateNozzle(dispenser.petrolN3))
            "Diesel N1" -> dispenser.copy(dieselN1 = updateNozzle(dispenser.dieselN1))
            "Diesel N4" -> dispenser.copy(dieselN4 = updateNozzle(dispenser.dieselN4))
            else -> dispenser
        }
    }

    fun updateShift(shift: DayShift): DayShift {
        return if (mpdName == "MPD 1") {
            shift.copy(mpd1 = updateDispenser(shift.mpd1))
        } else {
            shift.copy(mpd2 = updateDispenser(shift.mpd2))
        }
    }

    return when (shiftNumber) {
        1 -> record.copy(shift1 = updateShift(record.shift1), lastUpdatedTimestamp = System.currentTimeMillis())
        2 -> record.copy(shift2 = updateShift(record.shift2), lastUpdatedTimestamp = System.currentTimeMillis())
        else -> record.copy(shift3 = updateShift(record.shift3), lastUpdatedTimestamp = System.currentTimeMillis())
    }
}

fun applyNozzleUndoReset(
    record: DailyFuelRecord,
    shiftNumber: Int,
    mpdName: String,
    nozzleKey: String
): DailyFuelRecord {
    fun revertNozzle(nozzle: NozzleShift): NozzleShift {
        return nozzle.copy(
            open = if (nozzle.originalOpenBeforeReset > 0.0) nozzle.originalOpenBeforeReset else nozzle.open,
            isReset = false
        )
    }

    fun updateDispenser(dispenser: DispenserShift): DispenserShift {
        return when (nozzleKey) {
            "Petrol N2" -> dispenser.copy(petrolN2 = revertNozzle(dispenser.petrolN2))
            "Petrol N3" -> dispenser.copy(petrolN3 = revertNozzle(dispenser.petrolN3))
            "Diesel N1" -> dispenser.copy(dieselN1 = revertNozzle(dispenser.dieselN1))
            "Diesel N4" -> dispenser.copy(dieselN4 = revertNozzle(dispenser.dieselN4))
            else -> dispenser
        }
    }

    fun updateShift(shift: DayShift): DayShift {
        return if (mpdName == "MPD 1") {
            shift.copy(mpd1 = updateDispenser(shift.mpd1))
        } else {
            shift.copy(mpd2 = updateDispenser(shift.mpd2))
        }
    }

    return when (shiftNumber) {
        1 -> record.copy(shift1 = updateShift(record.shift1), lastUpdatedTimestamp = System.currentTimeMillis())
        2 -> record.copy(shift2 = updateShift(record.shift2), lastUpdatedTimestamp = System.currentTimeMillis())
        else -> record.copy(shift3 = updateShift(record.shift3), lastUpdatedTimestamp = System.currentTimeMillis())
    }
}

@Composable
private fun StaffManagementScreen(
    onBack: () -> Unit,
    topInset: Dp,
    bottomInset: Dp
) {
    var staffKeyList by remember { mutableStateOf<List<StaffAccessKey>>(emptyList()) }
    var isLoadingKeys by remember { mutableStateOf(true) }
    var showGenerateKeyDialog by remember { mutableStateOf(false) }
    var selectedQrKey by remember { mutableStateOf<StaffAccessKey?>(null) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("access_keys")
            .addSnapshotListener { snapshot, _ ->
                isLoadingKeys = false
                if (snapshot != null) {
                    staffKeyList = snapshot.documents.mapNotNull { it.toObject(StaffAccessKey::class.java) }
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Spacer(Modifier.height(topInset + 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    Text("Staff Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Button(onClick = { showGenerateKeyDialog = true }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), modifier = Modifier.height(34.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Generate Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoadingKeys) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (staffKeyList.isEmpty()) {
                Text("No staff access codes generated yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(staffKeyList, key = { it.id }) { keyItem ->
                        StaffKeyRowItem(
                            keyItem = keyItem,
                            onShowQr = { selectedQrKey = keyItem },
                            onUpdateKey = { updatedKey -> FirebaseFirestore.getInstance().collection("access_keys").document(updatedKey.id).set(updatedKey) },
                            onDeleteKey = { keyId -> FirebaseFirestore.getInstance().collection("access_keys").document(keyId).delete() }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(bottomInset + 8.dp))
    }

    if (showGenerateKeyDialog) {
        GenerateStaffKeyModal(
            onDismiss = { showGenerateKeyDialog = false },
            onSave = { newKey ->
                FirebaseFirestore.getInstance().collection("access_keys").document(newKey.id).set(newKey)
                showGenerateKeyDialog = false
            }
        )
    }

    selectedQrKey?.let { staffKey ->
        ViewQrCodeModal(staffKey = staffKey, onDismiss = { selectedQrKey = null })
    }
}

@Composable
private fun StaffKeyRowItem(
    keyItem: StaffAccessKey,
    onShowQr: () -> Unit,
    onUpdateKey: (StaffAccessKey) -> Unit,
    onDeleteKey: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var localReadOnly by remember(keyItem.isReadOnly) { mutableStateOf(keyItem.isReadOnly) }
    var localCanEditPast by remember(keyItem.canEditPastDates) { mutableStateOf(keyItem.canEditPastDates) }
    var localStatus by remember(keyItem.status) { mutableStateOf(keyItem.status) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(keyItem.nickname.ifBlank { "Staff Member" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Code: ${keyItem.accessCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onShowQr, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "View QR", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Key", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (localStatus == KeyStatus.ACTIVE) "Status: ACTIVE" else "Status: REVOKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (localStatus == KeyStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                Switch(checked = localStatus == KeyStatus.ACTIVE, onCheckedChange = { isActive ->
                    val newStatus = if (isActive) KeyStatus.ACTIVE else KeyStatus.REVOKED
                    localStatus = newStatus
                    onUpdateKey(keyItem.copy(status = newStatus))
                }, modifier = Modifier.height(20.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Read-Only Mode (No Data Entry)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(checked = localReadOnly, onCheckedChange = { readOnly ->
                    localReadOnly = readOnly
                    onUpdateKey(keyItem.copy(isReadOnly = readOnly))
                }, modifier = Modifier.height(20.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Past Date Edit Privilege", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(checked = localCanEditPast, onCheckedChange = { canEdit ->
                    localCanEditPast = canEdit
                    onUpdateKey(keyItem.copy(canEditPastDates = canEdit))
                }, modifier = Modifier.height(20.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Role Permission", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilterChip(
                    selected = keyItem.role == Role.ADMIN,
                    onClick = {
                        val nextRole = if (keyItem.role == Role.ADMIN) Role.MANAGER else Role.ADMIN
                        onUpdateKey(keyItem.copy(role = nextRole))
                    },
                    label = { Text(keyItem.role.name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.height(26.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Key?", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${keyItem.nickname}'?", fontSize = 12.sp) },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDeleteKey(keyItem.id) }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun GenerateStaffKeyModal(
    onDismiss: () -> Unit,
    onSave: (StaffAccessKey) -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(Role.MANAGER) }
    var canEditPastDates by remember { mutableStateOf(false) }
    var isReadOnly by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Generate Staff Access Key", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Staff Name *", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = selectedRole == Role.MANAGER, onClick = { selectedRole = Role.MANAGER }, label = { Text("Manager", fontSize = 10.sp) })
                    FilterChip(selected = selectedRole == Role.ADMIN, onClick = { selectedRole = Role.ADMIN }, label = { Text("Admin", fontSize = 10.sp) })
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Read-Only Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isReadOnly, onCheckedChange = { isReadOnly = it })
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Allow Editing Past Dates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = canEditPastDates, onCheckedChange = { canEditPastDates = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        if (nickname.isNotBlank()) {
                            val generatedCode = generateRandom8CharKey()
                            val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            onSave(StaffAccessKey(id = System.currentTimeMillis().toString(), accessCode = generatedCode, nickname = nickname.trim(), role = selectedRole, status = KeyStatus.ACTIVE, canEditPastDates = canEditPastDates, isReadOnly = isReadOnly, createdAt = nowStr))
                        }
                    }) { Text("Generate", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ViewQrCodeModal(staffKey: StaffAccessKey, onDismiss: () -> Unit) {
    val qrBitmap = remember(staffKey.accessCode) { generateQrCodeBitmap(staffKey.accessCode) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(staffKey.nickname, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Access Code: ${staffKey.accessCode}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(200.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private fun generateRandom8CharKey(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val first4 = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    val last4 = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    return "$first4-$last4"
}
