package com.example.visualattendanceapp.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Attendance : Screen("attendance", "Attendance")
    object Settings : Screen("settings", "Settings")
}