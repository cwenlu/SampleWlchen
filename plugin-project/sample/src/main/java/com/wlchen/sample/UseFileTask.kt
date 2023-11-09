package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * [插件或者任务中使用文件](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:working_with_files_in_custom_tasks_and_plugins)
 * @Author cwl
 * @Date 2023/11/8 9:12 AM
 */

fun useFileTask(target: Project) {
    val fileProperty = target.objects.fileProperty()
    fileProperty.set(target.layout.buildDirectory.file("WriteInfo.txt"))

    target.tasks.register("writeInfo", WriteInfoToFileTask::class.java) {
        it.group = "sample"

        it.destination.set(fileProperty)

    }

    target.tasks.register("readInfo") {
        it.group = "sample"

        it.dependsOn("writeInfo")
        it.doLast {
            val file = fileProperty.get().asFile
            println("${file.readText()} (file: ${file.name})")
        }
    }
}

/**
 * 写内容到文件
 */
abstract class WriteInfoToFileTask : DefaultTask() {
    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun writeInfo() {
        val file = destination.get().asFile
        file.parentFile.mkdirs()
        file.writeText("write info")
    }
}

