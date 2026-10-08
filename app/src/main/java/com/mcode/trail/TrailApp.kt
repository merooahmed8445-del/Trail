package com.mcode.trail

import android.app.Application
import com.mcode.trail.data.local.TrailDatabase

class TrailApp : Application() {

    val database by lazy { TrailDatabase.getDatabase(this) }
}