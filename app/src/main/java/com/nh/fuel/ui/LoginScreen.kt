package com.nh.fuel.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nh.fuel.data.AccountStatus
import com.nh.fuel.data.AppUser
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (AppUser) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )

                Text(
                    text = "NH FUEL STATION",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "Authorized Staff Login",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                performGoogleSignIn(
                                    context = context,
                                    onSuccess = { user ->
                                        isLoading = false
                                        onLoginSuccess(user)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private suspend fun performGoogleSignIn(
    context: Context,
    onSuccess: (AppUser) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(context)
        
        // Note: WEB_CLIENT_ID will be replaced by your Firebase Web Client ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("YOUR_FIREBASE_WEB_CLIENT_ID.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        val googleIdToken = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")

        if (googleIdToken != null) {
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential)
            val signedInUser = authResult.user

            if (signedInUser?.email != null) {
                // Check if email exists in Whitelisted Users Firestore collection
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(signedInUser.email!!)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val user = doc.toObject(AppUser::class.java)
                            if (user != null && user.status == AccountStatus.ACTIVE) {
                                onSuccess(user)
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                onError("Access Denied: Account is suspended or inactive.")
                            }
                        } else {
                            FirebaseAuth.getInstance().signOut()
                            onError("Access Denied: '${signedInUser.email}' is not authorized by Admin.")
                        }
                    }
                    .addOnFailureListener {
                        onError("Network error verifying user access: ${it.localizedMessage}")
                    }
            } else {
                onError("Failed to retrieve Google user email.")
            }
        } else {
            onError("Failed to retrieve Google ID Token.")
        }
    } catch (e: Exception) {
        onError("Login Error: ${e.localizedMessage}")
    }
}
