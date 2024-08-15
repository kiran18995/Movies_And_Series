package com.kiran.movie.di

import android.content.Context
import androidx.room.Room
import com.kiran.movie.BuildConfig
import com.kiran.movie.api.MoviesAndSeriesApi
import com.kiran.movie.data.repository.MoviesAndSeriesRepository
import com.kiran.movie.data.repository.MoviesAndSeriesRepositoryImpl
import com.kiran.movie.db.BookmarkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val AUTHORIZATION_TOKEN = BuildConfig.API_READ_ACCESS_TOKEN
    private const val AUTHORIZATION = "Authorization"
    private const val BEARER = "Bearer"
    private const val TIMEOUT = 60L
    private const val BOOKMARK_DATABASE = "BookmarkDatabase"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(AUTHORIZATION, "$BEARER $AUTHORIZATION_TOKEN").build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder().readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS).addInterceptor(logging)
            .addInterceptor(authInterceptor).build()

        return Retrofit.Builder().baseUrl(MoviesAndSeriesApi.BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun provideMoviesAPi(retrofit: Retrofit): MoviesAndSeriesApi =
        retrofit.create(MoviesAndSeriesApi::class.java)

    @Provides
    @Singleton
    fun provideSavedQuotesDatabase(@ApplicationContext context: Context): BookmarkDatabase {
        return Room.databaseBuilder(
            context,
            BookmarkDatabase::class.java,
            BOOKMARK_DATABASE
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideMoviesAndSeriesRepository(
        api: MoviesAndSeriesApi,
        bookmarkDatabase: BookmarkDatabase
    ): MoviesAndSeriesRepository {
        return MoviesAndSeriesRepositoryImpl(api, bookmarkDatabase)
    }
}