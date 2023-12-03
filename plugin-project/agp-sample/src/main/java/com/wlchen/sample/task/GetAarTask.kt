package com.wlchen.sample.task

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

/**
 * @Author cwl
 * @Date 2023/11/29 4:28 PM
 */
abstract class GetAarTask : DefaultTask() {
    @get:InputFile
    abstract val aar: RegularFileProperty

    @TaskAction
    fun taskAction() {
        println("Aar: ${aar.get().asFile.absolutePath}")
    }
}

fun getAarTask(target: Project, variant: Variant) {
    target.tasks.register("${variant.name}GetAar", GetAarTask::class.java) {
        it.aar.set(variant.artifacts.get(SingleArtifact.AAR))
    }
}