package com.shopcart.shopcart.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shopcart.shopcart.R
import com.shopcart.shopcart.data.OrderStatus
import com.shopcart.shopcart.data.Product
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val usd = NumberFormat.getCurrencyInstance(Locale.US)
fun Double.asMoney(): String = usd.format(this)

fun Long.asDate(pattern: String = "d MMM yyyy, HH:mm"): String =
    if (this <= 0) "" else SimpleDateFormat(pattern, Locale.getDefault()).format(Date(this))

@Composable
fun AppIcon(@DrawableRes id: Int, contentDescription: String?, modifier: Modifier = Modifier, tint: Color? = null) {
    if (tint != null) {
        Icon(painterResource(id), contentDescription, modifier, tint = tint)
    } else {
        Icon(painterResource(id), contentDescription, modifier)
    }
}

@Composable
fun ProductImage(url: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
        if (url.isBlank()) {
            AppIcon(R.drawable.ic_cart, null, tint = MaterialTheme.colorScheme.outline)
        } else {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { state ->
                    android.util.Log.w("ShopCart", "Image failed to load: $url", state.result.throwable)
                },
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Box {
            ProductImage(
                url = product.listImage,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentDescription = product.name,
            )
            IconButton(onClick = onToggleFavorite, modifier = Modifier.align(Alignment.TopEnd)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)) {
                    AppIcon(
                        if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border,
                        if (isFavorite) "Remove from favourites" else "Add to favourites",
                        Modifier.padding(6.dp).size(18.dp),
                        tint = if (isFavorite) Color(0xFFD93F3F) else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (!product.inStock) {
                Text(
                    "Sold out",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    product.price.asMoney(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (product.reviewCount > 0) {
                    AppIcon(R.drawable.ic_star, null, Modifier.size(14.dp), tint = Color(0xFFE3A008))
                    Text(
                        "%.1f".format(product.rating),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityStepper(
    value: Int,
    onChange: (Int) -> Unit,
    max: Int,
    modifier: Modifier = Modifier,
    min: Int = 1,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = { onChange(value - 1) }, enabled = value > min, modifier = Modifier.size(36.dp)) {
            AppIcon(if (value <= 1) R.drawable.ic_delete else R.drawable.ic_remove, "Decrease", Modifier.size(18.dp))
        }
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp),
        )
        FilledTonalIconButton(onClick = { onChange(value + 1) }, enabled = value < max, modifier = Modifier.size(36.dp)) {
            AppIcon(R.drawable.ic_add, "Increase", Modifier.size(18.dp))
        }
    }
}

@Composable
fun RatingStars(rating: Int, modifier: Modifier = Modifier, size: Int = 16, onSelect: ((Int) -> Unit)? = null) {
    Row(modifier) {
        for (i in 1..5) {
            val filled = i <= rating
            AppIcon(
                R.drawable.ic_star,
                if (onSelect != null) "$i stars" else null,
                Modifier
                    .size(size.dp)
                    .then(if (onSelect != null) Modifier.clickable { onSelect(i) } else Modifier),
                tint = if (filled) Color(0xFFE3A008) else MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        OrderStatus.New -> Color(0xFF2D5FA8)
        OrderStatus.Shipped -> Color(0xFFA15C00)
        OrderStatus.Delivered -> Color(0xFF1B6B4A)
        OrderStatus.Cancelled, OrderStatus.PaymentFailed -> Color(0xFFB3261E)
        else -> MaterialTheme.colorScheme.outline
    }
    Text(
        status.label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            AppIcon(icon, null, Modifier.padding(20.dp).size(36.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
