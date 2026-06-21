# 🌍 KMP Migration & Cross-Platform Expansion

Welcome to the newly evolved **Movies & Series** architecture! 

This document outlines how the originally Android-only codebase was successfully migrated to **Kotlin Multiplatform (KMP)** to share business logic across Android and iOS, as well as the newly introduced web application.

---

## 🏗️ What Was Migrated?

The application has transitioned from a standard Android Clean Architecture to a unified **Kotlin Multiplatform Mobile (KMM)** repository. 

### 1. The `shared` Module
We created a new `shared` module that compiles to an Android library (`.aar`) and an iOS framework (`.framework`). This module now houses:
*   **Networking:** Migrated from Retrofit to **Ktor** for multiplatform HTTP requests.
*   **Database:** Migrated to the experimental **Room KMP (v2.7.0-alpha)** using SQLite bundled drivers to share local bookmark storage.
*   **Domain Logic:** All Use Cases (`GetMoviesUseCase`, `ToggleBookmarkUseCase`, etc.) are now completely shared.
*   **ViewModels:** The `HomeViewModel`, `DetailViewModel`, `SavedViewModel`, and `TvShowsViewModel` were moved into `shared` using Kotlin Coroutines `StateFlow` so both Android and iOS can share presentation logic.

### 2. The iOS App Refactor
The native Swift codebase (`iosApp`) was deeply refactored:
*   It now directly consumes the KMP `shared.framework`.
*   SwiftUI Views subscribe to Kotlin `StateFlow`s using an interoperability `FlowHelper` powered by Coroutines.
*   All data loading and bookmarking logic is executed natively in Kotlin Core, meaning Swift only handles the declarative UI rendering and routing.

### 3. The New Web App
A brand new Vanilla HTML/CSS/JS frontend was built in the `web/` directory. 
*   Powered by a minimal Node.js `Express` server (`server.js`).
*   Implements an exact replica of the native UI with debounced search, hero banners, and a responsive grid.
*   Embeds a high-quality streaming proxy (`streamimdb.ru`) for direct movie and TV playback.

---

## 🚀 How to Share the Web App URL with Others

Currently, the web app runs locally on your machine. To share it with friends or testers across the internet, you can use a tunneling service to expose your local port (`8080`) to the public web.

Here are the two easiest ways to do it:

### Option 1: Using Localtunnel (Fastest, requires Node.js)
If you already have `npm` or `npx` installed, you can generate a public URL instantly without an account.

1. First, make sure your local web app is running:
   ```bash
   cd web
   node server.js
   # Running on http://localhost:8080
   ```
2. Open a **new terminal tab** and run:
   ```bash
   npx localtunnel --port 8080
   ```
3. It will print a URL like `https://fuzzy-ants-jump.loca.lt`. 
4. **Share this URL!** Anyone who clicks it can test your web app.

### Option 2: Using Ngrok (More stable)
Ngrok is the industry standard for localhost tunneling.

1. Install Ngrok via Homebrew (Mac):
   ```bash
   brew install ngrok/ngrok/ngrok
   ```
2. Ensure your local server is running (`node server.js`).
3. In a new terminal, run:
   ```bash
   ngrok http 8080
   ```
4. Ngrok will provide a "Forwarding" URL (e.g., `https://a1b2c3d4.ngrok-free.app`). **Share this URL with your testers!**

> **Note:** Whenever you stop your terminal command, the public link will stop working. Your computer must remain awake and running the commands for others to access it.
