package com.kiran.movie.shared

import com.kiran.movie.api.KtorMoviesAndSeriesApi
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.data.repository.MoviesAndSeriesRepositoryImpl
import com.kiran.movie.db.BookmarkDatabase
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
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private const val AUTHORIZATION = "Authorization"
private const val BEARER = "Bearer"
private const val BASE_URL = "https://api.themoviedb.org/3/"

/**
 * Initialise Koin for iOS. Call from Swift [AppDelegate.application(_:didFinishLaunchingWithOptions:)]
 * or from SwiftUI @main App.init().
 *
 * @param apiToken the TMDB Bearer token from your iOS secrets / Config.plist
 * @param database a BookmarkDatabase instance created via [createIosRoomDatabase]
 */
fun initKoin(
    apiToken: String,
    database: BookmarkDatabase,
): KoinApplication {
    val app =
        startKoin {
            modules(
                iosNetworkModule(apiToken),
                iosDatabaseModule(database),
                sharedModule,
            )
        }
    KoinHelper.setKoinApp(app)
    return app
}

fun iosNetworkModule(apiToken: String) =
    module {
        single<HttpClient> {
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
                install(Logging) {
                    level = LogLevel.INFO
                    logger =
                        object : Logger {
                            override fun log(message: String) {
                                println("Ktor: $message")
                            }
                        }
                }
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 60000
                    connectTimeoutMillis = 60000
                    socketTimeoutMillis = 60000
                }
                install(io.ktor.client.plugins.HttpRequestRetry) {
                    retryOnExceptionOrServerErrors(maxRetries = 3)
                    exponentialDelay()
                    retryIf { request, response ->
                        !response.status.isSuccess() && (response.status.value == 429 || response.status.value >= 500)
                    }
                }
                defaultRequest {
                    url(BASE_URL)
                    header(AUTHORIZATION, "$BEARER $apiToken")
                    header(io.ktor.http.HttpHeaders.Accept, "application/json")
                    header(io.ktor.http.HttpHeaders.UserAgent, "MoviesApp/1.0 (iOS; Kotlin Multiplatform)")
                }
            }
        }

        single<MoviesAndSeriesApi> { KtorMoviesAndSeriesApi(get()) }
    }

fun iosDatabaseModule(database: BookmarkDatabase) =
    module {
        single<BookmarkDatabase> { database }
    }

val sharedModule =
    module {
        single<MoviesAndSeriesRepository> { MoviesAndSeriesRepositoryImpl(get(), get()) }
        single<com.kiran.movie.domain.repository.AiRepository> {
            com.kiran.movie.data.repository
                .AiRepositoryImpl(get())
        }

        factoryOf(::GetMoviesListUseCase)
        factoryOf(::DiscoverMoviesListUseCase)
        factoryOf(::GetTvShowsListUseCase)
        factoryOf(::GetUpcomingMoviesUseCase)
        factoryOf(::GetItemDetailsUseCase)
        factoryOf(::GetAllBookmarksUseCase)
        factoryOf(::GetBookmarkedIdsUseCase)
        factoryOf(::ToggleBookmarkUseCase)
        factoryOf(::GetMoviesUseCase)
        factoryOf(::GetTvShowsUseCase)
        single<com.kiran.movie.domain.voice.SpeechRecognizer> { com.kiran.movie.data.voice.IosSpeechRecognizer() }
        single<com.kiran.movie.domain.voice.VoiceSynthesizer> { com.kiran.movie.domain.voice.IosVoiceSynthesizer() }
        factory { AiSearchUseCase(get()) }
        factory { SharedAiSearchViewModel(get(), get(), get(), get(), get(), get()) }
    }

object KoinHelper {
    private lateinit var koinApp: KoinApplication

    fun setKoinApp(app: KoinApplication) {
        koinApp = app
    }

    fun getGetMoviesListUseCase(): GetMoviesListUseCase = koinApp.koin.get()

    fun getDiscoverMoviesListUseCase(): DiscoverMoviesListUseCase = koinApp.koin.get()

    fun getGetTvShowsListUseCase(): GetTvShowsListUseCase = koinApp.koin.get()

    fun getGetUpcomingMoviesUseCase(): GetUpcomingMoviesUseCase = koinApp.koin.get()

    fun getGetItemDetailsUseCase(): GetItemDetailsUseCase = koinApp.koin.get()

    fun getGetAllBookmarksUseCase(): GetAllBookmarksUseCase = koinApp.koin.get()

    fun getGetBookmarkedIdsUseCase(): GetBookmarkedIdsUseCase = koinApp.koin.get()

    fun getToggleBookmarkUseCase(): ToggleBookmarkUseCase = koinApp.koin.get()

    fun getGetMoviesUseCase(): GetMoviesUseCase = koinApp.koin.get()

    fun getGetTvShowsUseCase(): GetTvShowsUseCase = koinApp.koin.get()

    fun getSharedAiSearchViewModel(): SharedAiSearchViewModel = koinApp.koin.get()

    fun getAiSearchUseCase(): com.kiran.movie.domain.usecase.AiSearchUseCase = koinApp.koin.get()
}

object FlowHelper {
    fun <T> collectStateFlow(
        flow: StateFlow<T>,
        onNext: (T) -> Unit,
    ): CloseableJob {
        val job =
            CoroutineScope(Dispatchers.Main).launch {
                flow.collect {
                    onNext(it)
                }
            }
        return CloseableJob(job)
    }
}

class CloseableJob(
    private val job: Job,
) {
    fun cancel() {
        job.cancel()
    }
}
