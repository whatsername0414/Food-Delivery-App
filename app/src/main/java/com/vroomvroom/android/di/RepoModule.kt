package com.vroomvroom.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vroomvroom.android.data.db.Database
import com.vroomvroom.android.repository.local.UserPreferences
import com.vroomvroom.android.utils.Constants.PREFERENCES_STORE_NAME
import com.vroomvroom.android.utils.Constants.VROOMVROOM_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(app)

    @Singleton
    @Provides
    fun providePreferences(@ApplicationContext app: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
    produceFile = {
        app.preferencesDataStoreFile(PREFERENCES_STORE_NAME)
    })

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        Database::class.java,
        VROOMVROOM_DATABASE
    ).build()

    @Singleton
    @Provides
    fun provideCartItemDao(db: Database) = db.cartItemDao()

    @Singleton
    @Provides
    fun provideUserDao(db: Database) = db.userDao()

    @Singleton
    @Provides
    fun provideSearchDao(db: Database) = db.searchDao()
}