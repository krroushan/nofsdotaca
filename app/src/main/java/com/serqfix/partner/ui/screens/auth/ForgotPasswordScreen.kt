package com.serqfix.partner.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.ui.theme.PartnerAppTheme
import com.serqfix.partner.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var isEmail by remember { mutableStateOf(true) }
    val uiState by authViewModel.uiState.collectAsState()
    
    PartnerAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Forgot Password") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    text = "Enter your email or phone number to receive password reset instructions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Toggle between email and phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isEmail,
                        onClick = { isEmail = true },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    FilterChip(
                        selected = !isEmail,
                        onClick = { isEmail = false },
                        label = { Text("Phone") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text(if (isEmail) "Email" else "Phone Number") },
                    placeholder = { Text("Enter ${if (isEmail) "email" else "phone number"}") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = if (isEmail) KeyboardType.Email else KeyboardType.Phone
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            if (isEmail) Icons.Default.Email else Icons.Default.Phone,
                            contentDescription = null
                        )
                    },
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        // TODO: Implement forgot password logic
                        navController.navigate(NavRoute.VerifyOtpForgotPassword.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && emailOrPhone.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send Reset Code")
                    }
                }
                
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
