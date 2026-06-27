<p align="center">
  <h1 align="center">🎬 Movies & Series</h1>
  <p align="center">
    A cross-platform app for browsing popular movies and TV shows, powered by <a href="https://www.themoviedb.org/">TMDB API</a>.
    <br />Built with <b>Kotlin Multiplatform</b>, targeting <b>Android</b>, <b>iOS</b>, and <b>Web</b>.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.10-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/KMP-Multiplatform-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/SwiftUI-iOS-000000?logo=swift&logoColor=white" />
  <img src="https://img.shields.io/badge/Node.js-Web-339933?logo=nodedotjs&logoColor=white" />
  <img src="https://img.shields.io/badge/API-24%2B-brightgreen" />
  <img src="https://img.shields.io/badge/License-Apache_2.0-blue" />
</p>

---

## 📸 Screenshots

<p align="center">
  <img width="200" alt="Screenshot_20260501_005629" src="https://github.com/user-attachments/assets/dab8fe8b-d669-4f2b-a829-2ac1714b15a6" />
<img width="200" alt="Screenshot_20260501_005643" src="https://github.com/user-attachments/assets/94e1e914-29ff-4ee4-8579-e1b2c48e4f9e" />
<img width="200" alt="Screenshot_20260501_005951" src="https://github.com/user-attachments/assets/df0016c2-4897-46df-97ec-31671ce8fe17" />
</p>

<p align="center">
  <i>Movies • TV Shows • Saved Movies • Saved TV Shows</i>
</p>

---

## ✨ Features

### Cross-Platform
- 📱 **Android** — Jetpack Compose + Material 3 + Hilt + Paging 3
- 🍎 **iOS** — SwiftUI consuming shared Kotlin business logic via `.framework`
- 🌐 **Web** — Vanilla JS frontend with Node.js/Express proxy server

### Shared (Android + iOS)
- 🎥 **Browse Popular Movies** — Trending and popular movies
- 📺 **Browse Popular TV Shows** — Popular TV series
- 🔍 **Search** — Search movies and TV shows
- 🔖 **Bookmark / Save** — Bookmark favorites, persisted locally via Room KMP
- 📂 **Saved Collection** — View all bookmarks
- ⚡ **Offline-First Bookmarks** — Room KMP with bundled SQLite driver (Android + iOS)
- 🔗 **Shared Use Cases** — All business logic lives in `commonMain` (Kotlin)

### Android Specific
- 🎨 **Material 3 Design** — Material You theming
- 📱 **Edge-to-Edge UI** — Auto-hiding search bar and bottom nav on scroll
- ♾️ **Infinite Scroll** — Paging 3 with Compose integration
- 🧪 **Unit Tested** — ViewModel logic tested with MockK and Coroutines Test

### Web Specific
- 🔒 **Server-Side TMDB Token** — API key never exposed to the browser
- 🎬 **Streaming Embed** — Built-in stream proxy with iframe rewriting

---

## 🏛️ Architecture

The app follows **Clean Architecture** with a **KMP multi-module** structure. Android uses the **MVI** pattern; iOS consumes shared ViewModels via `StateFlow`; the web is an independent JS frontend.

### KMP Module Graph

```
          ┌─────────────────────────────────────────┐
          │                  :app                   │
          │   (Android — Compose, Hilt, Navigation) │
          └────────────────┬────────────────────────┘
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    :feature:movies  :feature:tvshows  :feature:saved
    (Android MVI)    (Android MVI)     (Android MVI)
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
       Ktor client    Room KMP

  ┌─────────────────────────────────┐
  │            :shared              │  ← KMP .framework for iOS
  │  SharedViewModels               │
  │  IosKoinSetup + KoinHelper      │
  │  FlowHelper (StateFlow bridge)  │
  └─────────────────────────────────┘
              consumed by
  ┌─────────────────────────────────┐
  │  iosApp/ (SwiftUI)              │
  │  ContentView, HomeView,         │
  │  TvShowsView, SavedView,        │
  │  DetailView, SearchView         │
  └─────────────────────────────────┘

  web/  ← Standalone (Node.js + Vanilla JS)
```

### Module Responsibilities

