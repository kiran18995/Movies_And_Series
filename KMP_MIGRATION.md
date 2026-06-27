# 🌍 KMP Migration & Cross-Platform Expansion

Welcome to the evolved **Movies & Series** architecture!

This document explains how the originally Android-only codebase was migrated to **Kotlin Multiplatform (KMP)** to share business logic across Android, iOS, and a new web app.

---

## 🏗️ What Was Migrated?

The app transitioned from a standard Android multi-module Clean Architecture to a **Kotlin Multiplatform** repository targeting **Android**, **iOS** (iosX64, iosArm64, iosSimulatorArm64), and a **Node.js web frontend**.

---

## 📦 Module Overview (Post-Migration)

```
Movies_And_Series/
├── :app                   # Android entry point (Jetpack Compose + Hilt)
├── :shared                # KMP bridge — exports framework to iOS, sets up Koin DI
├── :domain                # commonMain — Use Cases + Repository interface
├── :data                  # commonMain — Repository implementation + paging DataSource
├── :core:network          # commonMain — Ktor API interface + KtorMoviesAndSeriesApi
├── :core:database         # commonMain + androidMain + iosMain — Room KMP BookmarkDatabase
├── :core:ui               # Android-only — Compose components, theme, ViewModels
├── :core:common           # Shared utilities
├── :feature:movies        # Android-only — Movies screen (Compose + MVI ViewModel)
├── :feature:tvshows       # Android-only — TV Shows screen (Compose + MVI ViewModel)
├── :feature:saved         # Android-only — Saved/Bookmarks screen (Compose + MVI ViewModel)
├── iosApp/                # SwiftUI iOS app consuming the :shared framework
└── web/                   # Vanilla JS + Node.js/Express web frontend
```

---

## 🔄 Key Migrations

### 1. Networking — Retrofit → Ktor (KMP)

The `:core:network` module was rewritten from Retrofit to **Ktor**, enabling it to compile on both Android and iOS as `commonMain`.

| Before | After |
|---|---|
| `Retrofit` + `OkHttp` + `Gson` | `Ktor` + `kotlinx.serialization` |
| Android-only | `commonMain` (Android + iOS) |
| `MoviesAndSeriesApi` (Retrofit interface) | `KtorMoviesAndSeriesApi` (Ktor implementation) |

The `HttpClient` is configured per-platform:
- **Android**: `OkHttp` engine (via Hilt DI in `:app`)
- **iOS**: `Darwin` engine (`ktor-client-darwin`), configured in `IosKoinSetup.kt`

Features baked in on iOS:
- `ContentNegotiation` with `kotlinx.serialization`
- `Logging` at `LogLevel.INFO`
- `HttpTimeout` — 60s request/connect/socket
- `HttpRequestRetry` — 3 retries with exponential backoff on 429/5xx

---

### 2. Local Database — Room Android → Room KMP

`:core:database` was migrated to **Room KMP (v2.7.0-alpha11)** using the bundled SQLite driver, sharing the schema across platforms.

| Layer | What's shared |
|---|---|
| `BookmarkEntity.kt` | `commonMain` — the Room `@Entity` |
| `BookmarkDataDao.kt` | `commonMain` — DAO with Flow queries |
| `BookmarkDatabase.kt` | `commonMain` — `@Database` definition |
| `BookmarkDatabaseConstructor.kt` | `commonMain` — `RoomDatabaseConstructor` |
| `Mappers.kt` | `commonMain` — entity ↔ domain model mappers |
| `RoomDatabaseFactory.kt` | `androidMain` — Android Room builder |
| `IosRoomDatabaseFactory.kt` | `iosMain` — iOS Room builder using bundled SQLite |

---

### 3. Domain & Data Layers — Now commonMain

Both `:domain` and `:data` are now fully **`commonMain`** KMP modules.

**Use Cases in `:domain`:**
- `GetMoviesListUseCase` — paginated / searched movie list
- `GetTvShowsListUseCase` — paginated / searched TV show list
- `GetUpcomingMoviesUseCase` — upcoming movies list
- `GetItemDetailsUseCase` — full details for a movie or TV show
- `GetAllBookmarksUseCase` — live `Flow<List<Item>>` of all bookmarks
- `GetBookmarkedIdsUseCase` — live `Flow<Set<Int>>` of bookmarked IDs
- `ToggleBookmarkUseCase` — insert or delete a bookmark atomically
- `GetMoviesUseCase` — raw movie fetch by page
- `GetTvShowsUseCase` — raw TV show fetch by page
- `DiscoverMoviesListUseCase` — discover-endpoint movie list

---

### 4. The `:shared` Module — iOS Bridge

The `:shared` module compiles to a `.framework` for iOS and serves two purposes:

**a) Shared ViewModels** (`SharedViewModels.kt`) — Plain Kotlin classes (not Android `ViewModel`) that manage a `CoroutineScope` internally and expose `StateFlow` for Swift to observe:

| ViewModel | Exposed StateFlows |
|---|---|
| `HomeViewModel` | `movies`, `tvShows`, `upcomingMovies`, `isLoading` |
| `DetailViewModel` | `details`, `isLoading` |
| `SavedViewModel` | `bookmarks` |

**b) iOS Koin DI Setup** (`IosKoinSetup.kt`):
- `initKoin(apiToken, database)` — called once from Swift `App.init()`
- `iosNetworkModule(apiToken)` — sets up Ktor `HttpClient` with Darwin engine + TMDB bearer token
- `iosDatabaseModule(database)` — binds the pre-built Room DB
- `sharedModule` — registers all use cases + repository via Koin `factoryOf`
- `KoinHelper` — singleton object exposing named getters for each use case (called from Swift)
- `FlowHelper.collectStateFlow(...)` — bridges Kotlin `StateFlow<T>` to a Swift callback via `CloseableJob`

