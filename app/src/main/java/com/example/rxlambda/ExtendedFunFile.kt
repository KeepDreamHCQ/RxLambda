package com.wtz.tj.zjz.mis.util

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * 用于添加扩展方法
 */
fun Any.deBug(msg: String) = Log.e("${this.javaClass.simpleName}------->", msg)

fun <T> runRxLambda(observable: Observable<T>, next: (T) -> Unit, error: (e: Throwable?) -> Unit, completed: () -> Unit = { Log.e("completed", "completed") }) {
    observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<T> {
                override fun onComplete() { completed() }

                override fun onError(e: Throwable?) { error(e) }

                override fun onNext(value: T) { next(value) }

                override fun onSubscribe(d: Disposable?) {}
            })
}