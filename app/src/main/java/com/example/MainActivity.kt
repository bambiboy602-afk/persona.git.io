package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PersonaDashboard
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ClinicalViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ClinicalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(viewModel = viewModel, onNavigate = { route ->
                                navController.navigate(route)
                            })
                        }
                        composable("personas") {
                            PersonaListScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSelectPersona = { personaId ->
                                    navController.navigate("chat/$personaId")
                                },
                                onOpenGrid = {
                                    navController.navigate("persona_grid")
                                }
                            )
                        }
                        composable("persona_grid") {
                            PersonaDashboard(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSelectPersona = { personaId ->
                                    navController.navigate("chat/$personaId")
                                }
                            )
                        }
                        composable("create_persona") {
                            CreatePersonaScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "chat/{personaId}",
                            arguments = listOf(navArgument("personaId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            RoleplayChatScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("training") {
                            TrainingModulesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onSelectModule = { moduleId ->
                                    navController.navigate("personas")
                                }
                            )
                        }
                        composable("first_aid") {
                            FirstAidKitScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("analytics") {
                            AnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
