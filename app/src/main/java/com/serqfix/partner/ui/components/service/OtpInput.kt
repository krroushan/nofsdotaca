package com.serqfix.partner.ui.components.service

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OtpData(
    val otp: List<String> = listOf("", "", "", ""),
    val generatedOtp: String? = null
)

@Composable
fun OtpInput(
    otpData: OtpData,
    onOtpChange: (Int, String) -> Unit,
    isOtpVerified: Boolean = false,
    disabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current
    var focusedIndex by remember { mutableIntStateOf(-1) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        otpData.otp.forEachIndexed { index, digit ->
            OtpDigitField(
                value = digit,
                onValueChange = { newValue ->
                    if (newValue.length <= 1) {
                        onOtpChange(index, newValue)
                        
                        // Auto-focus next field if digit entered
                        if (newValue.isNotEmpty() && index < 3) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    }
                },
                onBackspace = {
                    if (digit.isEmpty() && index > 0) {
                        // Move to previous field if current is empty
                        focusRequesters[index - 1].requestFocus()
                        onOtpChange(index - 1, "")
                    } else if (digit.isNotEmpty()) {
                        // Clear current field
                        onOtpChange(index, "")
                    }
                },
                isFocused = focusedIndex == index,
                onFocusChange = { isFocused ->
                    focusedIndex = if (isFocused) index else -1
                },
                isVerified = isOtpVerified,
                disabled = disabled,
                focusRequester = focusRequesters[index],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OtpDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    isVerified: Boolean,
    disabled: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isVerified -> Color(0xFF4CAF50)
        disabled -> Color(0xFFBDBDBD)
        isFocused -> Color(0xFF3954A4)
        else -> Color(0xFFBDBDBD)
    }
    
    val borderWidth = when {
        isVerified -> 1.5.dp
        isFocused -> 2.dp
        else -> 1.5.dp
    }
    
    val backgroundColor = when {
        isVerified -> Color(0xFFE8F5E9)
        disabled -> Color(0xFFF5F5F5)
        isFocused -> Color(0xFFF5F9FF)
        else -> Color.White
    }
    
    val textColor = when {
        isVerified -> Color(0xFF2E7D32)
        disabled -> Color(0xFF9E9E9E)
        else -> Color(0xFF212121)
    }

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow single digit
            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        enabled = !disabled && !isVerified,
        readOnly = disabled || isVerified,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                // Handle done action
            }
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            color = textColor
        ),
        modifier = modifier
            .size(55.dp)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChange(focusState.isFocused)
            }
            .focusable(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                    // Show placeholder when empty and not focused
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            text = "•",
                            color = Color(0xFFE0E0E0),
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}
