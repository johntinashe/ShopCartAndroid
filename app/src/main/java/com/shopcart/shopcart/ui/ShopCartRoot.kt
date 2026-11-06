package com.shopcart.shopcart.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.ui.account.AccountScreen
import com.shopcart.shopcart.ui.account.EditProfileScreen
import com.shopcart.shopcart.ui.auth.AuthScreen
import com.shopcart.shopcart.ui.cart.CartScreen
import com.shopcart.shopcart.ui.chat.ChatScreen
import com.shopcart.shopcart.ui.checkout.CheckoutScreen
import com.shopcart.shopcart.ui.checkout.PaymentScreen
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.favorites.FavoritesScreen
import com.shopcart.shopcart.ui.home.HomeScreen
import com.shopcart.shopcart.ui.orders.OrderDetailScreen
import com.shopcart.shopcart.ui.orders.OrdersScreen
import com.shopcart.shopcart.ui.product.ProductScreen

object Routes {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ACCOUNT = "account"
    const val PRODUCT = "product/{id}"
    const val ORDER = "order/{id}"
    const val CHECKOUT = "checkout"
    const val PAYMENT = "payment/{orderId}?instructions={instructions}"
    const val CHAT = "chat"
    const val EDIT_PROFILE = "profile/edit"

    fun product(id: String) = "product/$id"
    fun order(id: String) = "order/$id"
    fun payment(orderId: String, instructions: String?) =
        "payment/$orderId" + (instructions?.let { "?instructions=${Uri.encode(it)}" } ?: "")
}

private data class Tab(val route: String, val label: String, @DrawableRes val icon: Int)

private val tabs = listOf(
    Tab(Routes.HOME, "Shop", R.drawable.ic_home),
    Tab(Routes.FAVORITES, "Saved", R.drawable.ic_favorite_border),
    Tab(Routes.CART, "Cart", R.drawable.ic_cart),
    Tab(Routes.ORDERS, "Orders", R.drawable.ic_receipt),
    Tab(Routes.ACCOUNT, "Account", R.drawable.ic_person),
)

@Composable
fun ShopCartRoot(pendingOrderId: String?, onPendingOrderHandled: () -> Unit) {
    val container = appContainer()
    val user by container.auth.currentUser.collectAsStateWithLifecycle(initialValue = container.auth.userNow)

    val current = user
    if (current == null) {
        AuthScreen()
    } else {
        // Rebuild everything (view models, back stack) when the account changes.
        key(current.uid) {
            SignedInApp(current.uid, pendingOrderId, onPendingOrderHandled)
        }
    }
}

@Composable
private fun SignedInApp(uid: String, pendingOrderId: String?, onPendingOrderHandled: () -> Unit) {
    val container = appContainer()
    val nav = rememberNavController()
    val cartCount by remember(uid) { container.cart.itemCount(uid) }.collectAsStateWithLifecycle(0)

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        runCatching { container.auth.syncDeviceToken() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(pendingOrderId) {
        if (pendingOrderId != null) {
            nav.navigate(Routes.order(pendingOrderId))
            onPendingOrderHandled()
        }
    }

    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBar = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { nav.switchTab(tab.route) },
                            icon = {
                                if (tab.route == Routes.CART && cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text(cartCount.toString()) } }) {
                                        AppIcon(tab.icon, tab.label)
                                    }
                                } else {
                                    AppIcon(tab.icon, tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            // Apply and consume only the bottom inset (nav bar or system bar)
            // so each screen's own Scaffold handles the top.
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = padding.calculateBottomPadding())),
        ) {
            composable(Routes.HOME) {
                HomeScreen(onOpenProduct = { nav.navigate(Routes.product(it)) })
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onOpenProduct = { nav.navigate(Routes.product(it)) },
                    onBrowse = { nav.switchTab(Routes.HOME) },
                )
            }
            composable(Routes.CART) {
                CartScreen(
                    onCheckout = { nav.navigate(Routes.CHECKOUT) },
                    onOpenProduct = { nav.navigate(Routes.product(it)) },
                    onBrowse = { nav.switchTab(Routes.HOME) },
                )
            }
            composable(Routes.ORDERS) {
                OrdersScreen(
                    onOpenOrder = { nav.navigate(Routes.order(it)) },
                    onBrowse = { nav.switchTab(Routes.HOME) },
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(
                    onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                    onOpenChat = { nav.navigate(Routes.CHAT) },
                    onOpenOrders = { nav.switchTab(Routes.ORDERS) },
                )
            }
            composable(Routes.PRODUCT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                ProductScreen(
                    productId = entry.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onGoToCart = { nav.switchTab(Routes.CART) },
                )
            }
            composable(Routes.ORDER, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                OrderDetailScreen(
                    orderId = entry.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onContactSupport = { nav.navigate(Routes.CHAT) },
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { nav.popBackStack() },
                    onPaymentStarted = { result ->
                        nav.navigate(Routes.payment(result.orderId, result.instructions)) {
                            popUpTo(Routes.CART)
                        }
                    },
                )
            }
            composable(
                Routes.PAYMENT,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("instructions") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                PaymentScreen(
                    orderId = entry.arguments?.getString("orderId").orEmpty(),
                    instructions = entry.arguments?.getString("instructions"),
                    onDone = { orderId ->
                        nav.navigate(Routes.order(orderId)) { popUpTo(Routes.HOME) }
                    },
                    onRetry = { nav.navigate(Routes.CHECKOUT) { popUpTo(Routes.CART) } },
                )
            }
            composable(Routes.CHAT) { ChatScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.EDIT_PROFILE) { EditProfileScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
