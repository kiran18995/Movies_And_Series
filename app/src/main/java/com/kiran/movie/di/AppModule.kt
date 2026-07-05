package com.kiran.movie.di

import androidx.room.Room
import com.kiran.movie.BuildConfig
import com.kiran.movie.MainViewModel
import com.kiran.movie.api.KtorMoviesAndSeriesApi
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.core.ui.details.ItemDetailsViewModel
import com.kiran.movie.data.repository.AiRepositoryImpl
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.data.repository.MoviesAndSeriesRepositoryImpl
import com.kiran.movie.data.voice.AndroidSpeechRecognizer
import com.kiran.movie.db.BookmarkDatabase
import com.kiran.movie.db.getRoomDatabase
import com.kiran.movie.domain.repository.AiRepository
import com.kiran.movie.domain.usecase.AiDiscoverUseCase
import com.kiran.movie.domain.usecase.AiSearchUseCase
import com.kiran.movie.domain.usecase.DiscoverMoviesListUseCase
import com.kiran.movie.domain.usecase.GetAllBookmarksUseCase
import com.kiran.movie.domain.usecase.GetBookmarkedIdsUseCase
import com.kiran.movie.domain.usecase.GetItemDetailsUseCase
import com.kiran.movie.domain.usecase.GetMoviesListUseCase
import com.kiran.movie.domain.usecase.GetMoviesUseCase
import com.kiran.movie.domain.usecase.GetTvShowsListUseCase
import com.kiran.movie.domain.usecase.GetTvShowsUseCase
import com.kiran.movie.domain.usecase.GetUpcomingMoviesUseCase
import com.kiran.movie.domain.usecase.ToggleBookmarkUseCase
import com.kiran.movie.domain.voice.SpeechRecognizer
import com.kiran.movie.voice.AndroidVoiceSynthesizer
import com.kiran.movie.domain.voice.VoiceSynthesizer
import com.kiran.movie.shared.SharedAiSearchViewModel
import com.kiran.movie.ui.movies.MoviesViewModel
import com.kiran.movie.ui.saved.SavedViewModel
import com.kiran.movie.ui.tvshows.TvShowsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val AUTHORIZATION_TOKEN = BuildConfig.API_READ_ACCESS_TOKEN
private const val AUTHORIZATION = "Authorization"
private const val BEARER = "Bearer"
private const val BOOKMARK_DATABASE = "BookmarkDatabase"

val appModule =
    module {
        single<HttpClient> {
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
                if (BuildConfig.DEBUG) {
                    install(Logging) {
                        level = LogLevel.INFO
                        logger =
                            object : Logger {
                                override fun log(message: String) {
                                    println("Ktor: $message")
                                }
                            }
                    }
                }
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 15000
                    connectTimeoutMillis = 15000
                    socketTimeoutMillis = 15000
                }
                install(io.ktor.client.plugins.HttpRequestRetry) {
                    retryOnExceptionOrServerErrors(maxRetries = 3)
                    exponentialDelay()
                    retryIf { _, response ->
                        !response.status.isSuccess() && (response.status.value == 429 || response.status.value >= 500)
                    }
                }
                defaultRequest {
                    url(BuildConfig.BASE_URL)
                    header(AUTHORIZATION, "$BEARER $AUTHORIZATION_TOKEN")
                    header(io.ktor.http.HttpHeaders.Accept, "application/json")
                    header(io.ktor.http.HttpHeaders.UserAgent, "MoviesApp/1.0 (Android; Kotlin Multiplatform)")
                }
            }
        }

        single<MoviesAndSeriesApi> { KtorMoviesAndSeriesApi(get()) }

        // Separate clean HttpClient for Gemini AI - NO TMDB base URL or auth headers
        single<HttpClient>(named("gemini")) {
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            println("GeminiHttp: $message")
                        }
                    }
                }
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 20000
                    connectTimeoutMillis = 15000
                }
            }
        }

        single<BookmarkDatabase> {
            val context = androidContext()
            val dbFile = context.getDatabasePath(BOOKMARK_DATABASE)
            val builder =
                Room.databaseBuilder(
                    context,
                    BookmarkDatabase::class.java,
                    dbFile.absolutePath,
                )
            getRoomDatabase(builder)
        }

        single<MoviesAndSeriesRepository> { MoviesAndSeriesRepositoryImpl(get(), get()) }

        factoryOf(::ToggleBookmarkUseCase)
        factoryOf(::GetTvShowsListUseCase)
        factoryOf(::GetUpcomingMoviesUseCase)
        factoryOf(::GetAllBookmarksUseCase)
        factoryOf(::GetBookmarkedIdsUseCase)
        factoryOf(::GetMoviesUseCase)
        factoryOf(::GetMoviesListUseCase)
        factoryOf(::DiscoverMoviesListUseCase)
        factoryOf(::GetItemDetailsUseCase)
        factoryOf(::GetTvShowsUseCase)
        factoryOf(::AiDiscoverUseCase)
        factory { AiSearchUseCase(get()) }

        single<AiRepository> { AiRepositoryImpl(get(named("gemini")), BuildConfig.GEMINI_API_KEY) }
        single<SpeechRecognizer> { AndroidSpeechRecognizer(androidContext()) }
        single<VoiceSynthesizer> { AndroidVoiceSynthesizer(androidContext()) }

        viewModelOf(::MainViewModel)
        viewModelOf(::ItemDetailsViewModel)
        viewModelOf(::MoviesViewModel)
        viewModelOf(::TvShowsViewModel)
        viewModelOf(::SavedViewModel)
        factory { SharedAiSearchViewModel(get(), get(), get(), get()) }
    }
