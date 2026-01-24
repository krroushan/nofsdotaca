package com.serqfix.partner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.serqfix.partner.data.local.UserPreferencesDataStore
import com.serqfix.partner.data.repository.AMCRepository
import com.serqfix.partner.data.repository.PaymentRepository
import com.serqfix.partner.data.repository.WalletRepository
import com.serqfix.partner.ui.screens.auth.*
import com.serqfix.partner.ui.screens.main.*
import com.serqfix.partner.ui.viewmodel.*
import javax.inject.Inject

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoute.Splash.route,
    userPreferences: UserPreferencesDataStore
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth flow
        composable(NavRoute.Splash.route) {
            SplashScreen(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(NavRoute.Register.route) {
            // RegisterScreen(navController = navController) // TODO: Implement Register screen
        }
        
        composable(NavRoute.VerificationOptions.route) {
            // VerificationOptionsScreen(navController = navController) // TODO: Implement VerificationOptions screen
        }
        
        composable(NavRoute.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        
        composable(NavRoute.VerifyOtpForgotPassword.route) {
            VerifyOtpForgotPasswordScreen(navController = navController)
        }
        
        composable(NavRoute.ResetPassword.route) {
            ResetPasswordScreen(navController = navController)
        }
        
        composable(NavRoute.OnBoarding.route) {
            OnBoardingScreen(navController = navController)
        }
        
        composable(NavRoute.IdVerification.route) {
            PlaceholderScreen(navController = navController, screenName = "ID Verification")
        }
        
        composable(NavRoute.SetProfileImage.route) {
            PlaceholderScreen(navController = navController, screenName = "Set Profile Image")
        }
        
        composable(NavRoute.UnderReview.route) {
            PlaceholderScreen(navController = navController, screenName = "Under Review")
        }
        
        composable(NavRoute.AskNotification.route) {
            PlaceholderScreen(navController = navController, screenName = "Notification Permission")
        }
        
        // Main flow
        composable(NavRoute.TabNavigator.route) {
            TabNavigator(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(
            route = "${NavRoute.ServiceDetails.route}?bookingId={bookingId}",
            arguments = listOf(
                navArgument("bookingId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val bookingRepository = com.serqfix.partner.data.repository.BookingRepository()
            ServiceDetailsScreen(
                navController = navController,
                bookingId = bookingId,
                bookingRepository = bookingRepository,
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.Bookings.route) {
            val bookingRepository = com.serqfix.partner.data.repository.BookingRepository()
            BookingsScreen(
                navController = navController,
                bookingRepository = bookingRepository,
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.Payment.route) {
            // PaymentScreen will be implemented with ViewModel
            // PaymentScreen(navController = navController, paymentViewModel = hiltViewModel())
        }
        
        composable(NavRoute.Wallet.route) {
            WalletScreen(
                navController = navController,
                walletViewModel = hiltViewModel(),
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.AMCOrders.route) {
            AMCOrdersScreen(
                navController = navController,
                amcViewModel = hiltViewModel()
            )
        }
        
        composable(
            route = "${NavRoute.AMCOrderDetails.route}?orderId={orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            // AMCOrderDetailsScreen(
            //     navController = navController,
            //     orderId = orderId,
            //     amcViewModel = hiltViewModel()
            // )
        }
        
        composable(NavRoute.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                userPreferences = userPreferences
            )
        }
        
        composable(NavRoute.RaiseTicket.route) {
            PlaceholderScreen(navController = navController, screenName = "Raise Ticket")
        }
        
        composable(NavRoute.TicketChat.route) {
            PlaceholderScreen(navController = navController, screenName = "Ticket Chat")
        }
        
        composable(NavRoute.TicketsList.route) {
            PlaceholderScreen(navController = navController, screenName = "Tickets List")
        }
        
        composable(NavRoute.RateCardList.route) {
            PlaceholderScreen(navController = navController, screenName = "Rate Cards")
        }
        
        composable(NavRoute.ViewRateCard.route) {
            PlaceholderScreen(navController = navController, screenName = "View Rate Card")
        }
        
        composable(NavRoute.ReferralDetails.route) {
            PlaceholderScreen(navController = navController, screenName = "Referral Details")
        }
        
        composable(NavRoute.Reviews.route) {
            PlaceholderScreen(navController = navController, screenName = "Reviews")
        }
        
        composable(
            route = "${NavRoute.WebView.route}?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            WebViewScreen(navController = navController, url = url)
        }
        
        composable(NavRoute.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        
        composable(NavRoute.AttendanceDetails.route) {
            PlaceholderScreen(navController = navController, screenName = "Attendance Details")
        }
        
        composable(NavRoute.LiveLocation.route) {
            PlaceholderScreen(navController = navController, screenName = "Live Location")
        }
        
        composable(NavRoute.CustomPage.route) {
            PlaceholderScreen(navController = navController, screenName = "Custom Page")
        }
        
        composable(NavRoute.Permissions.route) {
            PermissionsScreen(navController = navController, userPreferences = userPreferences)
        }
        
        composable(NavRoute.ProfilePictureVerification.route) {
            // ProfilePictureVerificationScreen(navController = navController)
        }
    }
}
