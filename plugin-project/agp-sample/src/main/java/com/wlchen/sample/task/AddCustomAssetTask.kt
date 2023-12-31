package com.wlchen.sample.task

import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @Author cwl
 * @Date 2023/12/2 2:42 PM
 */
abstract class AddCustomAssetTask : DefaultTask() {
    //官方例子用的 @get:OutputFiles,但是在二次执行的时候会报错
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        outputDirectory.get().asFile.mkdirs()
        File(outputDirectory.get().asFile, "custom_asset.txt").writeText("some real asset file")
    }
}

fun addCustomAsset(target: Project, variant: Variant) {
    val addCustomAssetTask =
        target.tasks.register("${variant.name}AddCustomAsset", AddCustomAssetTask::class.java)
    variant.sources.assets?.addGeneratedSourceDirectory(
        addCustomAssetTask,
        AddCustomAssetTask::outputDirectory
    )
}