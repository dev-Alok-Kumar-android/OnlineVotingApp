package com.alokkumar.onlinevotingapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alokkumar.onlinevotingapp.ui.screens.admin.AdminHomeScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.ManagePollScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.ManageVoterScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.MonitorVotesScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.ViewStatsScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.polls.AddOrEditCandidateScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.polls.AddOrEditPollScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.polls.PollResultScreen
import com.alokkumar.onlinevotingapp.ui.screens.admin.polls.VoteDetailScreen
import com.alokkumar.onlinevotingapp.ui.screens.auth.AdminLoginScreen
import com.alokkumar.onlinevotingapp.ui.screens.auth.AuthScreen
import com.alokkumar.onlinevotingapp.ui.screens.auth.UserLoginScreen
import com.alokkumar.onlinevotingapp.ui.screens.auth.UserRegistrationScreen
import com.alokkumar.onlinevotingapp.ui.screens.user.PollActionsScreen
import com.alokkumar.onlinevotingapp.ui.screens.user.PollResultScreen
import com.alokkumar.onlinevotingapp.ui.screens.user.UserHomeScreen
import com.alokkumar.onlinevotingapp.ui.screens.user.UserProfileScreen
import com.alokkumar.onlinevotingapp.ui.screens.user.VoteScreen
import com.alokkumar.onlinevotingapp.viewmodel.auth.SessionViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier,  sessionViewModel: SessionViewModel = viewModel()) {
    val navController = rememberNavController()

    val isLoggedIn by sessionViewModel.isLoggedIn
    val email by sessionViewModel.email

    val startDestination = when {
        !isLoggedIn -> Routes.AUTH
        email == "admin@gmail.com" -> Routes.ADMIN_HOME
        else -> Routes.USER_HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
//        startDestination = Routes.ADMIN_HOME
    ) {
        // 🔐 Auth Screens
        composable(Routes.AUTH) { AuthScreen(modifier, navController) }
        composable(Routes.ADMIN_LOGIN) { AdminLoginScreen(modifier, navController) }
        composable(Routes.USER_LOGIN) { UserLoginScreen(modifier, navController) }
        composable(Routes.USER_REGISTRATION) { UserRegistrationScreen(modifier, navController) }


        // 👨‍💼 Admin Screens
        composable(Routes.ADMIN_HOME) { AdminHomeScreen(navController) }
        composable(Routes.VIEW_STATS) { ViewStatsScreen(navController) }
        composable(Routes.MANAGE_VOTER) { ManageVoterScreen(navController) }
        composable(Routes.MANAGE_POLLS) { ManagePollScreen(navController) }
        composable(Routes.MONITOR_VOTES) { MonitorVotesScreen(navController) }

        // PollModel/Vote Screens
        composable("${Routes.POLL_RESULT}/{pollId}") { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId") ?: ""
            PollResultScreen(navController, pollId)
        }
        composable(Routes.ADD_OR_EDIT_POLL) {
            AddOrEditPollScreen(navController = navController, pollId = null)
        }
        composable("${Routes.ADD_OR_EDIT_POLL}/{pollId}") { backStackEntry ->
            val pollId = backStackEntry.arguments?.getString("pollId")
            AddOrEditPollScreen(navController = navController, pollId = pollId)
        }
        composable("${Routes.ADD_OR_EDIT_CANDIDATE}/{pollId}") {
            val pollId = it.arguments?.getString("pollId") ?: ""
            AddOrEditCandidateScreen(navController, pollId)
        }
        composable("${Routes.ADD_OR_EDIT_CANDIDATE}/{pollId}/{candidateId}") {
            val pollId = it.arguments?.getString("pollId") ?: ""
            val candidateId = it.arguments?.getString("candidateId") ?: ""
            AddOrEditCandidateScreen(navController, pollId, candidateId)
        }
        composable("${Routes.VOTE_DETAIL}/{voteId}") {
            val voteId = it.arguments?.getString("voteId") ?: ""
            VoteDetailScreen(navController, voteId)
        }

        // 🙋‍♂️ User Screens
        composable(Routes.USER_HOME) { UserHomeScreen(navController) }
        composable("${ Routes.USER_PROFILE }/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            UserProfileScreen(navController=navController, userId = userId)
        }
        composable("${Routes.POLL_ACTIONS}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            PollActionsScreen(navController, pollId)
        }
        composable(
            "${Routes.VOTE_SCREEN}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            VoteScreen(pollId, navController = navController)
        }
        composable(
            "${Routes.RESULT_SCREEN}/{pollId}",
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            PollResultScreen(pollId, navController)
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
    const val VIEW_STATS = "view_states"
    const val MONITOR_VOTES = "monitor_votes"

    const val POLL_RESULT = "poll_result"
    const val ADD_OR_EDIT_POLL = "add_or_edit_poll"
    const val ADD_OR_EDIT_CANDIDATE = "add_or_edit_candidate"
    const val VOTE_DETAIL = "vote_detail"

    const val USER_HOME = "user_home"
    const val USER_PROFILE = "user_profile"
    const val POLL_ACTIONS = "poll_actions"
    const val VOTE_SCREEN = "vote_screen"
    const val RESULT_SCREEN = "result_screen"
}
