package com.medisupply.data.session

import android.content.Context
import android.content.SharedPreferences


class SessionManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("MediSupplySession", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_ID_CLIENT = "id_client"
        private const val KEY_ID_SELLER = "id_seller"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    /**
     * Guarda la información de la sesión después del login
     */
    fun saveSession(
        accessToken: String,
        tokenType: String,
        userId: String,
        email: String,
        name: String?,
        role: String?,
        idClient: String? = null,
        idSeller: String? = null
    ) {
        with(sharedPreferences.edit()) {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_TOKEN_TYPE, tokenType)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name ?: "Usuario")
            putString(KEY_USER_ROLE, role)
            putString(KEY_ID_CLIENT, idClient)
            putString(KEY_ID_SELLER, idSeller)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Obtiene el token de autorización completo (Bearer token)
     */
    fun getAuthorizationToken(): String? {
        val tokenType = sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer")
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        return if (accessToken != null) "$tokenType $accessToken" else null
    }
    
    /**
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAuthorizationToken() != null
    }
    
    /**
     * Obtiene el rol del usuario
     */
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    
    /**
     * Obtiene el nombre del usuario
     */
    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }
    
    /**
     * Obtiene el email del usuario
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }
    
    /**
     * Obtiene el ID del cliente
     */
    fun getIdClient(): String? {
        return sharedPreferences.getString(KEY_ID_CLIENT, null)
    }
    
    /**
     * Obtiene el ID del vendedor
     */
    fun getIdSeller(): String? {
        return sharedPreferences.getString(KEY_ID_SELLER, null)
    }
    
    /**
     * Cierra la sesión del usuario
     */
    fun logout() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}