| Module | Platform | Description |
|---|---|---|
| `:app` | Android | `MainActivity`, `AppNavigation`, Hilt setup, immersive scroll scaffold |
| `:feature:movies` | Android | Movies screen UI + `MoviesViewModel` (MVI) |
| `:feature:tvshows` | Android | TV Shows screen UI + `TvShowsViewModel` (MVI) |
| `:feature:saved` | Android | Saved/Bookmarks screen UI + `SavedViewModel` (MVI) |
| `:domain` | commonMain | All Use Cases + `MoviesAndSeriesRepository` interface |
| `:data` | commonMain | `MoviesAndSeriesRepositoryImpl` + `MoviesAndSeriesDataSource` |
| `:core:network` | commonMain | `MoviesAndSeriesApi` interface + `KtorMoviesAndSeriesApi` |
| `:core:database` | commonMain + androidMain + iosMain | Room KMP — `BookmarkDatabase`, `BookmarkDataDao`, `BookmarkEntity` |
| `:core:ui` | Android | Compose components (`ItemCard`, `EmptyStateScreen`), theme, `MainViewModel` |
| `:core:common` | Android | Shared utilities and constants |
| `:shared` | commonMain + iosMain | Shared ViewModels, iOS Koin DI, `FlowHelper` for Swift interop |
| `iosApp/` | iOS | SwiftUI app — `ContentView`, `HomeView`, `TvShowsView`, `SavedView`, `DetailView` |
| `web/` | Web | Node.js Express server + Vanilla JS/CSS/HTML frontend |

### Data Flow

**Android (MVI):**
```
User Action → Intent → ViewModel → Use Case → Repository → Ktor API / Room DB
                           │
                           ▼
                  State (StateFlow)
                           │
                           ▼
                 Compose UI re-renders
```

**iOS (StateFlow bridge):**
```
Swift View → KoinHelper.get<UseCase>() → SharedViewModel.load()
                 │
                 ▼
        FlowHelper.collectStateFlow(vm.movies) { items in
            self.items = items   // @State update → SwiftUI re-render
        }
```

---

## 🛠️ Tech Stack

### Shared (commonMain — Android + iOS)

| Technology | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.0.10 | Primary language |
| **Kotlin Multiplatform** | 2.0.10 | Cross-platform compilation |
| **Ktor** | 2.3.11 | KMP HTTP client (replaces Retrofit) |
| **kotlinx.serialization** | 1.6.3 | JSON serialization (replaces Gson) |
| **Room KMP** | 2.7.0-alpha11 | KMP SQLite database |
| **sqlite-bundled** | 2.5.0-alpha11 | Bundled SQLite driver for iOS |
| **Coroutines + StateFlow** | 1.8.1 | Async + reactive streams |
| **Koin** | 3.5.3 | Multiplatform DI (used on iOS) |

### Android

| Technology | Version | Purpose |
|---|---|---|
| **Jetpack Compose** | BOM 2024.06.00 | Declarative Android UI |
| **Material 3** | 1.3.1 | Design system |
| **Hilt** | 2.51.1 | Android DI |
| **Paging 3** | 3.3.2 | Infinite scroll pagination |
| **Coil** | 2.6.0 | Image loading |
| **Toasty** | 1.5.2 | Custom toasts |
| **Shimmer** | 0.5.0 | Loading placeholders |
| **LeakCanary** | 2.14 | Memory leak detection (debug) |

### iOS

| Technology | Version | Purpose |
|---|---|---|
| **SwiftUI** | — | Declarative iOS UI |
| **shared.framework** | — | KMP compiled framework |
| **Ktor Darwin engine** | 2.3.11 | iOS HTTP engine |
| **Room KMP (iosMain)** | 2.7.0-alpha11 | Shared database on iOS |

### Web

| Technology | Version | Purpose |
|---|---|---|
| **Node.js + Express** | 5.x | Web server + API proxy |
| **http-proxy-middleware** | 2.x | TMDB + stream proxy |
| **Vanilla HTML/CSS/JS** | — | Frontend (no framework) |

### Testing

