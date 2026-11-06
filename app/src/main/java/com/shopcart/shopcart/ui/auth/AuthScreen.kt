package com.shopcart.shopcart.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shopcart.shopcart.R
import com.shopcart.shopcart.appContainer
import com.shopcart.shopcart.data.AuthRepository
import com.shopcart.shopcart.ui.components.AppIcon
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
    var info by mutableStateOf<String?>(null)

    fun signIn(email: String, password: String) = launchAction { auth.signIn(email, password) }

    fun register(name: String, surname: String, email: String, phone: String, password: String) =
        launchAction { auth.register(name, surname, email, phone, password) }

    fun resetPassword(email: String) = launchAction {
        auth.sendPasswordReset(email)
        info = "Password reset link sent to $email."
    }

    private fun launchAction(block: suspend () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true
            error = null
            info = null
            try {
                block()
            } catch (e: Exception) {
                error = friendlyMessage(e)
            } finally {
                busy = false
            }
        }
    }

    private fun friendlyMessage(e: Exception): String = when ((e as? FirebaseAuthException)?.errorCode) {
        "ERROR_INVALID_EMAIL" -> "That email address doesn't look right."
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND" -> "Incorrect email or password."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists. Try signing in."
        "ERROR_WEAK_PASSWORD" -> "Use a password of at least 6 characters."
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please wait a moment and try again."
        else -> e.message ?: "Something went wrong. Check your connection and try again."
    }
}

@Composable
fun AuthScreen() {
    val container = appContainer()
    val vm = viewModel { AuthViewModel(container.auth) }

    var registering by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    val canSubmit = email.isNotBlank() && password.length >= 6 &&
        (!registering || (name.isNotBlank() && surname.isNotBlank()))

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(Modifier.widthIn(max = 420.dp).fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary) {
                    AppIcon(
                        R.drawable.ic_cart,
                        null,
                        Modifier.padding(14.dp).size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    if (registering) "Create your account" else "Welcome back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (registering) "Shop, save favourites and track orders." else "Sign in to continue shopping.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                if (registering) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("First name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text("Surname") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile number") },
                        placeholder = { Text("0771234567") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    supportingText = if (registering) ({ Text("At least 6 characters") }) else null,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )

                vm.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                vm.info?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        if (registering) vm.register(name, surname, email, phone, password)
                        else vm.signIn(email, password)
                    },
                    enabled = canSubmit && !vm.busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (vm.busy) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (registering) "Create account" else "Sign in")
                    }
                }

                if (!registering) {
                    TextButton(
                        onClick = {
                            if (email.isBlank()) vm.error = "Enter your email first, then tap “Forgot password”."
                            else vm.resetPassword(email)
                        },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Forgot password?") }
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (registering) "Already have an account?" else "New to ShopCart?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = {
                        registering = !registering
                        vm.error = null
                    }) { Text(if (registering) "Sign in" else "Create account") }
                }
            }
        }
    }
}
