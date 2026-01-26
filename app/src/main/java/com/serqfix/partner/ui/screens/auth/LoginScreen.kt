package com.serqfix.partner.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serqfix.partner.R
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.ui.theme.PartnerAppTheme
import com.serqfix.partner.ui.theme.Primary
import com.serqfix.partner.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    var loginMethod by remember { mutableStateOf("phone-otp") } // Default to phone-otp (tabs hidden)
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var showOtpInput by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }
    var otpRequested by remember { mutableStateOf(false) }

    // Clear errors when input changes
    LaunchedEffect(phoneNumber) {
        phoneError = null
        generalError = null
    }
    LaunchedEffect(otp) {
        otpError = null
        generalError = null
    }
    LaunchedEffect(email) {
        emailError = null
        generalError = null
    }
    LaunchedEffect(password) {
        passwordError = null
        generalError = null
    }

    // Handle API errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            generalError = when {
                error.contains("Service provider not found", ignoreCase = true) -> 
                    "No account found with this phone number. Please check or register."
                error.contains("Invalid OTP", ignoreCase = true) -> 
                    "The OTP you entered is incorrect. Please try again."
                error.contains("OTP has expired", ignoreCase = true) -> 
                    "Your OTP has expired. Please request a new one."
                else -> error
            }
        }
    }

    // Navigate on successful login with post-login logic
    // Use both isLoggedIn and userData as keys to ensure it triggers when both are set
    LaunchedEffect(key1 = uiState.isLoggedIn, key2 = uiState.userData) {
        android.util.Log.d("LoginScreen", "LaunchedEffect triggered - isLoggedIn: ${uiState.isLoggedIn}, userData: ${uiState.userData != null}, userData._id: ${uiState.userData?._id}, hasNavigated: $hasNavigated")
        
        if (uiState.isLoggedIn && uiState.userData != null && !hasNavigated) {
            android.util.Log.d("LoginScreen", "Starting navigation logic...")
            hasNavigated = true
            val userData = uiState.userData!!
            
            // Debug logging to see actual user data values
            android.util.Log.d("LoginScreen", "Navigation check - active: ${userData.active} (type: ${userData.active?.javaClass?.simpleName}), isVerified: ${userData.isVerified} (type: ${userData.isVerified?.javaClass?.simpleName}), agreementOpen: ${userData.agreementOpen} (type: ${userData.agreementOpen?.javaClass?.simpleName}), agreementSigned: ${userData.agreementSigned} (type: ${userData.agreementSigned?.javaClass?.simpleName})")
            
            // Post-login navigation logic exactly matching React Native structure
            // React Native uses: if (!userData.active) { ... } else { if (!userData.isVerified) { ... } else { ... } }
            // In Kotlin: !active means active == false || active == null (treat null as falsy like JavaScript)
            
            // For debugging: Check if we should go to Home based on API response
            val shouldGoToHome = userData.active == true && 
                                 (userData.isVerified == true || userData.isVerified == null) &&
                                 (userData.agreementSigned == true || userData.agreementOpen == false)
            android.util.Log.d("LoginScreen", "Should go to Home: $shouldGoToHome")
            
            if (userData.active != true) {
                // User is not active (false or null)
                android.util.Log.d("LoginScreen", "User not active (active=${userData.active}), checking verification status")
                if (userData.isVerified != true) {
                    // Verification incomplete
                    android.util.Log.d("LoginScreen", "Navigating to VerificationOptions (not verified)")
                    navController.navigate(NavRoute.VerificationOptions.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                } else {
                    // Verification complete but account not active
                    android.util.Log.d("LoginScreen", "Navigating to UnderReview (verified but not active)")
                    navController.navigate(NavRoute.UnderReview.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                }
            } else {
                // User is active (true)
                android.util.Log.d("LoginScreen", "User is active, checking verification status")
                if (userData.isVerified != true) {
                    // Verification incomplete
                    android.util.Log.d("LoginScreen", "Navigating to VerificationOptions (active but not verified)")
                    navController.navigate(NavRoute.VerificationOptions.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                } else {
                    // User is active and verified - check agreement status
                    android.util.Log.d("LoginScreen", "User is active and verified, checking agreement status")
                    if (userData.agreementOpen == true) {
                        // Agreement is open (needs signing)
                        android.util.Log.d("LoginScreen", "Agreement open, navigating to UnderReview")
                        navController.navigate(NavRoute.UnderReview.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    } else if (userData.agreementSigned == true || userData.agreementOpen == false) {
                        // Agreement is signed or not open, go to Home
                        android.util.Log.d("LoginScreen", "Agreement signed/closed (agreementSigned=${userData.agreementSigned}, agreementOpen=${userData.agreementOpen}), navigating to TabNavigator (Home)")
                        try {
                            navController.navigate(NavRoute.TabNavigator.route) {
                                popUpTo(NavRoute.Login.route) { inclusive = true }
                            }
                            android.util.Log.d("LoginScreen", "Navigation to TabNavigator executed successfully")
                        } catch (e: Exception) {
                            android.util.Log.e("LoginScreen", "Error navigating to TabNavigator", e)
                        }
                    } else {
                        // Agreement status unknown/null - go to UnderReview to check (matching React Native)
                        android.util.Log.d("LoginScreen", "Agreement status unknown (agreementSigned=${userData.agreementSigned}, agreementOpen=${userData.agreementOpen}), navigating to UnderReview")
                        navController.navigate(NavRoute.UnderReview.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
    
    PartnerAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFFFFFF)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top padding (10% of screen - approximated)
                Spacer(modifier = Modifier.height(48.dp))
                
                // Logo Section
                // Note: Logo will only display if R.drawable.logo exists
                // If logo resource doesn't exist, comment out the Image block below
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Uncomment below if logo resource exists
                    // Image(
                    //     painter = painterResource(id = R.drawable.logo),
                    //     contentDescription = "App Logo",
                    //     modifier = Modifier
                    //         .size(130.dp)
                    //         .clip(RoundedCornerShape(100.dp)),
                    //     contentScale = ContentScale.Crop
                    // )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Content Wrapper (Card)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0x40000000)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Text
                        Text(
                            text = "Welcome Back!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Sign in to continue",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        // Login Method Tabs - HIDDEN (as per React Native)
                        // Default to phone-otp only
                        
                        // Login Component Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            // Phone OTP Login
                            if (loginMethod == "phone-otp") {
                                if (!showOtpInput) {
                                    // Step 1: Phone Input
                                    Column {
                                        Text(
                                            text = "Your Phone Number",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        TextField(
                                            value = phoneNumber,
                                            onValueChange = { 
                                                if (it.length <= 10) {
                                                    phoneNumber = it.filter { char -> char.isDigit() }
                                                }
                                            },
                                            placeholder = { Text("Enter 10-digit phone number") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                                .clip(RoundedCornerShape(12.dp)),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFF5F5F5),
                                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 16.sp,
                                                color = Color(0xFF1A1A1A)
                                            ),
                                            singleLine = true
                                        )
                                        
                                        phoneError?.let {
                                            Text(
                                                text = it,
                                                color = Color(0xFFE53935),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        
                                        generalError?.let {
                                            Text(
                                                text = it,
                                                color = Color(0xFFE53935),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        
                                        Button(
                                            onClick = {
                                                phoneError = null
                                                generalError = null
                                                
                                                if (phoneNumber.length != 10) {
                                                    phoneError = "Please enter a valid 10-digit phone number"
                                                } else {
                                                    otpRequested = false
                                                    authViewModel.requestPhoneOtp(phoneNumber)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .padding(top = 8.dp),
                                            enabled = !uiState.isLoading && phoneNumber.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White
                                                )
                                            } else {
                                                Text(
                                                    text = "SEND OTP",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Step 2: OTP Verification
                                    Column {
                                        Text(
                                            text = "Enter OTP sent to $phoneNumber",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        TextField(
                                            value = otp,
                                            onValueChange = { newValue: String -> 
                                                if (newValue.length <= 4) {
                                                    otp = newValue.filter { char: Char -> char.isDigit() }
                                                }
                                            },
                                            placeholder = { Text("Enter 4-digit OTP") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                                .clip(RoundedCornerShape(12.dp)),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFF5F5F5),
                                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                fontSize = 16.sp,
                                                color = Color(0xFF1A1A1A),
                                                letterSpacing = 2.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            singleLine = true
                                        )
                                        
                                        otpError?.let {
                                            Text(
                                                text = it,
                                                color = Color(0xFFE53935),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        
                                        generalError?.let {
                                            Text(
                                                text = it,
                                                color = if (it.contains("successfully", ignoreCase = true)) Color(0xFF4CAF50) else Color(0xFFE53935),
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                        
                                        Button(
                                            onClick = {
                                                otpError = null
                                                generalError = null
                                                
                                                if (otp.length != 4) {
                                                    otpError = "Please enter a valid 4-digit OTP"
                                                } else {
                                                    authViewModel.verifyPhoneOtp(phoneNumber, otp)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .padding(top = 8.dp),
                                            enabled = !uiState.isLoading && otp.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Primary
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White
                                                )
                                            } else {
                                                Text(
                                                    text = "VERIFY & LOGIN",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        
                                        // Resend OTP Button
                                        TextButton(
                                            onClick = {
                                                generalError = null
                                                authViewModel.requestPhoneOtp(phoneNumber)
                                                successMessage = "OTP resent successfully"
                                                generalError = "OTP resent successfully"
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 8.dp),
                                            enabled = !uiState.isLoading
                                        ) {
                                            Text(
                                                text = "Resend OTP",
                                                color = Primary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        // Change Phone Number Button
                                        TextButton(
                                            onClick = {
                                                showOtpInput = false
                                                otp = ""
                                                otpError = null
                                                generalError = null
                                                successMessage = null
                                                otpRequested = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Change Phone Number",
                                                color = Color(0xFF666666),
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                                
                                // Show OTP input after successful OTP request
                                LaunchedEffect(uiState.isLoading, uiState.error) {
                                    // When OTP request completes successfully (no error, not loading)
                                    if (!uiState.isLoading && uiState.error == null && phoneNumber.length == 10 && !showOtpInput && !otpRequested) {
                                        showOtpInput = true
                                        otpRequested = true
                                    }
                                }
                            }
                            
                            // Email Password Login
                            if (loginMethod == "email-password") {
                                Column {
                                    Text(
                                        text = "Email Address",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    TextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = { Text("Enter your email address") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp)),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedContainerColor = Color(0xFFF5F5F5),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 16.sp,
                                            color = Color(0xFF1A1A1A)
                                        ),
                                        singleLine = true
                                    )
                                    
                                    emailError?.let {
                                        Text(
                                            text = it,
                                            color = Color(0xFFE53935),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Password",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    var passwordVisible by remember { mutableStateOf(false) }
                                    
                                    TextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        placeholder = { Text("Enter your password") },
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp)),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedContainerColor = Color(0xFFF5F5F5),
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 16.sp,
                                            color = Color(0xFF1A1A1A)
                                        ),
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                // Icon for password visibility toggle would go here
                                            }
                                        },
                                        singleLine = true
                                    )
                                    
                                    passwordError?.let {
                                        Text(
                                            text = it,
                                            color = Color(0xFFE53935),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    
                                    generalError?.let {
                                        Text(
                                            text = it,
                                            color = Color(0xFFE53935),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            emailError = null
                                            passwordError = null
                                            generalError = null
                                            
                                            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
                                            if (email.isBlank() || !emailRegex.matches(email)) {
                                                emailError = "Please enter a valid email address"
                                                return@Button
                                            }
                                            if (password.length < 6) {
                                                passwordError = "Please enter a valid password (min 6 characters)"
                                                return@Button
                                            }
                                            
                                            authViewModel.loginWithEmailPassword(email, password)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .padding(top = 8.dp),
                                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (uiState.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White
                                            )
                                        } else {
                                            Text(
                                                text = "LOG IN",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                
                                // Forgot Password Link (only for email-password)
                                TextButton(
                                    onClick = {
                                        navController.navigate(NavRoute.ForgotPassword.route)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = Primary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        // Register link - HIDDEN (as per React Native)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
