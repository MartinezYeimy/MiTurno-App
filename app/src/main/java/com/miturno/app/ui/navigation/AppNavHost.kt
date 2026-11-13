package com.miturno.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miturno.app.AppContainer
import com.miturno.app.data.model.Role
import com.miturno.app.ui.affiliate.HistoryScreen
import com.miturno.app.ui.affiliate.HistoryViewModel
import com.miturno.app.ui.affiliate.HomeScreen
import com.miturno.app.ui.affiliate.HomeViewModel
import com.miturno.app.ui.affiliate.ServicesScreen
import com.miturno.app.ui.affiliate.ServicesViewModel
import com.miturno.app.ui.auth.AuthViewModel
import com.miturno.app.ui.auth.LoginScreen
import com.miturno.app.ui.auth.RegisterScreen
import com.miturno.app.ui.components.vmFactory
import com.miturno.app.ui.staff.StaffScreen
import com.miturno.app.ui.staff.StaffViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val SERVICES = "services"
    const val HISTORY = "history"
    const val STAFF = "staff"
    const val FLASH = "flash"
}

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val repo = container.repository
    val session = container.session

    // Si ya hay una sesión guardada, se entra directo a la pantalla que corresponde
    val start = remember {
        when (session.state.value?.role) {
            Role.AFFILIATE -> Routes.HOME
            Role.STAFF -> Routes.STAFF
            else -> Routes.LOGIN
        }
    }

    val logout: () -> Unit = {
        session.clear()
        nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
    }

    NavHost(navController = nav, startDestination = start) {

        composable(Routes.LOGIN) {
            val vm: AuthViewModel = viewModel(factory = vmFactory { AuthViewModel(repo, session) })
            LoginScreen(
                vm = vm,
                onRegister = { nav.navigate(Routes.REGISTER) },
                onLoggedIn = { user ->
                    val target = if (user.role == Role.STAFF) Routes.STAFF else Routes.HOME
                    nav.navigate(target) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Routes.REGISTER) {
            val vm: AuthViewModel = viewModel(factory = vmFactory { AuthViewModel(repo, session) })
            RegisterScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onRegistered = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Routes.HOME) { entry ->
            val vm: HomeViewModel = viewModel(factory = vmFactory { HomeViewModel(repo, session) })
            val flash by entry.savedStateHandle.getStateFlow<String?>(Routes.FLASH, null).collectAsStateWithLifecycle()
            HomeScreen(
                vm = vm,
                flash = flash,
                onFlashShown = { entry.savedStateHandle[Routes.FLASH] = null },
                onRequestTurn = { nav.navigate(Routes.SERVICES) },
                onHistory = { nav.navigate(Routes.HISTORY) },
                onLogout = logout
            )
        }

        composable(Routes.SERVICES) {
            val vm: ServicesViewModel = viewModel(factory = vmFactory { ServicesViewModel(repo, session) })
            ServicesScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onRequested = { message ->
                    nav.previousBackStackEntry?.savedStateHandle?.set(Routes.FLASH, message)
                    nav.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel(factory = vmFactory { HistoryViewModel(repo, session) })
            HistoryScreen(vm = vm, onBack = { nav.popBackStack() })
        }

        composable(Routes.STAFF) {
            val vm: StaffViewModel = viewModel(factory = vmFactory { StaffViewModel(repo, session) })
            StaffScreen(vm = vm, onLogout = logout)
        }
    }
}
