package com.rxlambda

import android.app.Application
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Administrator on 2018/1/20.
 */
class BaseApplication : Application() {
    private lateinit var retrofitService: RetrofitService
    companion object {
        private lateinit var instance: BaseApplication
        fun App(): BaseApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val build = OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15,TimeUnit.SECONDS)
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
            Log.w("retrofit url",it)
        })
        logging.level = HttpLoggingInterceptor.Level.BODY

        build.addNetworkInterceptor(logging)

        retrofitService = Retrofit.Builder()
                .client(build.build())
                .baseUrl("your base url")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(RetrofitService::class.java)
    }

    fun getService():RetrofitService{
        return retrofitService
    }
}