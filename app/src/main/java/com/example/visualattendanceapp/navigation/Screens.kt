package com.example.visualattendanceapp.navigation

sealed class Screen(val route: String, val title: String, val icon : String) {
    object Home : Screen("⌘", "Home", "⌘")
    object Attendance : Screen("⌆", "Register", "⌆")
    object Settings : Screen("⚙︎", "Settings", "⚙︎")
}