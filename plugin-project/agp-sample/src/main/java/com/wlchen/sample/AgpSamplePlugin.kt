package com.wlchen.sample

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wlchen.sample.task.ProvideCustomFieldValueTask
import com.wlchen.sample.task.addCustomAsset
import com.wlchen.sample.task.androidComponentsConfig
import com.wlchen.sample.task.getAarTask
import com.wlchen.sample.task.getAllClassesTask
import com.wlchen.sample.task.getApksTask
import com.wlchen.sample.task.manifestReaderTask
import com.wlchen.sample.task.provideCustomFieldValueTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named


abstract class AgpSamplePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        provideCustomFieldValueTask(target)

        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponentsConfig(androidComponents)
            androidComponents.onVariants {
                getAllClassesTask(target, it)
                getApksTask(target, it)
                addCustomAsset(target, it)
                addCustomFieldWithValueFromTask(target,it)
                manifestReaderTask(target,it)
            }
        }

        target.plugins.withType(LibraryPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants(androidComponents.selector().withBuildType("debug")) {
                getAarTask(target, it)
            }
        }
    }

    private fun addCustomFieldWithValueFromTask(target: Project, variant: Variant) {
        //可以在这直接定义task的，这里我们在外部定义，此处获取
        //target.tasks.named("provideCustomFieldValueTask",ProvideCustomFieldValueTask::class.java)
        val gitVersionProvider = target.tasks.named<ProvideCustomFieldValueTask>("provideCustomFieldValueTask")
        variant.buildConfigFields.put("gitVersion", gitVersionProvider.map {
            BuildConfigField("String","\"${it.gitVersionOutputFile.get().asFile.readText()}\"","Git Version")
        })
    }

}

