package com.gethomsefe.arch26

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Arch26 : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Arch26)
            modules(EditModel.module)
        }
    }
}
