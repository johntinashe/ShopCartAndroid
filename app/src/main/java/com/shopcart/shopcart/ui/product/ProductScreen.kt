package com.shopcart.shopcart.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.data.CartRepository
import com.shopcart.shopcart.data.CatalogRepository
import com.shopcart.shopcart.data.FavoritesRepository
import com.shopcart.shopcart.data.Product
import com.shopcart.shopcart.data.Review
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.EmptyState
import com.shopcart.shopcart.ui.components.LoadingBox
import com.shopcart.shopcart.ui.components.ProductImage
import com.shopcart.shopcart.ui.components.QuantityStepper
import com.shopcart.shopcart.ui.components.RatingStars
import com.shopcart.shopcart.ui.components.asDate
import com.shopcart.shopcart.ui.components.asMoney
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductState(
    val loading: Boolean = true,
    val product: Product? = null,
    val isFavorite: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val hasReviewed: Boolean = false,
)

class ProductViewModel(
    private val productId: String,
    private val catalog: CatalogRepository,
    private val cart: CartRepository,
    private val favorites: FavoritesRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val uid = auth.requireUid()

    val state: StateFlow<ProductState> = combine(
        catalog.product(productId),
        favorites.ids(uid).map { productId in it },
        catalog.reviews(productId),
    ) { product, fav, reviews ->
        ProductState(
            loading = false,
            product = product,
            isFavorite = fav,
            reviews = reviews,
            hasReviewed = reviews.any { it.userId == uid },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductState())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    fun addToCart(product: Product, quantity: Int) = viewModelScope.launch {
        runCatching { cart.add(uid, product, quantity) }
            .onSuccess { _messages.emit(if (it > 0) "Added to cart · $it in cart" else "Out of stock") }
            .onFailure { _messages.emit("Couldn't add to cart. Check your connection.") }
    }

    fun toggleFavorite(product: Product, isFavorite: Boolean) = viewModelScope.launch {
        runCatching { favorites.setFavorite(uid, product, !isFavorite) }
    }

    fun submitReview(product: Product, rating: Int, text: String) = viewModelScope.launch {
        runCatching {
            val profile = auth.profile(uid).first()
            catalog.addReview(product, uid, profile.fullName, rating, text)
        }
            .onSuccess { _messages.emit("Thanks for your review!") }
            .onFailure { _messages.emit("Couldn't post review.") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(productId: String, onBack: () -> Unit, onGoToCart: () -> Unit) {
    val container = appContainer()
    val vm = viewModel(key = productId) {
        ProductViewModel(productId, container.catalog, container.cart, container.favorites, container.auth)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var quantity by rememberSaveable { mutableIntStateOf(1) }
    var reviewing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            val action = if (msg.startsWith("Added")) "View cart" else null
            if (snackbar.showSnackbar(msg, actionLabel = action) == SnackbarResult.ActionPerformed) onGoToCart()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) { AppIcon(R.drawable.ic_arrow_back, "Back") }
                },
                actions = {
                    state.product?.let { p ->
                        IconButton(onClick = { vm.toggleFavorite(p, state.isFavorite) }) {
                            AppIcon(
                                if (state.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
                                if (state.isFavorite) "Remove from favourites" else "Add to favourites",
                                tint = if (state.isFavorite) Color(0xFFD93F3F) else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            val p = state.product
            if (p != null) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (p.inStock) {
                            QuantityStepper(
                                value = quantity.coerceAtMost(p.quantity),
                                onChange = { quantity = it },
                                max = p.quantity,
                            )
                        }
                        Button(
                            onClick = { vm.addToCart(p, quantity) },
                            enabled = p.inStock,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text(if (p.inStock) "Add · ${(p.price * quantity).asMoney()}" else "Sold out")
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding))
            state.product == null -> EmptyState(
                R.drawable.ic_error,
                "Product unavailable",
                "This product was removed from the shop.",
                Modifier.padding(padding),
            )
            else -> ProductContent(
                product = state.product!!,
                reviews = state.reviews,
                canReview = !state.hasReviewed,
                onWriteReview = { reviewing = true },
                contentPadding = padding,
            )
        }
    }

    val product = state.product
    if (reviewing && product != null) {
        ReviewDialog(
            onDismiss = { reviewing = false },
            onSubmit = { rating, text ->
                vm.submitReview(product, rating, text)
                reviewing = false
            },
        )
    }
}

@Composable
private fun ProductContent(
    product: Product,
    reviews: List<Review>,
    canReview: Boolean,
    onWriteReview: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            ProductImage(
                url = product.image.ifBlank { product.thumbnail },
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).widthIn(max = 600.dp),
            )
        }
        item {
            Column(Modifier.padding(20.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        product.price.asMoney(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        when {
                            !product.inStock -> "Sold out"
                            product.quantity <= 5 -> "Only ${product.quantity} left"
                            else -> "In stock"
                        },
                        color = if (product.quantity <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                if (product.reviewCount > 0) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(product.rating.toInt())
                        Text(
                            "  %.1f · %d reviews".format(product.rating, product.reviewCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (product.shortDescription.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(product.shortDescription, style = MaterialTheme.typography.titleSmall)
                }
                Section("Description", product.description)
                Section("Ingredients", product.ingredients)
                Section("Nutritional facts", product.nutrition)

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reviews", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (canReview) OutlinedButton(onClick = onWriteReview) { Text("Write a review") }
                }
                if (reviews.isEmpty()) {
                    Text(
                        "No reviews yet. Be the first!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
        items(reviews, key = { it.id }) { r ->
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.userName, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(r.time.asDate("d MMM yyyy"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RatingStars(r.rating, size = 14)
                if (r.text.isNotBlank()) Text(r.text, modifier = Modifier.padding(top = 4.dp))
                HorizontalDivider(Modifier.padding(top = 12.dp))
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun Section(title: String, body: String) {
    if (body.isBlank()) return
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by rememberSaveable { mutableIntStateOf(5) }
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this product") },
        text = {
            Column {
                RatingStars(rating, size = 36, onSelect = { rating = it })
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 2000) text = it },
                    label = { Text("Your review (optional)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(rating, text) }) { Text("Post") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

