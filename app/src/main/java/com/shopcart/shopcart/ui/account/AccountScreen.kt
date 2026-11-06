package com.shopcart.shopcart.ui.account

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import coil3.compose.AsyncImage
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.data.UserProfile
import com.shopcart.shopcart.ui.components.AppIcon
import com.shopcart.shopcart.ui.components.LoadingBox
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountViewModel(private val auth: AuthRepository) : ViewModel() {
    private val uid = auth.requireUid()

    val profile: StateFlow<UserProfile?> = auth.profile(uid)
        .catch { emit(UserProfile(uid, email = auth.userNow?.email.orEmpty())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var saving by mutableStateOf(false)
        private set
    var saved by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun save(name: String, surname: String, phone: String, address: String) {
        viewModelScope.launch {
            saving = true
            error = null
            runCatching { auth.updateProfile(uid, name, surname, phone, address) }
                .onSuccess { saved = true }
                .onFailure { error = "Couldn't save your details. Try again." }
            saving = false
        }
    }

    fun signOut() = auth.signOut()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onEditProfile: () -> Unit, onOpenChat: () -> Unit, onOpenOrders: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { AccountViewModel(container.auth) }
    val profile by vm.profile.collectAsStateWithLifecycle()
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Account") }) }) { padding ->
        val p = profile
        if (p == null) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(p)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(p.fullName.ifBlank { "Your profile" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(p.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                MenuRow(R.drawable.ic_edit, "Personal details", p.phone.ifBlank { "Add your phone and address" }, onEditProfile)
                HorizontalDivider()
                MenuRow(R.drawable.ic_receipt, "Orders", "Track and review your orders", onOpenOrders)
                HorizontalDivider()
                MenuRow(R.drawable.ic_chat, "Help & support", "Chat with the ShopCart team", onOpenChat)
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                MenuRow(R.drawable.ic_logout, "Sign out", null) { confirmSignOut = true }
            }
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("Your cart and favourites stay saved to your account.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; vm.signOut() }) { Text("Sign out") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Avatar(p: UserProfile) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(64.dp)) {
        Box(contentAlignment = Alignment.Center) {
            if (p.image.isNotBlank()) {
                AsyncImage(
                    model = p.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    (p.name.firstOrNull() ?: p.email.firstOrNull() ?: '?').uppercaseChar().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun MenuRow(@DrawableRes icon: Int, title: String, subtitle: String?, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle != null) { { Text(subtitle) } } else null,
        leadingContent = { AppIcon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val container = appContainer()
    val vm = viewModel { AccountViewModel(container.auth) }
    val profile by vm.profile.collectAsStateWithLifecycle()

    var name by rememberSaveable { mutableStateOf<String?>(null) }
    var surname by rememberSaveable { mutableStateOf<String?>(null) }
    var phone by rememberSaveable { mutableStateOf<String?>(null) }
    var address by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(profile) {
        profile?.let {
            if (name == null) name = it.name
            if (surname == null) surname = it.surname
            if (phone == null) phone = it.phone
            if (address == null) address = it.address
        }
    }
    LaunchedEffect(vm.saved) { if (vm.saved) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal details") },
                navigationIcon = { IconButton(onClick = onBack) { AppIcon(R.drawable.ic_arrow_back, "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name.orEmpty(), { name = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(surname.orEmpty(), { surname = it }, label = { Text("Surname") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(
                phone.orEmpty(),
                { phone = it },
                label = { Text("Mobile number") },
                placeholder = { Text("0771234567") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                address.orEmpty(),
                { address = it },
                label = { Text("Delivery address") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.save(name.orEmpty(), surname.orEmpty(), phone.orEmpty(), address.orEmpty()) },
                enabled = !vm.saving && !name.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(if (vm.saving) "Saving…" else "Save") }
        }
    }
}
