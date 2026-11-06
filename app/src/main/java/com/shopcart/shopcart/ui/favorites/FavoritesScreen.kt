package com.shopcart.shopcart.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.FavoritesRepository
import com.shopcart.shopcart.data.Product
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.ProductCard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(private val favorites: FavoritesRepository, private val uid: String) : ViewModel() {
    val products: StateFlow<List<Product>?> =
        favorites.products(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(product: Product) {
        viewModelScope.launch { runCatching { favorites.setFavorite(uid, product, false) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onOpenProduct: (String) -> Unit, onBrowse: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { FavoritesViewModel(container.favorites, container.auth.requireUid()) }
    val products by vm.products.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Saved for later") }) }) { padding ->
        val list = products
        when {
            list == null -> LoadingBox(Modifier.padding(padding))
            list.isEmpty() -> EmptyState(
                R.drawable.ic_favorite_border,
                "Nothing saved yet",
                "Tap the heart on any product to keep it here.",
                Modifier.padding(padding),
                actionLabel = "Browse products",
                onAction = onBrowse,
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(list, key = { it.id }) { p ->
                    ProductCard(
                        product = p,
                        isFavorite = true,
                        onClick = { onOpenProduct(p.id) },
                        onToggleFavorite = { vm.remove(p) },
                    )
                }
            }
        }
    }
}
