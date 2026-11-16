package com.irondiscipline.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.irondiscipline.app.domain.model.AppLockerItem
import com.irondiscipline.app.service.ScreenShareService
import com.irondiscipline.app.service.ScreenShareSessionManager
import com.irondiscipline.app.ui.components.AppLockerRowItem
import com.irondiscipline.app.ui.components.LuxuryBrushes
import com.irondiscipline.app.ui.components.LuxuryGlassCard
import com.irondiscipline.app.ui.components.LuxuryStatusPill
import com.irondiscipline.app.ui.components.jellyElastic
import com.irondiscipline.app.ui.theme.LuxuryCardBottom
import com.irondiscipline.app.ui.theme.LuxuryCardTop
import com.irondiscipline.app.ui.theme.LuxuryChampagneGold
import com.irondiscipline.app.ui.theme.LuxuryCrimsonAlert
import com.irondiscipline.app.ui.theme.LuxuryPlatinumSubtitle
import com.irondiscipline.app.ui.theme.LuxurySapphireCyan
import com.irondiscipline.app.ui.theme.LuxuryTacticalEmerald
import com.irondiscipline.app.util.RealtimeQuotaSnapshot
import kotlinx.coroutines.launch

@Composable
private fun SidebarFullScreenTopBar(
    title: String,
    subtitle: String,
    accentColor: Color,
    onBack: () -> Unit
) {
    LuxuryGlassCard(
        cornerRadius = 20.dp,
        borderBrush = Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.75f), LuxuryCardTop)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LuxuryCardTop, LuxuryCardBottom)
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(LuxuryChampagneGold, LuxurySapphireCyan)
                        ),
                        shape = CircleShape
                    )
                    .jellyElastic(baseCornerRadius = 22.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LuxuryChampagneGold,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White, LuxuryChampagneGold)
                        )
                    ),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.0.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Dedicated Screen 1 — Universal App Locker Vault Full Screen.
 */
