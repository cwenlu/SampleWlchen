package com.wlchen.sample

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * @Author cwl
 * @Date 2023/11/18 9:42 PM
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //模拟lambda
        findViewById<Button>(R.id.btn).setOnClickListener {
            println("btn")
        }
        //模拟匿名类
        findViewById<Button>(R.id.btn2).setOnClickListener(object : OnClickListener {
            override fun onClick(p0: View?) {
                println("btn2")
            }

        })
        //模拟继承
        findViewById<Button>(R.id.btn3).setOnClickListener(Btn3ClickListener())
        //模拟方法引用
        findViewById<Button>(R.id.btn4).setOnClickListener(this::onClickDefine)
        //这个逐类方式处理不到这种
        findViewById<Button>(R.id.btn5).setOnClickListener(ClickDefine::onClickDefine)
    }

    fun onClickDefine(p0: View?) {
        println("btn4")
    }

}

object ClickDefine{
    fun onClickDefine(p0: View?) {
        println("btn4")
    }
}

interface CustomClickListener : OnClickListener {
    override fun onClick(p0: View?) {
        println("CustomClickListener")
    }
}

class Btn3ClickListener : CustomClickListener {
    override fun onClick(p0: View?) {
        super.onClick(p0)
        println("Btn3ClickListener")
    }
}