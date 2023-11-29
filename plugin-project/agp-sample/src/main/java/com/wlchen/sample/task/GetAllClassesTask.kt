package com.wlchen.sample.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @Author cwl
 * @Date 2023/11/27 9:25 PM
 */
abstract class GetAllClassesTask : DefaultTask() {
    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @TaskAction
    fun taskAction() {
        allDirectories.get().forEach { directory ->
            println("Directory: ${directory.asFile.absolutePath}")
            traverseFolder(directory.asFile)
        }
        allJars.get().forEach { file ->
            println("JarFile: ${file.asFile.absolutePath}")
        }
    }

    private fun traverseFolder(folder: File) {
        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        traverseFolder(file) // 递归调用自身
                    } else {
                        // 处理文件
                        println("File: ${file.absolutePath}")
                    }
                }
            }
        }
    }
}


