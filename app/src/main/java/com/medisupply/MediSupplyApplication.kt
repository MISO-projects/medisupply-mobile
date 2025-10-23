package com.medisupply

import android.app.Application
import com.medisupply.data.repositories.network.NetworkServiceAdapter

class MediSupplyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        NetworkServiceAdapter.initialize(this)
    }
}