@Composable
fun UniversalAppLockerFullScreen(
    isMasterPasswordEnabled: Boolean,
    lockedPackages: Set<String>,
    onSetMasterPassword: (String) -> Unit,
    onVerifyMasterPassword: (String) -> Boolean,
    onLockSessionNow: () -> Unit,
    onToggleCustomPackageLock: (String, Boolean) -> Unit,
    onLoadInstalledApps: suspend () -> List<AppLockerItem>,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) { onBack() }

    val coroutineScope = rememberCoroutineScope()
    var installedApps by remember { mutableStateOf<List<AppLockerItem>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var showChangePasswordForm by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordSuccessNote by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isMasterPasswordEnabled) {
        if (isMasterPasswordEnabled) {
            isLoadingApps = true
            installedApps = onLoadInstalledApps()
            isLoadingApps = false
        }
    }

    val filteredApps = remember(installedApps, lockedPackages, searchQuery) {
        val query = searchQuery.trim().lowercase()
        val list = if (query.isEmpty()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
        }
        list.sortedWith(
            compareByDescending<AppLockerItem> { lockedPackages.contains(it.packageName.trim().lowercase()) }
                .thenBy { it.appName.lowercase() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SidebarFullScreenTopBar(
            title = "UNIVERSAL APP LOCKER VAULT",
            subtitle = if (isMasterPasswordEnabled) {
                "${lockedPackages.size} Phone Apps + IronDiscipline Protected"
            } else {
                "Setup Master Password to Arm Vault"
            },
            accentColor = LuxuryChampagneGold,
            onBack = onBack
        )

        if (!isMasterPasswordEnabled) {
            // Full-Screen Master Password Setup Gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                LuxuryGlassCard(
                    cornerRadius = 24.dp,
                    borderBrush = LuxuryBrushes.activeHighlightBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(LuxuryChampagneGold.copy(alpha = 0.14f))
                                .border(1.5.dp, LuxuryChampagneGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LuxuryChampagneGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "CREATE MASTER PASSWORD",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.0.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Password enter karein (minimum 4 characters). Create hote hi IronDiscipline aur App Locker Vault protected ho jayega.",
                            color = LuxuryPlatinumSubtitle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = {
                                newPasswordInput = it
                                passwordError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text("New Master Password", fontSize = 12.sp) },
                            placeholder = { Text("At least 4 characters", color = Color(0xFF64748B)) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = LuxuryPlatinumSubtitle
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B0F17),
                                unfocusedContainerColor = Color(0xFF0B0F17),
                                focusedBorderColor = LuxuryChampagneGold,
                                unfocusedBorderColor = Color(0xFF26334D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                passwordError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            label = { Text("Confirm Master Password", fontSize = 12.sp) },
                            placeholder = { Text("Re-enter password", color = Color(0xFF64748B)) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B0F17),
                                unfocusedContainerColor = Color(0xFF0B0F17),
                                focusedBorderColor = LuxuryChampagneGold,
                                unfocusedBorderColor = Color(0xFF26334D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        AnimatedVisibility(
                            visible = passwordError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = passwordError.orEmpty(),
                                    color = LuxuryCrimsonAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val clean = newPasswordInput.trim()
                                val cleanConfirm = confirmPasswordInput.trim()
                                when {
                                    clean.length < 4 -> {
                                        passwordError = "Password kam az kam 4 characters ka hona chahiye."
                                    }
                                    clean != cleanConfirm -> {
                                        passwordError = "Passwords match nahi kar rahe. Dobara check karein."
                                    }
                                    else -> {
                                        passwordError = null
                                        newPasswordInput = ""
                                        confirmPasswordInput = ""
                                        onSetMasterPassword(clean)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .jellyElastic(baseCornerRadius = 14.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuxuryChampagneGold,
                                contentColor = Color(0xFF06080E)
                            )
                        ) {
                            Text(
                                text = "Create Password & Lock App",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Armed Vault: IronDiscipline Lock Card + Verify/Change Password + Searchable Installed Apps List
            LuxuryGlassCard(
                cornerRadius = 20.dp,
                borderBrush = Brush.linearGradient(
                    colors = listOf(LuxuryTacticalEmerald.copy(alpha = 0.7f), LuxurySapphireCyan.copy(alpha = 0.45f))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryTacticalEmerald.copy(alpha = 0.14f))
                                    .border(1.dp, LuxuryTacticalEmerald, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = LuxuryTacticalEmerald,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "IronDiscipline Self-Lock",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Password verified on open • ${lockedPackages.size} phone apps locked",
                                    color = LuxuryPlatinumSubtitle,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isMasterPasswordEnabled,
                            onCheckedChange = {
                                showChangePasswordForm = !showChangePasswordForm
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF06080E),
                                checkedTrackColor = LuxuryTacticalEmerald
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showChangePasswordForm = !showChangePasswordForm
                                passwordError = null
                                passwordSuccessNote = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .jellyElastic(baseCornerRadius = 11.dp),
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(1.dp, LuxuryChampagneGold.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (showChangePasswordForm) "Hide Password Form" else "Verify / Change Password",
                                color = LuxuryChampagneGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onLockSessionNow,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(38.dp)
                                .jellyElastic(baseCornerRadius = 11.dp),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LuxurySapphireCyan.copy(alpha = 0.16f),
                                contentColor = LuxurySapphireCyan
                            ),
                            border = BorderStroke(1.dp, LuxurySapphireCyan.copy(alpha = 0.65f)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lock App Now",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    AnimatedVisibility(visible = showChangePasswordForm) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = currentPasswordInput,
                                onValueChange = {
                                    currentPasswordInput = it
                                    passwordError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(11.dp),
                                label = { Text("Current Master Password", fontSize = 11.sp) },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = LuxuryPlatinumSubtitle
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B0F17),
                                    unfocusedContainerColor = Color(0xFF0B0F17),
                                    focusedBorderColor = LuxuryChampagneGold,
                                    unfocusedBorderColor = Color(0xFF26334D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = {
                                    newPasswordInput = it
                                    passwordError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(11.dp),
                                label = { Text("New Password (min 4 chars)", fontSize = 11.sp) },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B0F17),
                                    unfocusedContainerColor = Color(0xFF0B0F17),
                                    focusedBorderColor = LuxuryChampagneGold,
                                    unfocusedBorderColor = Color(0xFF26334D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    passwordError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(11.dp),
                                label = { Text("Confirm New Password", fontSize = 11.sp) },
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0B0F17),
                                    unfocusedContainerColor = Color(0xFF0B0F17),
                                    focusedBorderColor = LuxuryChampagneGold,
                                    unfocusedBorderColor = Color(0xFF26334D),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            if (!passwordError.isNullOrBlank()) {
                                Text(
                                    text = passwordError.orEmpty(),
                                    color = LuxuryCrimsonAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            if (!passwordSuccessNote.isNullOrBlank()) {
                                Text(
                                    text = passwordSuccessNote.orEmpty(),
                                    color = LuxuryTacticalEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val cleanNew = newPasswordInput.trim()
                                    val cleanConf = confirmPasswordInput.trim()
                                    when {
                                        !onVerifyMasterPassword(currentPasswordInput) -> {
                                            passwordError = "❌ Current password galat hai."
                                        }
                                        cleanNew.length < 4 -> {
                                            passwordError = "Password kam az kam 4 characters ka hona chahiye."
                                        }
                                        cleanNew != cleanConf -> {
                                            passwordError = "New passwords match nahi kar rahe."
                                        }
                                        else -> {
                                            passwordError = null
                                            currentPasswordInput = ""
                                            newPasswordInput = ""
                                            confirmPasswordInput = ""
                                            showChangePasswordForm = false
                                            onSetMasterPassword(cleanNew)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .jellyElastic(baseCornerRadius = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryChampagneGold,
                                    contentColor = Color(0xFF06080E)
                                )
                            ) {
                                Text(
                                    text = "Verify & Save Master Password",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar + Refresh Apps Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = {
                        Text(
                            text = "Search installed apps...",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = LuxuryPlatinumSubtitle
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = LuxuryPlatinumSubtitle
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0B0F17),
                        unfocusedContainerColor = Color(0xFF0B0F17),
                        focusedBorderColor = LuxurySapphireCyan,
                        unfocusedBorderColor = Color(0xFF26334D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoadingApps = true
                            installedApps = onLoadInstalledApps()
                            isLoadingApps = false
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141B29))
                        .border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Apps",
                        tint = LuxurySapphireCyan
                    )
                }
            }

            // Scrollable Installed Phone Apps List
            if (isLoadingApps) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LuxurySapphireCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading installed applications...",
                            color = LuxuryPlatinumSubtitle,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Koi app nahi mili." else "No launchable apps found.",
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isLocked = lockedPackages.contains(app.packageName.trim().lowercase())
                        AppLockerRowItem(
                            app = app,
                            isLocked = isLocked,
                            onToggleLock = { checked ->
                                onToggleCustomPackageLock(app.packageName, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Screen 2 — Live Screen & Audio Share Full Screen.
 * Active Sender broadcasts and Viewer streams persist in the background when navigating back.
 */
@Composable
fun LiveScreenShareFullScreen(
    onBack: () -> Unit
) {
    BackHandler(enabled = true) { onBack() }

    val context = LocalContext.current
    val viewerSessions by ScreenShareSessionManager.viewerSessions.collectAsState()
    var selectedTab by rememberSaveable {
        mutableIntStateOf(if (ScreenShareSessionManager.viewerSessions.value.isNotEmpty()) 1 else 0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        SidebarFullScreenTopBar(
            title = "LIVE SCREEN & AUDIO SHARE",
            subtitle = "🔒 AES-256-GCM Encrypted • Persistent Stream",
            accentColor = LuxurySapphireCyan,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp)),
            containerColor = Color(0xFF141C2A),
            contentColor = Color(0xFF38BDF8),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF38BDF8),
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "📤 Share My Screen",
                        fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Normal,
                        color = if (selectedTab == 0) Color(0xFF38BDF8) else Color(0xFF8A99AD),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = if (viewerSessions.isNotEmpty()) {
                            "📺 Watch Remote Screens (${viewerSessions.size})"
                        } else {
                            "📺 Watch Remote Screens"
                        },
                        fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Normal,
                        color = if (selectedTab == 1) Color(0xFF38BDF8) else Color(0xFF8A99AD),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (selectedTab == 0) {
                SenderScreenTab(context = context)
            } else {
                ViewerScreenTab()
            }
        }
    }
}

/**
 * Dedicated Screen 3 — Strict Protocol & Test Mode Control Center Full Screen.
 */
@Composable
fun StrictProtocolFullScreen(
    realtimeQuotaState: RealtimeQuotaSnapshot,
    onActivateOneMinuteTestMode: () -> Unit,
    onSwitchBackToNormalThirtyMinMode: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onConfirmUninstall: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) { onBack() }

    val isTestMode = realtimeQuotaState.isTestModeActive
    val isAnyAppLocked = realtimeQuotaState.anyAppLocked

    val modeAccentColor by animateColorAsState(
        targetValue = when {
            isAnyAppLocked -> LuxuryCrimsonAlert
            isTestMode -> LuxuryChampagneGold
            else -> LuxuryTacticalEmerald
        },
        animationSpec = tween(400),
        label = "StrictModeAccentColor"
    )

    var isPinVerificationExpanded by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var hasPinError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuxuryBrushes.primaryBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        SidebarFullScreenTopBar(
            title = "STRICT PROTOCOL & TEST MODE",
            subtitle = "Quota Enforcement • 1-Min Diagnostic • PIN Deletion",
            accentColor = LuxuryChampagneGold,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: ⚡ Strict Protocol: 30 Mins / App
            LuxuryGlassCard(
                cornerRadius = 22.dp,
                borderBrush = Brush.linearGradient(
                    colors = listOf(modeAccentColor.copy(alpha = 0.75f), Color(0xFF182232))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when {
                                    isAnyAppLocked -> Icons.Default.Lock
                                    isTestMode -> Icons.Default.Bolt
                                    else -> Icons.Default.Shield
                                },
                                contentDescription = null,
                                tint = modeAccentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "⚡ Strict Protocol: 30 Mins / App",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LuxuryStatusPill(
                        text = when {
                            isAnyAppLocked -> "🔒 UNBREAKABLE 5-HOUR LOCK ACTIVE"
                            isTestMode -> "⚡ DIAGNOSTIC TEST MODE: 1 MIN / APP"
                            else -> "🛡️ STRICT PROTOCOL ACTIVE: 30 MINS / APP"
                        },
                        accentColor = modeAccentColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• YouTube, Facebook, Instagram & TikTok each receive a strict 30-minute daily quota (1-minute in Diagnostic Test Mode).\n" +
                            "• Exhausting an app's quota triggers an unbreakable 5-hour wall-clock penalty lock that continues accurately even when the screen turns OFF.\n" +
                            "• Test Mode switching and quota resets remain disabled while any 5-hour penalty lock is active.",
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            // Card 2: 🧪 Activate 1-Min Test Mode (Auto-resets)
            LuxuryGlassCard(
                cornerRadius = 22.dp,
                borderBrush = if (isAnyAppLocked) {
                    Brush.linearGradient(colors = listOf(LuxuryCrimsonAlert.copy(alpha = 0.7f), Color(0xFF2E121A)))
                } else {
                    LuxuryBrushes.activeHighlightBorder
                }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isAnyAppLocked) LuxuryCrimsonAlert else LuxuryChampagneGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🧪 Activate 1-Min Test Mode (Auto-resets)",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAnyAppLocked) {
                            "A 5-hour penalty lock is currently active. Diagnostic mode switching is blocked until all 5-hour penalty timers expire."
                        } else if (isTestMode) {
                            "1-Minute Diagnostic Test Mode is active across YouTube, Facebook, Instagram & TikTok. Switch back to normal 30-Min Strict Mode anytime."
                        } else {
                            "Switch all 4 core social apps to a 1-minute diagnostic quota and auto-reset today's test timers for fast verification."
                        },
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!isAnyAppLocked) {
                                if (isTestMode) {
                                    onSwitchBackToNormalThirtyMinMode()
                                } else {
                                    onActivateOneMinuteTestMode()
                                }
                            }
                        },
                        enabled = !isAnyAppLocked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .jellyElastic(enabled = !isAnyAppLocked, baseCornerRadius = 14.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isAnyAppLocked -> Color(0xFF261017)
                                isTestMode -> Color(0xFF1E283A)
                                else -> Color(0xFF231C0F)
                            },
                            contentColor = when {
                                isAnyAppLocked -> LuxuryCrimsonAlert
                                isTestMode -> LuxuryTacticalEmerald
                                else -> LuxuryChampagneGold
                            },
                            disabledContainerColor = Color(0xFF261017),
                            disabledContentColor = LuxuryCrimsonAlert
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = when {
                                isAnyAppLocked -> LuxuryCrimsonAlert
                                isTestMode -> LuxuryTacticalEmerald
                                else -> LuxuryChampagneGold.copy(alpha = 0.85f)
                            }
                        )
                    ) {
                        Icon(
                            imageVector = when {
                                isAnyAppLocked -> Icons.Default.Lock
                                isTestMode -> Icons.Default.Shield
                                else -> Icons.Default.Bolt
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isAnyAppLocked -> "🔒 LOCKED: Test Mode Disabled Until 5-Hr Lock Expires"
                                isTestMode -> "🛡️ Switch to 30-Min Normal Mode (Auto-Resets)"
                                else -> "🧪 Activate 1-Min Test Mode (Auto-resets)"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Card 3: 🗑️ Delete App (Enter PIN) — Inline 4-digit PIN verification flow
            LuxuryGlassCard(
                cornerRadius = 22.dp,
                borderBrush = Brush.linearGradient(
                    colors = listOf(LuxuryCrimsonAlert.copy(alpha = 0.8f), Color(0xFF261017))
                ),
                surfaceBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF200E15), Color(0xFF12080D))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = LuxuryCrimsonAlert,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🗑️ Delete App (Enter PIN)",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your 4-digit security PIN (Default PIN: 1234) to disarm Device Admin protection, clear lock overlays, and launch Android uninstallation.",
                        color = LuxuryPlatinumSubtitle,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isPinVerificationExpanded) {
                        Button(
                            onClick = {
                                enteredPin = ""
                                hasPinError = false
                                isPinVerificationExpanded = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .jellyElastic(enabled = true, baseCornerRadius = 13.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF281018),
                                contentColor = LuxuryCrimsonAlert
                            ),
                            border = BorderStroke(1.dp, LuxuryCrimsonAlert.copy(alpha = 0.85f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🗑️ Delete App (Enter PIN)",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                if (it.length <= 10) {
                                    enteredPin = it
                                    hasPinError = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (onVerifyPin(enteredPin)) {
                                        onConfirmUninstall()
                                    } else {
                                        hasPinError = true
                                    }
                                }
                            ),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = "••••",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = LuxuryPlatinumSubtitle.copy(alpha = 0.5f),
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            isError = hasPinError,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0C1019),
                                unfocusedContainerColor = Color(0xFF0C1019),
                                focusedBorderColor = LuxuryCrimsonAlert,
                                unfocusedBorderColor = Color(0xFF26334D),
                                errorBorderColor = LuxuryCrimsonAlert,
                                cursorColor = LuxuryCrimsonAlert
                            )
                        )

                        if (hasPinError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "❌ Incorrect PIN! App cannot be deleted.",
                                color = LuxuryCrimsonAlert,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    enteredPin = ""
                                    hasPinError = false
                                    isPinVerificationExpanded = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .jellyElastic(baseCornerRadius = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF3B4860)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = LuxuryPlatinumSubtitle
                                )
                            ) {
                                Text(
                                    text = "Cancel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (onVerifyPin(enteredPin)) {
                                        onConfirmUninstall()
                                    } else {
                                        hasPinError = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                                    .jellyElastic(baseCornerRadius = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryCrimsonAlert,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Unlock & Delete App",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EncryptionStatusBadge() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF081C15))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "🛡️ AES-256-GCM Encrypted (Video + Audio Protected)",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "End-to-End Encrypted: Koi teesra phone ya server aapki screen ya aawaz nahi sun sakta.",
                    color = Color(0xFF86EFAC),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SenderScreenTab(context: Context) {
    val isSharing by ScreenShareSessionManager.isSenderActive.collectAsState()
    val activeRoomCode by ScreenShareSessionManager.senderRoomCode.collectAsState()
    val framesSent by ScreenShareSessionManager.senderFramesSent.collectAsState()
    val statusText by ScreenShareSessionManager.senderStatusText.collectAsState()
    val selectedAudioMode by ScreenShareSessionManager.senderAudioMode.collectAsState()

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, ScreenShareService::class.java).apply {
                action = ScreenShareService.ACTION_START_SHARE
                putExtra(ScreenShareService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenShareService.EXTRA_RESULT_DATA, result.data)
                putExtra(ScreenShareService.EXTRA_ROOM_CODE, activeRoomCode)
                putExtra(ScreenShareService.EXTRA_AUDIO_MODE, selectedAudioMode)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        val mpManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mpManager != null) {
            mediaProjectionLauncher.launch(mpManager.createScreenCaptureIntent())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EncryptionStatusBadge()
        Spacer(modifier = Modifier.height(14.dp))

        // Device Name Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1522))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x2638BDF8))
                        .border(1.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "YOUR PHONE'S BROADCAST NAME",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "📱 ${ScreenShareService.getLocalPhoneName()}",
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isSharing) {
            // Room Code Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF26334D), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1522))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR 6-DIGIT ROOM CODE",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeRoomCode,
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { ScreenShareSessionManager.regenerateSenderRoomCode() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Room Code",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share this 6-digit code with the viewer. They can add multiple phones to watch simultaneously!",
                        color = Color(0xFF8A99AD),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Mode Selector (4 options in a clean 2x2 grid)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111824))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎙️ AUDIO STREAMING MODE",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select what sound is broadcasted (switchable live anytime):",
                        color = Color(0xFF8A99AD),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val row1Modes = listOf(
                        ScreenShareService.AUDIO_MODE_INTERNAL to "🎵 Internal Sound",
                        ScreenShareService.AUDIO_MODE_MIC to "🎙️ Mic Voice"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1Modes.forEach { (mode, label) ->
                            val isSelected = selectedAudioMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF182232))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF26334D),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .jellyElastic(baseCornerRadius = 10.dp)
                                    .clickable { ScreenShareSessionManager.setSenderAudioMode(mode) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF06080E) else Color(0xFFBAC7D5),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val row2Modes = listOf(
                        ScreenShareService.AUDIO_MODE_BOTH to "🔊 Both (Mix)",
                        ScreenShareService.AUDIO_MODE_NONE to "🔇 None (Silent)"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2Modes.forEach { (mode, label) ->
                            val isSelected = selectedAudioMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF182232))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF26334D),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .jellyElastic(baseCornerRadius = 10.dp)
                                    .clickable { ScreenShareSessionManager.setSenderAudioMode(mode) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF06080E) else Color(0xFFBAC7D5),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111824))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔒 PRIVATE & ENCRYPTED STREAM",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Click 'Start Screen Sharing' below and approve Android's screen capture prompt.\n" +
                            "2. Video and device name are encrypted with AES-256-GCM using your Room Code key.\n" +
                            "3. Audio mode can be switched dynamically between Internal, Mic, Both, or Silent anytime.\n" +
                            "4. Broadcast runs 24/7 persistently in the background until you explicitly tap 'Stop Screen Sharing'.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val needsMic = selectedAudioMode == ScreenShareService.AUDIO_MODE_MIC || selectedAudioMode == ScreenShareService.AUDIO_MODE_BOTH
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (needsMic && !hasMicPermission) {
                        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        val mpManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                        if (mpManager != null) {
                            mediaProjectionLauncher.launch(mpManager.createScreenCaptureIntent())
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .jellyElastic(baseCornerRadius = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    contentColor = Color(0xFF06080E)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "▶️ Start Screen Sharing",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        } else {
            // Live Broadcasting Active State
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1B16))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300E676))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🟢 LIVE BROADCAST ACTIVE",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "ROOM #$activeRoomCode",
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Frames Sent: $framesSent  •  Status: $statusText",
                        color = Color(0xFF8A99AD),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Live Audio Mode Switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111824))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎙️ LIVE AUDIO CONTROL: ",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedAudioMode,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val allModes = listOf(
                        ScreenShareService.AUDIO_MODE_INTERNAL to "🎵 Internal",
                        ScreenShareService.AUDIO_MODE_MIC to "🎙️ Mic",
                        ScreenShareService.AUDIO_MODE_BOTH to "🔊 Both",
                        ScreenShareService.AUDIO_MODE_NONE to "🔇 Silent"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allModes.forEach { (mode, label) ->
                            val isSelected = selectedAudioMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF182232))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF26334D),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .jellyElastic(baseCornerRadius = 8.dp)
                                    .clickable { ScreenShareSessionManager.setSenderAudioMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF06080E) else Color(0xFFBAC7D5),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111824))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📱 BACKGROUND STREAMING ACTIVE",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Broadcasting live as '${ScreenShareService.getLocalPhoneName()}'. You can minimize this app or lock the screen without interrupting the broadcast.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    ScreenShareService.stopSharing(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .jellyElastic(baseCornerRadius = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2E54),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⏹️ Stop Screen Sharing",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ViewerScreenTab() {
    val context = LocalContext.current

    val viewerSessions by ScreenShareSessionManager.viewerSessions.collectAsState()
    val selectedTabRoomCode by ScreenShareSessionManager.selectedTabRoomCode.collectAsState()

    var inputRoomCode by rememberSaveable { mutableStateOf("") }
    val isInputValid = inputRoomCode.length == 6

    val activeSession = viewerSessions.find { it.roomCode == selectedTabRoomCode } ?: viewerSessions.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EncryptionStatusBadge()
        Spacer(modifier = Modifier.height(14.dp))

        // Top Connect Bar: Always visible so user can add multiple phones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF26334D), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1522))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "CONNECT TO SENDER PHONE",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter a 6-digit Room Code to watch live. You can connect to multiple phones simultaneously:",
                    color = Color(0xFF8A99AD),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputRoomCode,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                                inputRoomCode = newValue
                            }
                        },
                        placeholder = {
                            Text(
                                text = "e.g. 482910",
                                color = Color(0xFF5E6D82),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF26334D),
                            focusedContainerColor = Color(0xFF060910),
                            unfocusedContainerColor = Color(0xFF060910)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            ScreenShareSessionManager.addOrSelectViewerPhone(context, inputRoomCode)
                            inputRoomCode = ""
                        },
                        enabled = isInputValid,
                        modifier = Modifier
                            .height(52.dp)
                            .jellyElastic(baseCornerRadius = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF38BDF8),
                            disabledContainerColor = Color(0xFF1E2838),
                            contentColor = Color(0xFF06080E),
                            disabledContentColor = Color(0xFF5E6D82)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "➕ Add Phone",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Phone Horizontal Device Tabs Bar
        if (viewerSessions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewerSessions.forEach { session ->
                    val isSelected = session.roomCode == activeSession?.roomCode
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF26334D),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { ScreenShareSessionManager.selectViewerTab(session.roomCode) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF162235) else Color(0xFF0F1522)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val statusDot = if (session.isStreamAvailable) "🟢" else "🔴"
                            val dotColor = if (session.isStreamAvailable) Color(0xFF00E676) else Color(0xFFFF2E54)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$statusDot 📱 ${session.phoneName}",
                                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF8A99AD),
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33FF2E54))
                                    .clickable {
                                        ScreenShareSessionManager.disconnectSinglePhoneTab(session.roomCode)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Disconnect tab",
                                    tint = Color(0xFFFF2E54),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active Session View or Empty State
        if (activeSession != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070A10))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (activeSession.isStreamAvailable) Color(0xFF00E676) else Color(0xFFFF2E54))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (activeSession.isStreamAvailable) "🟢" else "🔴"} 📱 ${activeSession.phoneName}",
                                    color = Color(0xFFFFFFFF),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = if (activeSession.isStreamAvailable) "AES-256-GCM Encrypted • ${activeSession.status}" else "🔴 Stream Offline • Waiting for Internet / Reconnect...",
                                color = if (activeSession.isStreamAvailable) Color(0xFF00E676) else Color(0xFFFF2E54),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { ScreenShareSessionManager.toggleMuteForPhone(activeSession.roomCode) },
                                modifier = Modifier
                                    .height(34.dp)
                                    .jellyElastic(baseCornerRadius = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (activeSession.isAudioMuted) Color(0xFFFF2E54) else Color(0xFF38BDF8)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (activeSession.isAudioMuted) Color(0xFFFF2E54) else Color(0xFF38BDF8)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = if (activeSession.isAudioMuted) "🔇 Audio: MUTED" else "🔊 Audio: ON",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { ScreenShareSessionManager.disconnectSinglePhoneTab(activeSession.roomCode) },
                                modifier = Modifier
                                    .height(34.dp)
                                    .jellyElastic(baseCornerRadius = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF2E54)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFFF2E54)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "❌ Disconnect",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remote Video Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030509))
                            .border(1.dp, Color(0xFF1B2433), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeSession.isStreamAvailable && activeSession.latestFrame != null) {
                            Image(
                                bitmap = activeSession.latestFrame.asImageBitmap(),
                                contentDescription = "Remote Screen Stream for ${activeSession.phoneName}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else if (!activeSession.isStreamAvailable) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.5.dp, Color(0xFFFF2E54)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141B29))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📡❌",
                                        fontSize = 42.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Screen is not available because internet/data is turn off",
                                        color = Color(0xFFFFFFFF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Jaise hi internet/data dobara connect hoga, live screen khud-ba-khud wapas visible ho jayegi...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "📡 Waiting for live frames...",
                                    color = Color(0xFFFFFFFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Make sure ${activeSession.phoneName} on Room #${activeSession.roomCode} is sharing.",
                                    color = Color(0xFF8A99AD),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E2838), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111824))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO PHONES CONNECTED YET",
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter a 6-digit Room Code in the top bar and tap '➕ Add Phone' to watch. You can connect to multiple phones at the same time and switch between them using tabs!",
                        color = Color(0xFF8A99AD),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