| Technology | Version | Purpose |
|---|---|---|
| **JUnit** | 4.13.2 | Unit testing |
| **MockK** | 1.13.12 | Kotlin mocking |
| **Coroutines Test** | 1.8.1 | Coroutine/Flow testing |
| **Turbine** | 1.1.0 | Flow testing helper |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or later
- **JDK 17** or later
- **Android SDK** API 35 (compile) / API 24+ (min)
- **Xcode 15+** (for iOS)
- **Node.js 18+** + npm (for web)
- A **TMDB API** read access token ([get one here](https://www.themoviedb.org/settings/api))

### Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/Movies_And_Series.git
   cd Movies_And_Series
   ```

2. **Add your TMDB API token**

   Create/open `local.properties` (gitignored — never commit this) and add:

   ```properties
   API_READ_ACCESS_TOKEN=your_tmdb_read_access_token_here
   ```

   Get a free token at [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api).

### Running Android

```bash
./gradlew assembleDebug
```
Or open in Android Studio and press **Run ▶**.

### Running iOS

1. Build the shared framework:
   ```bash
   ./gradlew :shared:assembleSharedXCFramework
   # or build from Android Studio's Gradle panel
   ```
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Add your TMDB token to `Config.plist` under key `TMDB_API_TOKEN`
4. Press **Run ▶** in Xcode

### Running the Web App

```bash
cd web
npm install
node server.js
# → http://localhost:8080
```

The server reads `API_READ_ACCESS_TOKEN` from the root `local.properties` automatically.

### Running Tests

```bash
./gradlew testDebugUnitTest
```

---

## 📁 Project Structure

```
Movies_And_Series/
├── app/                          # Android entry point (Compose + Hilt + Navigation)
├── shared/                       # KMP module → compiled to .framework for iOS
│   └── src/
│       ├── commonMain/           # SharedViewModels, IosKoinSetup, KoinHelper, FlowHelper
│       └── iosMain/              # (iOS-specific shared overrides if needed)
├── domain/                       # commonMain — Use Cases + Repository interface
├── data/                         # commonMain — Repository impl + paging DataSource
├── core/
│   ├── network/                  # commonMain — Ktor API (KtorMoviesAndSeriesApi)
│   ├── database/                 # commonMain + androidMain + iosMain — Room KMP
│   ├── ui/                       # Android — Compose components, theme, MainViewModel
│   └── common/                   # Android — utilities and constants
├── feature/
│   ├── movies/                   # Android — Movies screen (Compose MVI)
│   ├── tvshows/                  # Android — TV Shows screen (Compose MVI)
│   └── saved/                    # Android — Saved/Bookmarks screen (Compose MVI)
├── iosApp/                       # SwiftUI iOS app
│   └── iosApp/
│       ├── iOSApp.swift          # App entry point — initKoin + createIosRoomDatabase
│       ├── ContentView.swift     # TabView (Movies, TV Shows, Saved, Search)
│       ├── HomeView.swift        # Popular movies + upcoming banner
│       ├── TvShowsView.swift     # Popular TV shows grid
│       ├── SavedView.swift       # Bookmarks list
│       ├── DetailView.swift      # Full item details
│       └── Components.swift      # Shared SwiftUI components (PosterCard, etc.)
├── web/                          # Standalone web app
│   ├── index.html                # App shell
│   ├── app.js                    # All frontend JS logic
│   ├── style.css                 # Styles
│   ├── server.js                 # Express server (TMDB proxy + stream proxy)
│   └── package.json
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── settings.gradle.kts           # Module declarations
└── build.gradle.kts              # Root build configuration
```

---

## 🔑 Key Implementation Details

### Search Debouncing

All search queries are debounced at 300ms using Kotlin `Flow.debounce()` to prevent excessive API calls during rapid typing:

```kotlin
searchQueryFlow
    .debounce(300L)
    .distinctUntilChanged()
    .collectLatest { query ->
        fetchMovies()
    }
```

### Bookmark Reactivity

Bookmark state is managed as a separate `StateFlow<Set<Int>>` in each ViewModel, decoupled from `PagingData`. This ensures **instant UI updates** when toggling bookmarks:

```kotlin
// ViewModel exposes bookmark IDs as reactive state
val bookmarkedIds: StateFlow<Set<Int>> = _bookmarkedIds.asStateFlow()

// Screen overlays bookmark state at render time
val displayItem = item.copy(isBookmarked = bookmarkedIds.contains(item.id))
```

### Atomic Database Operations

Bookmark toggle operations are wrapped in Room transactions to ensure data consistency:

```kotlin
bookmarkDatabase.withTransaction {
    // Atomic insert/delete operations
}
```

### Immersive Scroll Behavior

The search bar and bottom navigation auto-hide on scroll using a custom `NestedScrollConnection`, with a safeguard that locks visibility when the list is empty:

```kotlin
override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
    if (isListEmpty) return Offset.Zero  // Don't hide when list is empty
    // ... hide/show bars based on scroll delta
}
```

---

## 🔌 API Reference

This app uses the [TMDB API v3](https://developer.themoviedb.org/docs):

| Endpoint | Description |
|---|---|
| `GET /movie/popular` | Fetch popular movies (paginated) |
| `GET /tv/popular` | Fetch popular TV shows (paginated) |
| `GET /search/movie` | Search movies by title |
| `GET /search/tv` | Search TV shows by title |

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

```
Copyright 2024 Kiran

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

<p align="center">
  Built with ❤️ using Kotlin & Jetpack Compose
</p>
