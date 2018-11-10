package com.heads.thinking.headhelper

import android.app.Application
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: Context? = null
            private set
    }
}