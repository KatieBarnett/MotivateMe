package dev.motivateme

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class MotivateMeApplication : Application(), Configuration.Provider {

    // This is required for the CoroutineWorker
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    override val workManagerConfiguration = Configuration.Builder()
        .setWorkerFactory(
            EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory()
        )
        .setMinimumLoggingLevel(Log.INFO)
        .build()
}
