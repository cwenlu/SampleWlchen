package com.wlchen.sample

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.wlchen.sample.task.GetAarTask
import com.wlchen.sample.task.GetAllClassesTask
import com.wlchen.sample.task.GetApksTask
import org.gradle.api.Plugin
import org.gradle.api.Project


abstract class AgpSamplePlugin : Plugin<Project> {
    override fun apply(target: Project) {

        target.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponents.onVariants {
                getAllClassesTask(target, it)
                getApksTask(target, it)
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

    //LibraryPlugin 好像不能获取到数据
    private fun getAllClassesTask(target: Project, variant: Variant) {
        val taskProvider =
            target.tasks.register(variant.name + "GetAllClasses", GetAllClassesTask::class.java)
        variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
            .use(taskProvider)
            .toGet(
                ScopedArtifact.CLASSES,
                { it.allJars },
                { it.allDirectories }
            )
    }

    private fun getApksTask(target: Project, variant: Variant) {
        target.tasks.register("${variant.name}GetApks", GetApksTask::class.java) {
            it.apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
            it.builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
        }
    }

    private fun getAarTask(target: Project, variant: Variant) {
        target.tasks.register("${variant.name}GetAar", GetAarTask::class.java) {
            it.aar.set(variant.artifacts.get(SingleArtifact.AAR))
        }
    }
}

