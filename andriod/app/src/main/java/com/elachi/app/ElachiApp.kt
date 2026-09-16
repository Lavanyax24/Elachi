package com.elachi.app

import android.app.Application

/**
 * Application entry point. Right now it just initialises — repositories,
 * Firebase, and Room get wired in as each layer is added in later phases.
 */
class ElachiApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}