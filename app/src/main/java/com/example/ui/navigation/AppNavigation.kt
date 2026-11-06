package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.council.AiCouncilScreen
import com.example.ui.launcher.LauncherScreen
import com.example.ui.media.AiMediaScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.workspaces.WorkspacesScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Launcher : Screen("launcher", "Launcher", Icons.Default.Home)
    object Council : Screen("council", "AI Council", Icons.Default.AutoAwesome)
    object Workspaces : Screen("workspaces", "Workspaces", Icons.Default.Folder)
    object Media : Screen("media", "AI Media", Icons.Default.Palette)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    onSignOut: () -> Unit
) {
    val items = listOf(
        Screen.Launcher,
        Screen.Council,
        Screen.Workspaces,
        Screen.Media,
        Screen.Settings
    )
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Launcher) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreen) {
                is Screen.Launcher -> LauncherScreen()
                is Screen.Council -> AiCouncilScreen()
                is Screen.Workspaces -> WorkspacesScreen()
                is Screen.Media -> AiMediaScreen()
                is Screen.Settings -> SettingsScreen(onSignOut = onSignOut)
            }
        }
    }
}
