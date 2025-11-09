package com.jdmobile.inflightsalesapp

import android.app.Application
import com.jdmobile.inflightsalesapp.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(uiModule)
        }
    }
}