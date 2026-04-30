<p align="center">
  <h1 align="center">🎬 Movies & Series</h1>
  <p align="center">
    A modern Android application for browsing popular movies and TV shows, powered by <a href="https://www.themoviedb.org/">TMDB API</a>.
    <br />Built with <b>Jetpack Compose</b>, <b>Multi-Module Architecture</b>, and <b>MVI Pattern</b>.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.10-7F52FF?logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Hilt-2.51-2196F3?logo=google&logoColor=white" />
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

- 🎥 **Browse Popular Movies** — Discover trending and popular movies with infinite scroll pagination
- 📺 **Browse Popular TV Shows** — Explore popular TV series with the same seamless experience
- 🔍 **Debounced Search** — Search movies and TV shows with a 300ms debounce to minimize API calls
- 🔖 **Bookmark / Save** — Bookmark your favorite movies and TV shows for quick access offline
- 📂 **Saved Collection** — View all bookmarked items, filtered by Movies or TV Shows tabs
- 🎨 **Material 3 Design** — Modern Material Design 3 with Material You theming
- 📱 **Edge-to-Edge UI** — Immersive experience with auto-hiding search bar and navigation bar on scroll
- 🏗️ **Multi-Module Architecture** — Clean separation of concerns with a scalable module structure
- ⚡ **Offline-First Bookmarks** — Bookmarks are persisted locally using Room with atomic transactions
- 🧪 **Unit Tested** — ViewModel logic tested with MockK and Coroutines Test

---

## 🏛️ Architecture

The app follows **Clean Architecture** principles with a **multi-module** structure and the **MVI (Model-View-Intent)** pattern for unidirectional data flow.

### Module Graph

```
┌─────────────────────────────────────────────────────┐
│                        :app                         │
│           (Navigation, DI, MainActivity)            │
└──────┬──────────────┬──────────────┬────────────────┘
       │              │              │
       ▼              ▼              ▼
 ┌───────────┐ ┌────────────┐ ┌───────────┐
 │ :feature: │ │ :feature:  │ │ :feature: │
 │  movies   │ │  tvshows   │ │   saved   │
 └─────┬─────┘ └─────┬──────┘ └─────┬─────┘
       │              │              │
       └──────────────┼──────────────┘
                      │
              ┌───────▼───────┐
              │   :domain     │
              │  (Use Cases)  │
              └───────┬───────┘
                      │
              ┌───────▼───────┐
              │    :data      │
              │ (Repository)  │
              └───────┬───────┘
                      │
       ┌──────────────┼──────────────┐
       │              │              │
       ▼              ▼              ▼
 ┌───────────┐ ┌────────────┐ ┌───────────┐
 │  :core:   │ │   :core:   │ │  :core:   │
 │  network  │ │  database  │ │    ui     │
 └───────────┘ └────────────┘ └───────────┘
                      │
              ┌───────▼───────┐
              │ :core:common  │
              └───────────────┘
```

### Module Responsibilities

| Module | Description |
|---|---|
| `:app` | Entry point. Contains `MainActivity`, `AppNavigation`, Hilt setup, and scaffold with immersive scroll behavior |
| `:feature:movies` | Movies screen UI + `MoviesViewModel` (MVI) |
| `:feature:tvshows` | TV Shows screen UI + `TvShowsViewModel` (MVI) |
| `:feature:saved` | Saved/Bookmarks screen UI + `SavedViewModel` (MVI) |
| `:domain` | Business logic layer — Use Cases and repository interface |
| `:data` | Data layer — Repository implementation coordinating network and local data sources |
| `:core:network` | Retrofit API interface (`MoviesAndSeriesApi`), OkHttp interceptors, and network DI |
| `:core:database` | Room database, DAOs, entities, and database DI |
| `:core:ui` | Shared Compose components (`ItemCard`, `EmptyStateScreen`), theme, and `MainViewModel` |
| `:core:common` | Shared utilities and constants |

### Data Flow (MVI)

```
User Action  →  Event  →  ViewModel  →  Use Case  →  Repository  →  API / DB
                              │
                              ▼
                     State (StateFlow)
                              │
                              ▼
                    Compose UI re-renders
```

---

## 🛠️ Tech Stack

### Core

| Technology | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.0.10 | Primary language |
| **Jetpack Compose** | BOM 2024.06.00 | Declarative UI framework |
| **Material 3** | 1.2.1 | Design system |
| **Hilt** | 2.51.1 | Dependency injection |
| **Coroutines + Flow** | 1.8.1 | Asynchronous programming and reactive streams |

### Networking

| Technology | Version | Purpose |
|---|---|---|
| **Retrofit** | 2.9.0 | REST API client |
| **OkHttp** | 4.12.0 | HTTP client with logging interceptor |
| **Gson** | 2.10.1 | JSON serialization / deserialization |

### Local Persistence

| Technology | Version | Purpose |
|---|---|---|
| **Room** | 2.6.1 | SQLite abstraction for bookmark storage |
| **Paging 3** | 3.3.2 | Infinite scroll pagination with Compose integration |

### UI & Image Loading

| Technology | Version | Purpose |
|---|---|---|
| **Coil** | 2.6.0 | Image loading with Compose support |
| **Toasty** | 1.5.2 | Custom toast messages |
| **Shimmer** | 0.5.0 | Loading placeholder animations |

### Testing & Debugging

| Technology | Version | Purpose |
|---|---|---|
| **JUnit** | 4.13.2 | Unit testing framework |
| **MockK** | 1.13.10 | Mocking library for Kotlin |
| **Coroutines Test** | 1.8.1 | Testing coroutines and flows |
| **LeakCanary** | 2.14 | Memory leak detection (debug only) |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2.1) or later
- **JDK 17** or later
- **Android SDK** with API 35 (compile) and API 24+ (min)
- A **TMDB API** read access token ([get one here](https://www.themoviedb.org/settings/api))

### Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/Movies_And_Series.git
   cd Movies_And_Series
   ```

2. **Add your TMDB API token**

   Open `local.properties` (gitignored — never commit this file) and add your read access token:

   ```properties
   API_READ_ACCESS_TOKEN=your_tmdb_read_access_token_here
   ```

   Get a free token at [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api).

3. **Build and run**

   ```bash
   ./gradlew assembleDebug
   ```

   Or simply open the project in Android Studio and press **Run ▶**.

### Running Tests

```bash
./gradlew testDebugUnitTest
```

---

## 📁 Project Structure

```
Movies_And_Series/
├── app/                          # Application module
│   └── navigation/               # AppNavigation (Scaffold, NavHost, scroll behavior)
├── core/
│   ├── common/                   # Shared utilities
│   ├── database/                 # Room DB, DAOs, entities
│   ├── network/                  # Retrofit API, interceptors, DI
│   └── ui/                       # Shared Compose components, theme, MainViewModel
├── data/                         # Repository implementation
├── domain/                       # Use cases and repository interface
├── feature/
│   ├── movies/                   # Movies screen (UI + ViewModel + Contract)
│   ├── tvshows/                  # TV Shows screen (UI + ViewModel + Contract)
│   └── saved/                    # Saved/Bookmarks screen (UI + ViewModel + Contract)
├── screenshots/                  # App screenshots for README
├── gradle/
│   └── libs.versions.toml        # Version catalog
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
