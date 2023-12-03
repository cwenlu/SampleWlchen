package com.wlchen.sample

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wlchen.sample.task.addCustomAsset
import com.wlchen.sample.task.androidComponentsConfig
import com.wlchen.sample.task.getAarTask
import com.wlchen.sample.task.getAllClassesTask
import com.wlchen.sample.task.getApksTask
import org.gradle.api.Plugin
import org.gradle.api.Project


abstract class AgpSamplePlugin : Plugin<Project> {
    override fun apply(target: Project) {

        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponentsConfig(androidComponents)
            androidComponents.onVariants {
                getAllClassesTask(target, it)
                getApksTask(target, it)
                addCustomAsset(target, it)
            }
        }

        target.plugins.withType(LibraryPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants {
                getAarTask(target, it)
            }
        }
    }


}

