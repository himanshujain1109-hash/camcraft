package com.camcraft.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Gallery : Screen("gallery")
    data object Settings : Screen("settings")

    data object PhotoViewer : Screen("photo_viewer/{photoUri}") {
        fun createRoute(photoUri: String): String = "photo_viewer/${Uri.encode(photoUri)}"
    }

    data object PhotoEditor : Screen("photo_editor/{photoUri}") {
        fun createRoute(photoUri: String): String = "photo_editor/${Uri.encode(photoUri)}"
    }
}
