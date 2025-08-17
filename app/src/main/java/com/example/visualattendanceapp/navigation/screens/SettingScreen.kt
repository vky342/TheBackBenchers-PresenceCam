package com.example.visualattendanceapp.navigation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SettingsScreen() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("VisualAttendanceApp is a streamlined tool designed to simplify attendance tracking using computer vision. Built with Jetpack Compose, this app lets users upload or capture three clear face photos for quick registration, and then accurately records attendance. With a clean Material design and smooth navigation, it offers an intuitive experience for educators and administrators alike.",
            textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth(0.8f),style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic
            ), color = Color(89, 88, 88, 255))
    }
}