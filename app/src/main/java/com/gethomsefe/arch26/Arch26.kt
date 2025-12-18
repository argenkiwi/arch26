package com.gethomsefe.arch26

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Arch26 : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Arch26)

            val module = module {
                factory { EditModel.Presenter() }
            }

            modules(module)
        }
    }
}
