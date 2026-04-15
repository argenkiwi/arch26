package com.gethomsefe.arch26

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.plugin.module.dsl.startKoin

@KoinApplication
class Arch26 : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<KoinApplication> {
            androidContext(this@Arch26)
        }
    }

}

@Module
@ComponentScan
object Arch26Module
