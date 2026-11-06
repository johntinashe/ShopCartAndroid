# ShopCart for Android (v2)

E‑commerce app for Zimbabwe with EcoCash, OneMoney and Paynow checkout.
Originally a 2018 final‑year project in Java; rebuilt in 2026 with Kotlin and
Jetpack Compose.

The admin dashboard and Firebase backend live in
[ShopCartAdmin](https://github.com/johntinashe/ShopCartAdmin), and must be
deployed for checkout to work.

## Features
- Email sign‑in / registration, password reset
- Shop: search, category filters, featured products, product details with ingredients and nutrition facts
- Ratings and reviews
- Favourites
- Cart synced to your account (works across devices)
- Checkout with **EcoCash / OneMoney** (approve on your phone) or **Paynow web checkout**
- Live payment status, order history and order tracking timeline
- Push notifications when an order is paid, shipped or delivered
- In‑app support chat with the shop
- Light and dark theme, edge‑to‑edge

## Architecture

```
ui/            Compose screens + a ViewModel per screen (StateFlow)
data/          Repositories over Firestore / Cloud Functions, models, mappers
AppContainer   Manual dependency injection (one instance of each repository)
notifications/ FCM service
```

- **MVVM + unidirectional data flow**: repositories expose `Flow`s from Firestore snapshot listeners; ViewModels combine them into screen state; screens collect with `collectAsStateWithLifecycle`.
- **Server‑side checkout**: the app never calculates the amount charged. It calls the `placeOrder` Cloud Function, which reads the cart, checks stock and prices, creates the order and starts the Paynow payment.
- **Hand‑written Firestore mappers** (`data/Mappers.kt`) so data written by the 2018 app (mixed number/timestamp types) can't crash the app.

## Requirements
- Android Studio (latest stable) with JDK 17+
- Android SDK 37 (Android Studio installs it on first sync)
- The backend from ShopCartAdmin deployed to the same Firebase project

## Build
```bash
./gradlew assembleDebug        # or open the project in Android Studio and Run
```

`app/google-services.json` is for the `shopcart-6fc67` Firebase project. To use
a different project, register the app (package `com.shopcart.shopcart`) and
replace that file.

## Tech
Kotlin 2.4 · Jetpack Compose (BOM 2026.09) · Material 3 · Navigation Compose ·
Lifecycle ViewModel · Coroutines/Flow · Firebase (Auth, Firestore, Functions,
Messaging; BoM 35.1) · Coil 3 · AGP 9.5 / Gradle 9.6

## Not carried over from 2018
QR code scanning and back‑in‑stock alerts were dropped in this version. Profile
photo upload is not wired up yet (existing photos still display).
