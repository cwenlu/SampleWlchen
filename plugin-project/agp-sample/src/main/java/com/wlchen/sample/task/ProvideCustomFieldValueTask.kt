package com.wlchen.sample.task

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * @Author cwl
 * @Date 2023/12/3 7:37 PM
 */
abstract class ProvideCustomFieldValueTask : DefaultTask() {
    @get:OutputFile
    abstract val gitVersionOutputFile: RegularFileProperty

    @TaskAction
    fun taskAction() {
        // this would be the code to get the tip of tree version,
        // val firstProcess = ProcessBuilder("git","rev-parse --short HEAD").start()
        // val error = firstProcess.errorStream.readBytes().decodeToString()
        // if (error.isNotBlank()) {
        //      System.err.println("Git error : $error")
        // }
        // var gitVersion = firstProcess.inputStream.readBytes().decodeToString()

        // but here, we are just hardcoding :
        gitVersionOutputFile.get().asFile.writeText("123")
    }
}

fun provideCustomFieldValueTask(target: Project) {
    target.tasks.register("provideCustomFieldValueTask", ProvideCustomFieldValueTask::class.java) {
        File(target.buildDir, "output/gitVersion").also { file ->
            //由于我们最后一个文件没指定后缀,如果用file.mkdirs()，则会创建成目录
            //所以这里我们找到父级进行创建
            file.parentFile.mkdirs()
            it.gitVersionOutputFile.set(file)
        }
        //设置不可重用输出
        it.outputs.upToDateWhen { false }
    }
}
