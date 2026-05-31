package com.example.collectionsapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.collectionsapp.ui.screens.collectiondetail.CollectionDetailScreen
import com.example.collectionsapp.ui.screens.collections.CollectionsScreen
import com.example.collectionsapp.ui.screens.itemdetail.ItemDetailScreen

/**
 * تعريف شاشات التطبيق
 */
sealed class Screen(val route: String) {
    // الشاشة الرئيسية - قائمة المجموعات
    object Collections : Screen("collections")

    // شاشة تفاصيل المجموعة - تستقبل معرف المجموعة
    object CollectionDetail : Screen("collection_detail/{collectionId}") {
        fun createRoute(collectionId: Long) = "collection_detail/$collectionId"
    }

    // شاشة تفاصيل العنصر - تستقبل معرف المجموعة والعنصر
    object ItemDetail : Screen("item_detail/{collectionId}/{itemId}") {
        fun createRoute(collectionId: Long, itemId: Long) = "item_detail/$collectionId/$itemId"
    }
}

/**
 * رسم بياني للتنقل بين الشاشات
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Collections.route  // الشاشة الابتدائية
    ) {
        // الشاشة الرئيسية
        composable(Screen.Collections.route) {
            CollectionsScreen(
                onCollectionClick = { collectionId ->
                    // الانتقال إلى تفاصيل المجموعة
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                }
            )
        }

        // شاشة تفاصيل المجموعة
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: return@composable
            CollectionDetailScreen(
                collectionId = collectionId,
                onItemClick = { itemId ->
                    // الانتقال إلى تفاصيل العنصر
                    navController.navigate(Screen.ItemDetail.createRoute(collectionId, itemId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // شاشة تفاصيل العنصر
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType },
                navArgument("itemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: return@composable
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            ItemDetailScreen(
                collectionId = collectionId,
                itemId = itemId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}