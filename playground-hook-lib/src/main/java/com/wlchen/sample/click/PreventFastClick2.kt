package com.wlchen.sample.click

import android.os.SystemClock
import android.util.Log
import android.view.View
import com.wlchen.sample.hook.lib.R

/**
 * 防止view多次点击
 * 这个利用时间，不使用handler
 */
object PreventFastClick2 {
    private const val VIEW_TIME = 500L

    //注意需要加这个注解
    /**
     * 快速点击返回true
     */
    @JvmStatic
    fun isFastClick(view: View): Boolean {
        val key = R.id.last_click_time
        val currentTime = SystemClock.elapsedRealtime()
        val lastTime = view.getTag(key) as? Long
        if (lastTime == null) {
            view.setTag(key, currentTime)
            return false

        }
        if (currentTime - lastTime < VIEW_TIME) {
            Log.d("cwl","快速点击")
            return true
        } else {
            view.setTag(key, currentTime)
            return false
        }

    }


}


