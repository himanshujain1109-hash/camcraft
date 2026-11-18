package com.camcraft.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.camcraft.ui.screens.CameraScreen
import com.camcraft.ui.screens.GalleryScreen
import com.camcraft.ui.screens.HomeScreen
import com.camcraft.ui.screens.PhotoEditorScreen
import com.camcraft.ui.screens.PhotoViewerScreen
import com.camcraft.ui.screens.SettingsScreen
import com.camcraft.viewmodel.CameraViewModel
import com.camcraft.viewmodel.EditorViewModel
import com.camcraft.viewmodel.GalleryViewModel
import com.camcraft.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel = viewModel(),
    galleryViewModel: GalleryViewModel = viewModel(),
    editorViewModel: EditorViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        // Home Screen
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            HomeScreen(
                cameraViewModel = cameraViewModel,
                galleryViewModel = galleryViewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPhotoViewer = { uriStr ->
                    navController.navigate(Screen.PhotoViewer.createRoute(uriStr))
                }
            )
        }

        // Camera Screen
        composable(
            route = Screen.Camera.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) {
            CameraScreen(
                cameraViewModel = cameraViewModel,
                galleryViewModel = galleryViewModel,
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPhotoViewer = { uriStr ->
                    navController.navigate(Screen.PhotoViewer.createRoute(uriStr))
                }
            )
        }

        // Gallery Screen
        composable(
            route = Screen.Gallery.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            GalleryScreen(
                galleryViewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToPhotoViewer = { uriStr ->
                    navController.navigate(Screen.PhotoViewer.createRoute(uriStr))
                }
            )
        }

        // Photo Viewer Screen
        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(
                navArgument("photoUri") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri = Uri.decode(encodedUri)
            PhotoViewerScreen(
                photoUriString = photoUri,
                galleryViewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { uriStr ->
                    navController.navigate(Screen.PhotoEditor.createRoute(uriStr))
                }
            )
        }

        // Photo Editor Screen
        composable(
            route = Screen.PhotoEditor.route,
            arguments = listOf(
                navArgument("photoUri") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("photoUri") ?: ""
            val photoUri = Uri.decode(encodedUri)
            PhotoEditorScreen(
                photoUriString = photoUri,
                editorViewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { newUriStr ->
                    galleryViewModel.refresh()
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private fun fadeIn(animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float>) =
    androidx.compose.animation.fadeIn(animationSpec)

private fun fadeOut(animationSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float>) =
    androidx.compose.animation.fadeOut(animationSpec)
