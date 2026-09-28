package com.gstfkz.parismetrogame.data.network

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object PrimApiClient {

    /**
     * [apiKey] : clé obtenue sur https://prim.iledefrance-mobilites.fr/ (compte gratuit,
     * abonnement à l'API "Navitia" dans l'espace développeur). Voir BuildConfig.PRIM_API_KEY,
     * alimenté depuis local.properties.
     */
    fun create(apiKey: String): PrimApiService {
        val apiKeyInterceptor = Interceptor { chain: Interceptor.Chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", apiKey)
                .build()
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .build()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(PrimApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PrimApiService::class.java)
    }
}
