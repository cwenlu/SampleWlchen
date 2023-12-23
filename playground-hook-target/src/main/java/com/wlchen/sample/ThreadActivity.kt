package com.wlchen.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @Author cwl
 * @Date 2023/11/18 9:42 PM
 */
class ThreadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val runnable = Runnable {
            println("thread name:" + Thread.currentThread().name)
        }
        val threadGroup = ThreadGroup("group")
        Thread().start()
        Thread("thread test").start()
        Thread(runnable).start()
        Thread(runnable, "thread test").start()
        Thread(threadGroup, "thread test").start()
        Thread(threadGroup,runnable, "thread test").start()
        Thread(threadGroup,runnable, "thread test",0).start()


        CustomThread().start()
    }

}

open class CustomThread : Thread()

class SubCustomThread : CustomThread()