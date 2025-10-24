package com.medisupply.data.repositories.network

import android.content.Context
import com.medisupply.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val sessionManager = SessionManager(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val authToken = sessionManager.getAuthorizationToken()
        
        val newRequest = if (authToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", authToken)
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
