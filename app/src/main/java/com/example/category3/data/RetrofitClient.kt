package com.example.category3.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    // 🚨 IMPORTANT:
    // If using Android Emulator, localhost is 10.0.2.2
    // If using a physical phone, put your computer's local IP address (e.g., 192.168.1.X)
    private const val BASE_URL = "https://10.0.2.2:4000/" // Change port to match your backend

    val api: DigitalTwinApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient()) // Bypasses SSL errors for local development
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DigitalTwinApi::class.java)
    }

    // ============================================================================
    // ⚠️ DEVELOPMENT ONLY: Trust all SSL Certificates (Fixes Chain Validation Error)
    // ============================================================================
    fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Trust all hostnames
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}