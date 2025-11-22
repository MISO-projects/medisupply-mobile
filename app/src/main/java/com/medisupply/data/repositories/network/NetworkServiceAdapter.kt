package com.medisupply.data.repositories.network

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocketFactory

object NetworkServiceAdapter {
    private const val BASE_URL = "https://medisupply.tech/movil/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var httpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    /**
     * TrustManager que acepta todos los certificados (solo para desarrollo)
     * NO usar en producción
     */
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    /**
     * Inicializa el cliente HTTP con el contexto para el interceptor de autenticación
     */
    fun initialize(context: Context) {
        if (httpClient == null) {
            // Configurar SSL para manejar certificados problemáticos
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            httpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))
                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Solo para desarrollo
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()

            apiService = retrofit!!.create(ApiService::class.java)
        }
    }

    /**
     * Obtiene el servicio API (debe llamarse después de initialize)
     */
    fun getApiService(): ApiService {
        return apiService ?: throw IllegalStateException("NetworkServiceAdapter no ha sido inicializado. Llama a initialize(context) primero.")
    }

    /**
     * Obtiene la instancia de Retrofit para crear servicios
     */
    fun getInstance(): Retrofit {
        return retrofit ?: throw IllegalStateException("NetworkServiceAdapter no ha sido inicializado. Llama a initialize(context) primero.")
    }
}
