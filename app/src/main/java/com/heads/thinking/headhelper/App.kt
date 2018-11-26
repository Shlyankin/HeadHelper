package com.heads.thinking.headhelper

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object { //возможна утечка памяти
        var instance: Context? = null
            private set
    }
}