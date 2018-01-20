package com.example.rxlambda

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.rxlambda.BaseApplication
import com.wtz.tj.zjz.mis.util.deBug
import com.wtz.tj.zjz.mis.util.runRxLambda
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        load.setOnClickListener {
            loadOrg()
        }
    }

    private fun loadOrg() {
        runRxLambda(BaseApplication.App().getService().loadOrg("1"), {
            toast("success")
            deBug(it.toString())
        }, {
            it?.printStackTrace()
            toast("fail")
        })
    }
}
