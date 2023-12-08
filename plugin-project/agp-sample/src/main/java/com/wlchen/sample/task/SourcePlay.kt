package com.wlchen.sample.task

import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @Author cwl
 * @Date 2023/12/8 8:50 PM
 */

abstract class AddCustomSourcesTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val outputFile = File(outputFolder.asFile.get(), "com/foo/bar.toml")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            [clients]
            data = [ ["gamma", "delta"], [1, 2] ]
        """
        )
    }
}

abstract class DisplayAllSourcesTask : DefaultTask() {
    @get:InputFiles
    abstract val sourceFolders: ListProperty<Directory>

    @TaskAction
    fun taskAction() {
        sourceFolders.get().forEach {
            println("---> Got a Directory $it")
            println("<--- done")
        }
    }
}

fun addSourceAndShow(target: Project, variant: Variant) {
    val addSourceTaskProvider =
        target.tasks.register("${variant.name}AddCustomSources", AddCustomSourcesTask::class.java) {
            it.outputFolder.set(File(target.layout.buildDirectory.asFile.get(), "toml2/gen"))
        }
    //访问（并可能创建）可按其名称引用的自定义源类型的新 Flat。
    //第一个调用方将创建新实例，其他具有相同名称的调用方将获得相同的实例返回。
    //任何调用方都可以通过调用 Flat.all 来获取在此自定义源类型下注册的文件夹的最终列表。
    //这些源目录会附加到变体中，并且对 Android Studio 可见
    variant.sources.getByName("toml2").also {
        it.addStaticSourceDirectory("src/${variant.name}/toml2")
        it.addGeneratedSourceDirectory(addSourceTaskProvider, AddCustomSourcesTask::outputFolder)
    }
    println(variant.sources.getByName("toml2"))
    target.tasks.register("${variant.name}DispplayAllsources", DisplayAllSourcesTask::class.java) {
        it.sourceFolders.set(variant.sources.getByName("toml2").all)
    }
}