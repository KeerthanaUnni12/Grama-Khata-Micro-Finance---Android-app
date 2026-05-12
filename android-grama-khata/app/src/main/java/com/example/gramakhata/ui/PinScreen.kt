package com.example.gramakhata.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(correctPin: String, onVerified: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Secure Access",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Enter your 4-digit security PIN",
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = pin,
            onValueChange = { 
                if (it.length <= 4) {
                    val newPin = it.replace(Regex("\\D"), "")
                    pin = newPin
                    if (newPin.length == 4) {
                        if (newPin == correctPin) {
                            onVerified()
                        } else {
                            error = true
                            pin = ""
                        }
                    } else {
                        error = false
                    }
                }
            },
            label = { Text("Enter PIN") },
            modifier = Modifier.width(200.dp),
            isError = error
        )
        
        if (error) {
            Text(
                text = "Incorrect PIN, please try again",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
