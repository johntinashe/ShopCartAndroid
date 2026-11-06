package com.shopcart.shopcart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.CatalogRepository
import com.shopcart.shopcart.data.Category
import com.shopcart.shopcart.data.FavoritesRepository
import com.shopcart.shopcart.data.Product
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.ProductCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val featured: List<Product> = emptyList(),
    val products: List<Product> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val query: String = "",
    val categoryId: String? = null,
)

class HomeViewModel(
    catalog: CatalogRepository,
    private val favoritesRepo: FavoritesRepository,
    private val uid: String,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)

    val state: StateFlow<HomeState> = combine(
        catalog.products,
        catalog.categories,
        favoritesRepo.ids(uid),
        query,
        categoryId,
    ) { products, categories, favorites, q, cat ->
        val term = q.trim().lowercase()
        HomeState(
            loading = false,
            categories = categories,
            featured = if (term.isEmpty() && cat == null) products.filter { it.featured } else emptyList(),
            products = products.filter { p ->
                (cat == null || p.categoryId == cat) &&
                    (term.isEmpty() || p.name.lowercase().contains(term) || p.shortDescription.lowercase().contains(term))
            },
            favorites = favorites,
            query = q,
            categoryId = cat,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun setQuery(value: String) { query.value = value }
    fun setCategory(id: String?) { categoryId.value = id }

    fun toggleFavorite(product: Product, isFavorite: Boolean) {
        viewModelScope.launch { runCatching { favoritesRepo.setFavorite(uid, product, !isFavorite) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenProduct: (String) -> Unit) {
    val container = appContainer()
    val vm = viewModel { HomeViewModel(container.catalog, container.favorites, container.auth.requireUid()) }
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ShopCart", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) })
        },
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = vm::setQuery,
                    placeholder = { Text("Search products") },
                    leadingIcon = { AppIcon(R.drawable.ic_search, null) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.categories.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = state.categoryId == null,
                                onClick = { vm.setCategory(null) },
                                label = { Text("All") },
                            )
                        }
                        items(state.categories, key = { it.id }) { c ->
                            FilterChip(
                                selected = state.categoryId == c.id,
                                onClick = { vm.setCategory(if (state.categoryId == c.id) null else c.id) },
                                label = { Text(c.name) },
                            )
                        }
                    }
                }
            }

            if (state.featured.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text("Featured", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.featured, key = { "f-" + it.id }) { p ->
                                ProductCard(
                                    product = p,
                                    isFavorite = p.id in state.favorites,
                                    onClick = { onOpenProduct(p.id) },
                                    onToggleFavorite = { vm.toggleFavorite(p, p.id in state.favorites) },
                                    modifier = Modifier.width(180.dp),
                                )
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    state.categories.firstOrNull { it.id == state.categoryId }?.name ?: "All products",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (state.products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        R.drawable.ic_search,
                        "Nothing found",
                        if (state.query.isNotBlank()) "No products match “${state.query}”." else "No products in this category yet.",
                        modifier = Modifier.height(320.dp),
                    )
                }
            }

            items(state.products, key = { it.id }) { p ->
                ProductCard(
                    product = p,
                    isFavorite = p.id in state.favorites,
                    onClick = { onOpenProduct(p.id) },
                    onToggleFavorite = { vm.toggleFavorite(p, p.id in state.favorites) },
                )
            }
        }
    }
}
