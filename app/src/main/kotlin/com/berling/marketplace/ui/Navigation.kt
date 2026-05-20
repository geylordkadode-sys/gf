package com.berling.marketplace.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.berling.marketplace.ui.screens.AuthNavigation
import com.berling.marketplace.ui.screens.authNavigation
import com.berling.marketplace.ui.screens.home.HomeScreen
import com.berling.marketplace.ui.screens.profile.ProfileScreen
import com.berling.marketplace.ui.screens.profile.ProfileMenuScreen
import com.berling.marketplace.ui.screens.profile.SellerProfileScreen
import com.berling.marketplace.ui.screens.profile.SettingsScreen
import com.berling.marketplace.ui.screens.profile.WalletScreen
import com.berling.marketplace.ui.screens.profile.HelpSupportScreen
import com.berling.marketplace.ui.screens.profile.MyShopScreen
import com.berling.marketplace.ui.screens.profile.MyListingsScreen
import com.berling.marketplace.ui.screens.splash.SplashScreen
import com.berling.marketplace.ui.screens.chats.ChatsScreen
import com.berling.marketplace.ui.screens.chat.ChatScreen
import com.berling.marketplace.ui.screens.post.PostScreen
import com.berling.marketplace.ui.screens.search.SearchScreen
import com.berling.marketplace.ui.screens.orders.OrdersScreen
import com.berling.marketplace.ui.screens.analytics.AnalyticsScreen
import com.berling.marketplace.ui.screens.product.ProductDetailScreen

@Composable
fun BerlingApp() {
    val navController = rememberNavController()
    BerlingNavigation(navController)
}

@Composable
fun BerlingNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        authNavigation(navController)
        
        composable("home") {
            HomeScreen(navController = navController)
        }
        
        composable("chats") {
            ChatsScreen(navController = navController)
        }
        
        composable("post") {
            PostScreen(navController = navController)
        }
        
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        
        composable("profile_menu") {
            ProfileMenuScreen(navController = navController)
        }
        
        composable("seller_profile") {
            SellerProfileScreen(navController = navController)
        }
        
        composable("seller_profile/{sellerId}") { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            SellerProfileScreen(navController = navController, sellerId = sellerId)
        }
        
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        
        composable("wallet") {
            WalletScreen(navController = navController)
        }
        
        composable("help_support") {
            HelpSupportScreen(navController = navController)
        }
        
        composable("my_shop") {
            MyShopScreen(navController = navController)
        }
        
        composable("my_listings") {
            MyListingsScreen(navController = navController)
        }
        
        composable("search") {
            SearchScreen(navController = navController)
        }
        
        composable("chat") {
            ChatScreen(navController = navController)
        }
        
        composable("chat_with_seller/{sellerId}/{sellerName}/{productId}/{productTitle}") { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            val sellerName = backStackEntry.arguments?.getString("sellerName") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val productTitle = backStackEntry.arguments?.getString("productTitle") ?: ""
            ChatScreen(
                navController = navController,
                sellerId = sellerId,
                sellerName = sellerName,
                productId = productId,
                productTitle = productTitle
            )
        }
        
        composable("orders/{buyerId}") { backStackEntry ->
            val buyerId = backStackEntry.arguments?.getString("buyerId") ?: ""
            OrdersScreen(navController = navController, buyerId = buyerId)
        }
        
        composable("analytics/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            AnalyticsScreen(navController = navController, userId = userId)
        }
        
        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController = navController, productId = productId)
        }
    }
}
