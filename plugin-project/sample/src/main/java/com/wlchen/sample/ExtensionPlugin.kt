package com.wlchen.sample

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create

/**
 * 填加创建扩展属性
 * [doc](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:getting_input_from_the_build)
 */
abstract class ExtensionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<Extension>("extension")
        //设置默认值
        extension.lazyMessage.convention("lazyMessage default")
        target.tasks.register("dumpExtension") {
            it.group = "sample"
            it.doLast {
                println("message:${extension.message}, lazyMessage:${extension.lazyMessage.orNull}")
            }
        }

        val extension2 = target.extensions.create<Extension2>("extension2")
        target.task("dumpExtension2") {
            it.group = "sample"
            it.doLast {
                println("lazyMessage:${extension2.lazyMessage.orNull}")
            }
        }
    }
}

interface Extension {
    var message: String?

    //注意必须声明为val
    //这种方式声明的获取的时候如果使用get(),则必须进行配置不然会报错
    //使用orNull,orElse则不会
    val lazyMessage: Property<String?>
}

abstract class Extension2 {
    abstract val lazyMessage: Property<String>

    init {
        lazyMessage.convention("lazyMessage default")
    }
}