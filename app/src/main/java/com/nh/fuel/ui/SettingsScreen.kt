package com.nh.fuel.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.nh.fuel.data.AppUserSession
import com.nh.fuel.data.KeyStatus
import com.nh.fuel.data.Role
import com.nh.fuel.data.StaffAccessKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    session: AppUserSession,
    currentOpacity: Float,
    currentThemeMode: ThemeMode,
    onOpacityChanged: (Float) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit = {},
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp
) {
    var sliderValue by remember(currentOpacity) { mutableFloatStateOf(currentOpacity) }
    var showStaffManagementPage by remember { mutableStateOf(false) }

    val canAccessAdminPanel = session.isOwnerLogin || session.role == Role.SUPER_ADMIN || session.role == Role.ADMIN

    if (showStaffManagementPage && canAccessAdminPanel) {
        StaffManagementScreen(
            onBack = { showStaffManagementPage = false },
            topInset = topInset,
            bottomInset = bottomInset
        )
    } else {
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

                // Staff Access & Roles Navigation Card
                if (canAccessAdminPanel) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { showStaffManagementPage = true },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text("Staff Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Button(
                    onClick = { showGenerateKeyDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Generate Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoadingKeys) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (staffKeyList.isEmpty()) {
                Text("No staff access codes generated yet. Tap 'Generate Code' above.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(staffKeyList, key = { it.id }) { keyItem ->
                        StaffKeyRowItem(
                            keyItem = keyItem,
                            onShowQr = { selectedQrKey = keyItem },
                            onUpdateKey = { updatedKey ->
                                FirebaseFirestore.getInstance()
                                    .collection("access_keys")
                                    .document(updatedKey.id)
                                    .set(updatedKey)
                            },
                            onDeleteKey = { keyId ->
                                FirebaseFirestore.getInstance()
                                    .collection("access_keys")
                                    .document(keyId)
                                    .delete()
                            }
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
    onUpdateKey: (StaffAccessKey) -> Unit,
    onDeleteKey: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(keyItem.nickname.ifBlank { "Staff Member" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = "Code: ${keyItem.accessCode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
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

            // Row 1: Active Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (keyItem.status == KeyStatus.ACTIVE) "Status: ACTIVE" else "Status: REVOKED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (keyItem.status == KeyStatus.ACTIVE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Switch(
                    checked = keyItem.status == KeyStatus.ACTIVE,
                    onCheckedChange = { isActive ->
                        onUpdateKey(keyItem.copy(status = if (isActive) KeyStatus.ACTIVE else KeyStatus.REVOKED))
                    },
                    modifier = Modifier.height(20.dp)
                )
            }

            // Row 2: Read-Only Privilege Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Read-Only Mode (No Data Entry)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(
                    checked = keyItem.isReadOnly,
                    onCheckedChange = { readOnly ->
                        onUpdateKey(keyItem.copy(isReadOnly = readOnly))
                    },
                    modifier = Modifier.height(20.dp)
                )
            }

            // Row 3: Past Date Edit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Past Date Edit Privilege", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Switch(
                    checked = keyItem.canEditPastDates,
                    onCheckedChange = { canEdit ->
                        onUpdateKey(keyItem.copy(canEditPastDates = canEdit))
                    },
                    modifier = Modifier.height(20.dp)
                )
            }

            // Row 4: Role Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            title = { Text("Delete & Revoke Key?", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${keyItem.nickname}'? They will be logged out permanently.", fontSize = 12.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteKey(keyItem.id)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
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
                    Text("Read-Only Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isReadOnly, onCheckedChange = { isReadOnly = it })
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
                                    isReadOnly = isReadOnly,
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
