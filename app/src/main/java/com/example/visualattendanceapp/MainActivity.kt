package com.example.visualattendanceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.visualattendanceapp.navigation.Screen
import com.example.visualattendanceapp.navigation.screens.HomeScreen
import com.example.visualattendanceapp.navigation.screens.RegisterScreen
import com.example.visualattendanceapp.navigation.screens.SettingsScreen
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0xFF181717.toInt()), // dark background, light icons
            navigationBarStyle = SystemBarStyle.dark(0xFF181717.toInt())
        )

        setContent {
            VisualAttendanceApp()
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun VisualAttendanceApp() {
    val items = listOf(Screen.Home, Screen.Attendance, Screen.Settings)
    val pagerState = rememberPagerState(initialPage = 0) { items.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { CustomTopBar(items[pagerState.currentPage].route) },
        bottomBar = {
            NavigationBar(containerColor = Color(30, 28, 28)) {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = Color(238, 238, 238),
                            selectedIconColor = Color.Black,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White
                        ),
                        label = { Text(screen.title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = { Text(screen.icon, style = MaterialTheme.typography.titleLarge) }
                    )
                }
            }
        },
        containerColor = Color(24, 23, 23)
    ) { innerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            when (items[page].route) {
                Screen.Home.route -> HomeScreen()
                Screen.Attendance.route -> RegisterScreen()
                Screen.Settings.route -> SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(currentRoute: String?) {

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = currentRoute ?: "",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(238, 238, 238) // ensures contrast
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(24, 23, 23), // dark background
            titleContentColor = Color(238, 238, 238)
        )
    )
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Attendance, Screen.Settings)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = Color(30, 28, 28, 255)) {
        items.forEach { screen ->
            NavigationBarItem(colors = NavigationBarItemDefaults.colors().copy(selectedTextColor = Color(238, 238, 238), selectedIndicatorColor = Color.White, unselectedTextColor = Color.Gray),
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Text(screen.icon, style = MaterialTheme.typography.titleLarge) } // add icons later
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    val routeOrder = listOf(
        Screen.Home.route,
        Screen.Attendance.route,
        Screen.Settings.route
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            val initialIndex = routeOrder.indexOf(initialState.destination.route)
            val targetIndex = routeOrder.indexOf(targetState.destination.route)

            if (targetIndex > initialIndex) {
                // Forward navigation → slide left
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                )
            } else {
                // Back navigation → slide right
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                )
            }
        },
        exitTransition = {
            val initialIndex = routeOrder.indexOf(initialState.destination.route)
            val targetIndex = routeOrder.indexOf(targetState.destination.route)

            if (targetIndex > initialIndex) {
                // Forward navigation → exit left
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                )
            } else {
                // Back navigation → exit right
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                )
            }
        }
    ) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Attendance.route) { RegisterScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}