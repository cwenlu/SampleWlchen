package com.wlchen.playground

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wlchen.playground.click.ViewClickClassVisitorFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * build/intermediates/classes 可以查看转换后的类
 */
abstract class PlaygroundPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            //androidComponents.sdkComponents.bootClasspath
            androidComponents.onVariants {
                handleViewClick(it)
                it.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
            }
        }
    }

    private fun handleViewClick(variant: Variant) {
        variant.instrumentation.transformClassesWith(
            ViewClickClassVisitorFactory::class.java,
            InstrumentationScope.ALL
        ){}
    }


    private fun check(target: Project) {
        //这2种方式的判断必须要先注册对应插件再注册自己的才行,换了顺序就不行
        val hasAppPlugin = target.plugins.hasPlugin(AppPlugin::class.java)
        val hasLibPlugin = target.plugins.hasPlugin(LibraryPlugin::class.java)

    }
}


//AppPlugin
//与com.android.application一起应用的插件,可判断是否是android app

//LibraryPlugin
//与com.android.library一起应用的插件,可判断是否是android lib

//AndroidComponentsExtension
//Android Gradle 插件相关组件的通用扩展。 每个组件都有一个类型，如应用程序或库，并且将具有与特定组件类型相关的方法的专用扩展

//ApplicationAndroidComponentsExtension
//Android Application Gradle Plugin 组件的扩展。这是应用 com.android.application 插件时的 androidComponents 块
//只有Android Gradle插件才能在com.android.build.api.variant中创建接口实例。

//LibraryAndroidComponentsExtension
//Android Library Gradle 插件组件的扩展。这是应用 com.android.library 插件时的 androidComponents 块
//只有Android Gradle插件才能在com.android.build.api.variant中创建接口实例

//project.plugins.withType(AppPlugin::class.java) {
//    val extension = project.extensions.getByName("androidComponents") as ApplicationAndroidComponentsExtension
//}
//
//project.plugins.withType(LibraryPlugin::class.java) {
//    val extension = project.extensions.getByName("androidComponents") as LibraryAndroidComponentsExtension
//}

