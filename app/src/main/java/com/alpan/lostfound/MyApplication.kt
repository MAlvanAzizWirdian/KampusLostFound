package com.alpan.lostfound

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = HashMap<String, String>()
        config["cloud_name"] = "dpeowlqys"
        config["api_key"] = "111553395446372"
        config["api_secret"] = "_Qu-uD0yXXSMxhycU9IDPGL2dyI"

        MediaManager.init(this, config)
    }
}
