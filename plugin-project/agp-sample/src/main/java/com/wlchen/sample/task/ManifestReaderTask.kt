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
 * @Date 2023/12/4 5:27 PM
 */
abstract class ManifestReaderTask : DefaultTask() {
    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    @TaskAction
    fun taskActions() {
        val manifest = mergedManifest.asFile.get().readText()
        // ensure that merged manifest contains the right activity name.
        //注意合并后的文件是格式化的,匹配时需注意。或者直接短字符匹配
        if (manifest.contains(
                """<meta-data
            android:name="name"
            android:value="wlchen" />"""
            )
        ) {
            println("Manifest Placeholder replaced successfully")
        }
    }
}

fun manifestReaderTask(target: Project, variant: Variant) {
    target.tasks.register("${variant.name}ManifestReader", ManifestReaderTask::class.java) {
        it.mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
    }
}