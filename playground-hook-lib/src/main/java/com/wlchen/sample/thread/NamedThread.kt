package com.wlchen.sample.thread

import java.util.concurrent.atomic.AtomicInteger

/**
 * 通过替换掉new Thread 达到命名线程的目的。但是没法处理线程的继承
 * @Author cwl
 * @Date 2023/12/11 11:32 AM
 */
class NamedThread(
    group: ThreadGroup?,
    runnable: Runnable?,
    name: String?,
    className: String,
    stackSize: Long
) :
    Thread(group, runnable, generateThreadName(name, className), stackSize) {
    private companion object {
        private val threadId = AtomicInteger(0)
        private fun generateThreadName(name: String?, className: String): String {
            return className + "-" + threadId.getAndIncrement() + if (name.isNullOrBlank()) "" else "-$name"
        }
    }

    constructor(group: ThreadGroup?, runnable: Runnable?, name: String?, className: String) : this(
        group,
        runnable,
        name,
        className,
        0
    )

    constructor(group: ThreadGroup?, runnable: Runnable?, className: String) : this(
        group,
        runnable,
        null,
        className,
        0
    )

    constructor(group: ThreadGroup?, name: String, className: String) : this(
        group,
        null,
        name,
        className,
        0
    )

    constructor(runnable: Runnable?, name: String, className: String) : this(
        null,
        runnable,
        name,
        className,
        0
    )

    constructor(runnable: Runnable?, className: String) : this(null, runnable, null, className, 0)


    constructor(name: String, className: String) : this(null, null, name, className, 0)

    constructor(className: String):this(null, null, null, className, 0)
}

//使用这个构造线程的应该很少
//Thread(ThreadGroup group, Runnable target, String name,
//long stackSize, boolean inheritThreadLocals)