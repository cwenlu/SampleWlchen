package com.wlchen.sample.task

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * @Author cwl
 * @Date 2023/11/28 3:12 PM
 */
abstract class GetApksTask : DefaultTask() {
    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun taskAction() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
            ?: throw RuntimeException("Cannot load APKs")
        builtArtifacts.elements.forEach {
            println("Got an APK at ${it.outputFile}")
        }
    }
}

fun getApksTask(target: Project, variant: Variant) {
    target.tasks.register("${variant.name}GetApks", GetApksTask::class.java) {
        it.apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
        it.builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
    }
}