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