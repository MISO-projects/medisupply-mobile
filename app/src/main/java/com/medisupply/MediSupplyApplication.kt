package com.medisupply

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.medisupply.data.repositories.network.NetworkServiceAdapter

class MediSupplyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        NetworkServiceAdapter.initialize(this)
    }
}
