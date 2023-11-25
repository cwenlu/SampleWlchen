package com.wlchen.sample

import android.os.SystemClock

/**
 * [参考](https://juejin.cn/post/7140113885528326181#heading-18)
 * 不用静态的方式，静态的在碰到嵌套事件有问题
 *
 * public class XXXManager {
 *
 *     public static void start(View.OnClickListener click) {
 *         ...
 *         view.setOnClickListener(v -> {
 *            if(DebounceChecker.check()){
 *                return;
 *             }
 *             //do something
 *             if (click != null) {
 *                 click.onClick(v);
 *             }
 *             //do something
 *         });
 *     }
 * }
 *
 * public class XXXActivity{
 *
 *     private void test(){
 *         XXXManager.start(new View.OnClickListener() {
 *             @Override
 *             public void onClick(View v) {
 *                 if(DebounceChecker.check()){
 *                     return;
 *                 }
 *                 //do something
 *             }
 *         });
 *     }
 * }
 *
 *
 *
 * @Author cwl
 * @Date 2023/11/24 11:24 AM
 */
class DebounceChecker {
    private var lastClickTime = 0L
    private val interval = 500L

    /**
     * return true 则表示不能点
     */
    fun debounce(): Boolean {
        val isDebounce = SystemClock.elapsedRealtime() - lastClickTime < interval
        if (!isDebounce) {
            lastClickTime = SystemClock.elapsedRealtime()
        }
        return isDebounce
    }
}