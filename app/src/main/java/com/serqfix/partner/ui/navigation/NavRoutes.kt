package com.serqfix.partner.ui.navigation

sealed class NavRoute(val route: String) {
    // Auth routes
    object Splash : NavRoute("splash")
    object Login : NavRoute("login")
    object Register : NavRoute("register")
    object VerificationOptions : NavRoute("verification_options")
    object ForgotPassword : NavRoute("forgot_password")
    object VerifyOtpForgotPassword : NavRoute("verify_otp_forgot_password")
    object ResetPassword : NavRoute("reset_password")
    object IdVerification : NavRoute("id_verification")
    object SetProfileImage : NavRoute("set_profile_image")
    object UnderReview : NavRoute("under_review")
    object AskNotification : NavRoute("ask_notification")
    
    // Main routes
    object TabNavigator : NavRoute("tab_navigator")
    object Dashboard : NavRoute("dashboard")
    object Home : NavRoute("home")
    object Profile : NavRoute("profile")
    object ServiceDetails : NavRoute("service_details")
    object Bookings : NavRoute("bookings")
    object Payment : NavRoute("payment")
    object Wallet : NavRoute("wallet")
    object AMCOrders : NavRoute("amc_orders")
    object AMCOrderDetails : NavRoute("amc_order_details")
    object AMCVisitDetails : NavRoute("amc_visit_details")
    object GenerateInvoice : NavRoute("generate_invoice")
    object GenerateAMCInvoice : NavRoute("generate_amc_invoice")
    object EditProfile : NavRoute("edit_profile")
    object RaiseTicket : NavRoute("raise_ticket")
    object TicketChat : NavRoute("ticket_chat")
    object TicketsList : NavRoute("tickets_list")
    object RateCardList : NavRoute("rate_card_list")
    object ViewRateCard : NavRoute("view_rate_card")
    object ReferralDetails : NavRoute("referral_details")
    object Reviews : NavRoute("reviews")
    object Notifications : NavRoute("notifications")
    object AttendanceDetails : NavRoute("attendance_details")
    object LiveLocation : NavRoute("live_location")
    object WebView : NavRoute("web_view")
    object CustomPage : NavRoute("custom_page")
    object Permissions : NavRoute("permissions")
    object OnBoarding : NavRoute("onboarding")
    
    // Verification routes
    object ProfilePictureVerification : NavRoute("profile_picture_verification")
    
    // Navigation arguments
    fun withArgs(vararg args: Pair<String, String>): String {
        return buildString {
            append(route)
            if (args.isNotEmpty()) {
                append("?")
                args.forEachIndexed { index, (key, value) ->
                    if (index > 0) append("&")
                    append("$key=$value")
                }
            }
        }
    }
}
