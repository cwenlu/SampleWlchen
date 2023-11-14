package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileType
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges

/**
 *
 * [增量构建](https://docs.gradle.org/current/userguide/incremental_build.html#incremental_build)
 * [本例](https://docs.gradle.org/current/userguide/custom_tasks.html#incremental_tasks)
 *
 * Gradle测试自上次构建以来任何任务输入或输出是否发生了变化。
 * 如果他们还没有，Gradle可以考虑最新的任务，因此跳过执行其操作。
 * 另请注意，除非任务至少有一个任务输出，否则增量构建将不起作用
 * 您需要告诉Gradle哪些任务属性是输入，哪些是输出。
 * 如果任务属性影响输出，请务必将其注册为输入，否则任务将被视为最新的。
 * 相反，如果属性不影响输出，请不要将属性注册为输入，否则任务可能会在不需要时执行。
 * 还要小心可能为完全相同的输入产生不同输出的非确定性任务：这些任务不应配置为增量构建，因为最新的检查不起作用
 *
 * @Author cwl
 * @Date 2023/11/10 1:39 PM
 */

fun incrementTask(target: Project) {
    //创建原始的输入数据
    target.task("originalInputs") {
        it.group = "sample"
        val inputsDir = target.layout.projectDirectory.dir("inputs")
        it.outputs.dir(inputsDir)
        it.doLast {
            inputsDir.file("1.txt").asFile.writeText("Content for file 1.")
            inputsDir.file("2.txt").asFile.writeText("Content for file 2.")
            inputsDir.file("3.txt").asFile.writeText("Content for file 3.")
        }
    }

    //更新输入数据
    target.task("updateInputs") {
        it.group = "sample"
        val inputsDir = target.layout.projectDirectory.dir("inputs")
        it.outputs.dir(inputsDir)
        it.doLast {
            inputsDir.file("1.txt").asFile.writeText("Changed content for existing file 1.")
            inputsDir.file("4.txt").asFile.writeText("Content for new file 4.")
        }
    }

    //删除一个输入数据
    target.tasks.register("removeInput", Delete::class.java) {
        it.group = "sample"
        it.delete("inputs/3.txt")
    }

    //删除一个输出数据
    target.tasks.register("removeOutput", Delete::class.java) {
        it.group = "sample"
        it.delete(target.layout.buildDirectory.file("outputs/1.txt"))
    }

    //增量反转任务
    target.tasks.register("incrementalReverse", IncrementalReverseTask::class.java) {
        it.group = "sample"
        it.inputDir.set(target.file("inputs"))
        it.outputDir.set(target.layout.buildDirectory.dir("outputs"))
        it.inputProperty.set(target.properties["taskInputProperty"] as String? ?: "original")
    }

    target.tasks.named("incrementalReverse") {
        it.mustRunAfter("originalInputs", "updateInputs", "removeInput", "removeOutput")
    }
}

abstract class IncrementalReverseTask : DefaultTask() {
    @get:Incremental
    //与任何输入文件属性一起使用，告诉Gradle只考虑文件路径的给定部分很重要。
    //例如，如果一个属性带有@PathSensitive(PathSensitivity.NAME_ONLY)注释，那么在不更改其内容的情况下移动文件不会使任务过时
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val inputProperty: Property<String>

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        println(if (inputChanges.isIncremental) "增量执行" else "非增量执行")

        inputChanges.getFileChanges(inputDir).forEach {
            if (it.fileType == FileType.DIRECTORY) return@forEach
            println("${it.changeType}: ${it.normalizedPath}")
            val targetFile = outputDir.file(it.normalizedPath).get().asFile
            if (it.changeType == ChangeType.REMOVED) {
                targetFile.delete()
            } else {
                targetFile.writeText(it.file.readText().reversed())
            }
        }
    }
}