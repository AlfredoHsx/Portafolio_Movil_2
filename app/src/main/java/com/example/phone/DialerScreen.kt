package com.example.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialerScreen(onCallClick: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = phoneNumber.ifEmpty { "Ingrese número" },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineLarge
        )

        // Fila 1: 1 2 3
        Row(modifier = Modifier.padding(8.dp)) {
            NumberButton("1") { phoneNumber += "1" }
            Spacer(Modifier.width(8.dp))
            NumberButton("2") { phoneNumber += "2" }
            Spacer(Modifier.width(8.dp))
            NumberButton("3") { phoneNumber += "3" }
        }

        // Fila 2: 4 5 6
        Row(modifier = Modifier.padding(8.dp)) {
            NumberButton("4") { phoneNumber += "4" }
            Spacer(Modifier.width(8.dp))
            NumberButton("5") { phoneNumber += "5" }
            Spacer(Modifier.width(8.dp))
            NumberButton("6") { phoneNumber += "6" }
        }

        // Fila 3: 7 8 9
        Row(modifier = Modifier.padding(8.dp)) {
            NumberButton("7") { phoneNumber += "7" }
            Spacer(Modifier.width(8.dp))
            NumberButton("8") { phoneNumber += "8" }
            Spacer(Modifier.width(8.dp))
            NumberButton("9") { phoneNumber += "9" }
        }

        // Fila 4: * 0 #
        Row(modifier = Modifier.padding(8.dp)) {
            NumberButton("*") { phoneNumber += "*" }
            Spacer(Modifier.width(8.dp))
            NumberButton("0") { phoneNumber += "0" }
            Spacer(Modifier.width(8.dp))
            NumberButton("#") { phoneNumber += "#" }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de acción
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { phoneNumber = "" },
                modifier = Modifier.weight(1f)
            ) {
                Text("Borrar")
            }

            Button(
                onClick = { onCallClick(phoneNumber) },
                modifier = Modifier.weight(1f),
                enabled = phoneNumber.isNotEmpty()
            ) {
                Text("Llamar")
            }
        }
    }
}



@Composable
fun NumberButton(number: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
    ) {
        Text(text = number, style = MaterialTheme.typography.headlineMedium)
    }
}