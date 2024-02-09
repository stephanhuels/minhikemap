package com.example.minhike.dependencyinjection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.minhike.repositories.GpsRepository
import com.example.minhike.repositories.GpsService
import com.example.minhike.repositories.GpxRepository
import com.example.minhike.repositories.GpxService
import com.example.minhike.repositories.PersistenceRepository
import com.example.minhike.repositories.PersistenceService
import com.example.minhike.usecases.GpsUseCase
import com.example.minhike.usecases.GpxUseCase
import com.example.minhike.usecases.PersistenceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.prefs.Preferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext applicationContext: Context
    ): DataStore<androidx.datastore.preferences.core.Preferences> = applicationContext.dataStore

    @Provides
    @Singleton
    fun providePersistenceService(
        preferencesDataStore: DataStore<androidx.datastore.preferences.core.Preferences>
    ): PersistenceService = PersistenceService(preferencesDataStore)

    @Provides
    @Singleton
    fun providePersistenceRepository(
        persistenceService: PersistenceService
    ): PersistenceRepository = PersistenceRepository(
        persistenceService
    )

    @Provides
    @Singleton
    fun providePersistenceUseCase(
        persistenceRepository: PersistenceRepository
    ): PersistenceUseCase = PersistenceUseCase(
        persistenceRepository
    )

    @Provides
    @Singleton
    fun provideGpsService(
    ): GpsService = GpsService()

    @Provides
    @Singleton
    fun provideGpsRepository(
        gpsService: GpsService
    ): GpsRepository = GpsRepository(
        gpsService
    )

    @Provides
    @Singleton
    fun provideGpsUseCase(
        gpsRepository: GpsRepository
    ): GpsUseCase = GpsUseCase(gpsRepository)

    @Provides
    @Singleton
    fun provideGpxService(
    ): GpxService = GpxService()

    @Provides
    @Singleton
    fun provideGpxRepository(
        gpxService: GpxService
    ): GpxRepository = GpxRepository(
        gpxService
    )

    @Provides
    @Singleton
    fun provideGpxUseCase(
        gpxRepository: GpxRepository
    ): GpxUseCase = GpxUseCase(gpxRepository)

    companion object {
        val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "preferences")
    }
}
