package com.rxlambda

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Administrator on 2018/1/20.
 * 自定义请求接口的方法
 */
interface RetrofitService {

    @GET("url")
    fun loadOrg(
            @Query("id") tel:String
    ):Observable<Org>
}