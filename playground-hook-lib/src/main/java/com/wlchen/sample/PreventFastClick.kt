package com.wlchen.sample

import android.util.Log
import android.view.View
import android.widget.EditText
import com.wlchen.sample.hook.lib.R
import java.lang.ref.WeakReference

/**
 * 防止view多次点击
 */
object PreventFastClick {

    const val VIEW_TIME = 500L

    //注意需要加这个注解
    @JvmStatic
    fun onClick(view: View) {
        if (isIgnore(view)) return
        Log.d("cwl", "enable false")
        view.isClickable = false
        view.postDelayed(ViewDelayRunnable(view), VIEW_TIME)
    }

    /**
     * 当前视图忽略防止多次点击
     */
    fun ignore(view: View, desc: String) {
        view.setTag(R.id.ignore_prevent_fast_click, desc)
    }

    /**
     * 当前视图是否忽略防止多次点击
     */
    internal fun isIgnore(view: View): Boolean {
        if (view is EditText) return true
        return view.getTag(R.id.ignore_prevent_fast_click) != null
    }
}


class ViewDelayRunnable(view: View) : Runnable {

    private val viewWeakReference = WeakReference(view)

    override fun run() {
        val view = viewWeakReference.get()
        if (view != null) {
            Log.d("PointerEventDelegate", "enable true")
            view.isClickable = true
        }
    }

}