package com.nh.fuel.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.nh.fuel.data.KeyStatus
import com.nh.fuel.data.Role
import com.nh.fuel.data.StaffAccessKey
import com.nh.fuel.data.ThemeMode // FIXED: Explicit ThemeMode data model import
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SettingsScreen(
    currentOpacity: Float,
    currentThemeMode: ThemeMode,
    onOpacityChanged: (Float) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp
) {
    var sliderValue by remember(currentOpacity) { mutableFloatStateOf(currentOpacity) }
    var showGenerateKeyDialog by remember { mutableStateOf(false) }
    var selectedQrKey by remember { mutableStateOf<StaffAccessKey?>(null) }

    var staffKeyList by remember { mutableStateOf<List<StaffAccessKey>>(emptyList()) }
    var isLoadingKeys by remember { mutableStateOf(true) }

    // Fetch Key List from Firestore
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("access_keys")
            .addSnapshotListener { snapshot, error ->
                isLoadingKeys = false
                if (snapshot != null) {
                    staffKeyList = snapshot.documents.mapNotNull { it.toObject(StaffAccessKey::class.java) }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.height(topInset + 4.dp))

            Text(
                text = "App Settings & Admin Panel",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Admin Panel Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Staff Access & Roles", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = { showGenerateKeyDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    if (isLoadingKeys) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    } else if (staffKeyList.isEmpty()) {
                        Text("No staff access codes generated yet. Tap 'Generate Code' above.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            items(staffKeyList, key = { it.id }) { keyItem ->
                                StaffKeyRowItem(
                                    keyItem = keyItem,
                                    onShowQr = { selectedQrKey = keyItem },
                                    onToggleStatus = { updatedKey ->
                                        FirebaseFirestore.getInstance()
                                            .collection("access_keys")
                                            .document(updatedKey.id)
                                            .set(updatedKey)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Cloud & Local Backup Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Cloud & Drive Backup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("Automatic Google Drive cloud sync and one-click data restore.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Compact Bottom Nav Bar Opacity Bar
        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nav Bar Opacity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                onOpacityChanged(it)
                            },
                            valueRange = 0.2f..1.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "${(sliderValue * 100).roundToInt()}%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(bottomInset + 8.dp))
        }
    }

    if (showGenerateKeyDialog) {
        GenerateStaffKeyModal(
            onDismiss = { showGenerateKeyDialog = false },
            onSave = { newKey ->
                FirebaseFirestore.getInstance()
                    .collection("access_keys")
                    .document(newKey.id)
                    .set(newKey)
                showGenerateKeyDialog = false
            }
        )
    }

    selectedQrKey?.let { staffKey ->
        ViewQrCodeModal(
            staffKey = staffKey,
            onDismiss = { selectedQrKey = null }
        )
    }
}

@Composable
private fun StaffKeyRowItem(
    keyItem: StaffAccessKey,
    onShowQr: () -> Unit,
    onToggleStatus: (StaffAccessKey) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(keyItem.nickname.ifBlank { "Staff Member" }, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = "Code: ${keyItem.accessCode} | Role: ${keyItem.role.name}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Past Date Edit: ${if (keyItem.canEditPastDates) "ON" else "OFF"}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onShowQr, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.QrCode, contentDescription = "View QR", modifier = Modifier.size(16.dp))
                }

                Switch(
                    checked = keyItem.status == KeyStatus.ACTIVE,
                    onCheckedChange = { isActive ->
                        onToggleStatus(keyItem.copy(status = if (isActive) KeyStatus.ACTIVE else KeyStatus.REVOKED))
                    },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Generate Staff Access Key", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Staff Name / Shift Nickname *", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedRole == Role.MANAGER,
                        onClick = { selectedRole = Role.MANAGER },
                        label = { Text("Manager", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedRole == Role.ADMIN,
                        onClick = { selectedRole = Role.ADMIN },
                        label = { Text("Admin", fontSize = 10.sp) }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Allow Editing Past Dates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = canEditPastDates, onCheckedChange = { canEditPastDates = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nickname.isNotBlank()) {
                                val generatedCode = generateRandom8CharKey()
                                val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val newKey = StaffAccessKey(
                                    id = System.currentTimeMillis().toString(),
                                    accessCode = generatedCode,
                                    nickname = nickname.trim(),
                                    role = selectedRole,
                                    status = KeyStatus.ACTIVE,
                                    canEditPastDates = canEditPastDates,
                                    createdAt = nowStr
                                )
                                onSave(newKey)
                            }
                        }
                    ) { Text("Generate", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ViewQrCodeModal(
    staffKey: StaffAccessKey,
    onDismiss: () -> Unit
) {
    val qrBitmap = remember(staffKey.accessCode) {
        generateQrCodeBitmap(staffKey.accessCode)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(staffKey.nickname, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Access Code: ${staffKey.accessCode}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)

                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Access QR Code",
                    modifier = Modifier.size(200.dp)
                )

                Text("Point manager's phone camera at this QR code on the Login Screen to log in instantly.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
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
