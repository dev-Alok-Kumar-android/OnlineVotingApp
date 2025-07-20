package com.alokkumar.onlinevotingapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alokkumar.onlinevotingapp.screens.admin.AdminHomeScreen
import com.alokkumar.onlinevotingapp.screens.AdminLoginScreen
import com.alokkumar.onlinevotingapp.screens.AuthScreen
import com.alokkumar.onlinevotingapp.screens.UserLoginScreen
import com.alokkumar.onlinevotingapp.screens.UserRegistrationScreen
import com.alokkumar.onlinevotingapp.screens.admin.ManagePollScreen
import com.alokkumar.onlinevotingapp.screens.admin.ManageVoterScreen
import com.alokkumar.onlinevotingapp.screens.admin.AddCandidateScreen
import com.alokkumar.onlinevotingapp.screens.admin.AddOrEditPollScreen
import com.alokkumar.onlinevotingapp.screens.user.PollActionsScreen
import com.alokkumar.onlinevotingapp.screens.user.ResultScreen
import com.alokkumar.onlinevotingapp.screens.user.VoteScreen
import com.alokkumar.onlinevotingapp.screens.user.UserHomeScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val isLoggedIn = Firebase.auth.currentUser != null
    val currentUserEmail = Firebase.auth.currentUser?.email ?: ""

    NavHost(
        navController = navController,
        startDestination = when {
            !isLoggedIn -> Routes.AUTH
            currentUserEmail == "admin@gmail.com" -> Routes.ADMIN_HOME
            else -> Routes.USER_HOME
        }
    ) {
        // 🔐 Auth Screens
        composable(Routes.AUTH) { AuthScreen(modifier, navController) }
        composable(Routes.ADMIN_LOGIN) { AdminLoginScreen(modifier, navController) }
        composable(Routes.USER_LOGIN) { UserLoginScreen(modifier, navController) }
        composable(Routes.USER_REGISTRATION) { UserRegistrationScreen(modifier, navController) }

        // 👨‍💼 Admin Screens
        composable(Routes.ADMIN_HOME) { AdminHomeScreen(navController) }
        composable(Routes.MANAGE_VOTER) { ManageVoterScreen() }
        composable(Routes.MANAGE_POLLS) { ManagePollScreen(navController) }
        composable(
            "${Routes.ADD_CANDIDATE}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            AddCandidateScreen(navController, pollId)
        }
        composable("add_or_edit_poll") {
            AddOrEditPollScreen(navController = navController, pollId = null)
        }
        composable("add_or_edit_poll/{pollId}") { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId")
            AddOrEditPollScreen(navController = navController, pollId = pollId)
        }


        // 🙋‍♂️ User Screens
        composable(Routes.USER_HOME) {
            UserHomeScreen(navController) // Landing screen to select poll
        }

        composable("${Routes.POLL_ACTIONS}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            PollActionsScreen(navController, pollId)
        }

        composable("${Routes.VOTE_SCREEN}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            VoteScreen(navController, pollId)
        }

        composable("${Routes.RESULT_SCREEN}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            ResultScreen(pollId, navController)
        }
    }
}




object Routes {
    const val AUTH = "auth"
    const val ADMIN_LOGIN = "admin_login"
    const val USER_LOGIN = "user_login"
    const val USER_REGISTRATION = "user_registration"
    const val ADMIN_HOME = "admin_home"
    const val MANAGE_VOTER = "manage_voter"
    const val MANAGE_POLLS = "manage_polls"
    const val ADD_CANDIDATE = "add_candidate"

    const val USER_HOME = "user_home" // (Previously SelectPollScreen)
    const val POLL_ACTIONS = "poll_actions"

    const val VOTE_SCREEN = "vote_screen"
    const val RESULT_SCREEN = "result_screen"
}
