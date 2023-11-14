package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * https://docs.gradle.org/current/userguide/incremental_build.html#sec:task_inputs_outputs
 * task 的输入和输出
 */

fun inOutTask(target: Project) {
    target.tasks.create("inOutTask", InOutTask::class.java) {
        it.group = "sample"
        it.inputPath.set("must set input value")
        it.outputFile.set(File("output.txt"))
    }

    target.tasks.create("InternalInOutTask", InternalInOutTask::class.java) {
        it.group = "sample"

    }
}


abstract class InOutTask : DefaultTask() {
    //这个不设置会报错
    @get:Input
    abstract val inputPath: Property<String>

    //这个不设置会报错
    @get:OutputFile
    abstract val outputFile: RegularFileProperty


    @get:Optional //表示可选的
    @get:OutputFile
    abstract val optionalOutputFile: Property<File>

    @TaskAction
    fun perform() {
        println("inputPath:${inputPath}")
        outputFile.get().asFile.writeText("out put info")
    }
}

abstract class InternalInOutTask : DefaultTask() {
    //可以不使用注解的方式声明输入输出
    init {
        inputs.files(File("proguard-rules.pro"))
        outputs.files("proguard-rules-cpoy.pro")
        //设置非必须
        outputs.file("proguard-rules-cpoy-no-must.pro").optional()
    }

    @TaskAction
    fun perform() {
        //复制了文件
        //确定只有一个文件的时候可以使用singleFile
        inputs.files.singleFile.copyTo(outputs.files.elements.get().first().asFile, true)
    }
}