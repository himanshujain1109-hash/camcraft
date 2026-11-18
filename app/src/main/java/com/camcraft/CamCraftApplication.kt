package com.camcraft

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.camcraft.settings.SettingsManager
import com.camcraft.storage.PhotoRepository

class CamCraftApplication : Application(), ImageLoaderFactory {

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var photoRepository: PhotoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsManager = SettingsManager(this)
        photoRepository = PhotoRepository(this, settingsManager)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: CamCraftApplication
            private set
    }
}
