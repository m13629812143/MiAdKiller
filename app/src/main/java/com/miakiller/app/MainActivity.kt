package com.miakiller.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miakiller.app.ui.screens.*
import com.miakiller.app.ui.theme.MiAdKillerTheme
import com.miakiller.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiAdKillerTheme {
                MiAdKillerApp()
            }
        }
    }
}

/**
 * 应用导航路由
 */
object Routes {
    const val HOME = "home"
    const val AD_SWITCH = "ad_switch"
    const val FREEZE = "freeze"
    const val AUTO_START = "auto_start"
    const val PERMISSION = "permission"
    const val HOSTS = "hosts"
}

@Composable
fun MiAdKillerApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAdSwitch = { navController.navigate(Routes.AD_SWITCH) },
                    onNavigateToFreeze = { navController.navigate(Routes.FREEZE) },
                    onNavigateToAutoStart = { navController.navigate(Routes.AUTO_START) },
                    onNavigateToPermission = { navController.navigate(Routes.PERMISSION) },
                    onNavigateToHosts = { navController.navigate(Routes.HOSTS) }
                )
            }

            composable(Routes.AD_SWITCH) {
                AdSwitchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FREEZE) {
                FreezeScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.AUTO_START) {
                AutoStartScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PERMISSION) {
                PermissionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HOSTS) {
                HostsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