---

### 5. The iOS App — SwiftUI consuming the KMP Framework

The `iosApp/iosApp/` SwiftUI app directly imports `shared.framework`.

**Entry Point (`iOSApp.swift`):**
```swift
import shared

@main
struct iOSApp: App {
    init() {
        let db = IosRoomDatabaseFactoryKt.createIosRoomDatabase()
        let apiToken = Bundle.main.infoDictionary?["TMDB_API_TOKEN"] as? String ?? ""
        IosKoinSetupKt.doInitKoin(apiToken: apiToken, database: db)
    }
}
```

**Swift Views:**

| View | Description |
|---|---|
| `ContentView.swift` | `TabView` with 4 tabs: Movies, TV Shows, Saved, Search |
| `HomeView.swift` | Consumes `HomeViewModel` via `KoinHelper` |
| `TvShowsView.swift` | Consumes TV shows from shared use cases |
| `SavedView.swift` | Consumes `SavedViewModel` — shows bookmarks |
| `DetailView.swift` | Consumes `DetailViewModel` — full item details |
| `Components.swift` | Shared SwiftUI components (e.g. `PosterCard`) |
| `SearchView.swift` | In-tab search using `GetMoviesListUseCase` / `GetTvShowsListUseCase` directly |

The TMDB API token is stored in `Config.plist` (gitignored) and read at runtime via `Bundle.main.infoDictionary`.

---

### 6. The Web App — Vanilla JS + Node.js

A standalone web frontend lives in `web/` and mirrors the native app experience.

**Stack:**
- `index.html` + `style.css` + `app.js` — pure HTML/CSS/JS frontend
- `server.js` — Node.js Express server on port `8080`

**Server responsibilities:**
- Serves the static frontend
- **TMDB Proxy** (`/api/*` → `https://api.themoviedb.org/3/*`) — injects the Bearer token server-side from `local.properties`, keeping the token off the client
- **Stream Proxy** (`/stream/*` → `https://streamimdb.ru/*`) — rewrites root-relative paths and strips `X-Frame-Options` / `frame-ancestors` CSP headers so the embed renders inside an `<iframe>` without browser blocking

**Running locally:**
```bash
cd web
npm install
node server.js
# → http://localhost:8080
```

---

## 🚀 Sharing the Web App Publicly

The web app runs locally on port `8080`. To share it with others during development:

### Option 1: Localtunnel (Fastest — no account needed)

```bash
# Terminal 1 — start the server
cd web && node server.js

# Terminal 2 — expose to internet
npx localtunnel --port 8080
# → https://some-random-name.loca.lt
```

### Option 2: Ngrok (More stable)

```bash
# Install
brew install ngrok/ngrok/ngrok

# Expose
ngrok http 8080
# → https://a1b2c3d4.ngrok-free.app
```

> **Note:** The public link stops working the moment you stop the command. Your machine must stay awake and running for others to access it.

---

## 🛠️ Tech Stack — Post-Migration

| Layer | Technology | Version | Platform |
|---|---|---|---|
| Language | **Kotlin** | 2.0.10 | All |
| DI (Android) | **Hilt** | 2.51.1 | Android |
| DI (iOS) | **Koin** | 3.5.3 | iOS / Shared |
| Networking | **Ktor** | 2.3.11 | commonMain |
| Serialization | **kotlinx.serialization** | 1.6.3 | commonMain |
| Database | **Room KMP** | 2.7.0-alpha11 | commonMain |
| SQLite Driver | **sqlite-bundled** | 2.5.0-alpha11 | commonMain |
| Async | **Coroutines + StateFlow** | 1.8.1 | commonMain |
| Android UI | **Jetpack Compose + Material 3** | BOM 2024.06.00 | Android |
| iOS UI | **SwiftUI** | — | iOS |
| Web Server | **Node.js + Express** | 5.x | Web |
| Web Proxy | **http-proxy-middleware** | 2.x | Web |
| Image Loading | **Coil** | 2.6.0 | Android |
| Paging | **Paging 3** | 3.3.2 | Android |

---

## 📐 KMP Module Dependency Graph

```
          ┌─────────────────────────────────────────┐
          │                  :app                   │
          │   (Android — Compose, Hilt, Navigation) │
          └────────────────┬────────────────────────┘
                           │ depends on
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    :feature:movies  :feature:tvshows  :feature:saved
           └───────────────┼───────────────┘
                           │
                    ┌──────▼──────┐
                    │   :domain   │  ← commonMain
                    │ (Use Cases) │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │    :data    │  ← commonMain
                    │ (Repo Impl) │
                    └──────┬──────┘
              ┌────────────┼────────────┐
              ▼            ▼            ▼
       :core:network  :core:database  :core:ui
       (commonMain)   (commonMain)   (Android)
                           │
                    :core:common (commonMain)

  ┌─────────────────────────────────┐
  │            :shared              │  ← KMP framework for iOS
  │  SharedViewModels + IosKoinSetup│
  │  exports: domain, data, network,│
  │           database              │
  └─────────────────────────────────┘
              consumed by
  ┌─────────────────────────────────┐
  │           iosApp/               │  ← SwiftUI
  │  ContentView, HomeView,         │
  │  TvShowsView, SavedView,        │
  │  DetailView, SearchView         │
  └─────────────────────────────────┘

  web/  ← Standalone Node.js + Vanilla JS (independent of Gradle)
```
