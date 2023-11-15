package com.wlchen.sample

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * [worker api](https://docs.gradle.org/current/userguide/custom_tasks.html#worker_api)
 * @Author cwl
 * @Date 2023/11/15 6:42 PM
 */

fun workerApiTask(target: Project) {
    target.tasks.register("reverseFiles", ReverseFiles::class.java) {
        it.group = "sample"
        //指定数据源
        it.source = target.fileTree(("inputs"))
        //指定输出
        it.outputDir.set(target.layout.buildDirectory.dir("output-reverse"))
    }
}

// The parameters for a single unit of work
interface ReverseParameters : WorkParameters {
    val fileToReverse: RegularFileProperty
    val destinationDir: DirectoryProperty
}

// The implementation of a single unit of work
//[服务注入](https://docs.gradle.org/current/userguide/custom_gradle_types.html#service_injection)
//不应该实现getParameters()方法。Gradle将在运行时为每个工作单元注入参数对象。
abstract class ReverseFile @Inject constructor(private val fileSystemOperations: FileSystemOperations) :
    WorkAction<ReverseParameters> {
    override fun execute() {
        fileSystemOperations.copy {
            it.from(parameters.fileToReverse)
            it.into(parameters.destinationDir)
            it.filter {
                it.reversed()
            }
        }
    }
}

// The WorkerExecutor will be injected by Gradle at runtime
abstract class ReverseFiles @Inject constructor(private val workerExecutor: WorkerExecutor) :
    SourceTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun reverseFiles() {
        // Create a WorkQueue to submit work items
        val workQueue = workerExecutor.noIsolation()

        // Create and submit a unit of work for each file
        source.forEach { file ->
            workQueue.submit(ReverseFile::class.java) {
                it.fileToReverse.set(file)
                it.destinationDir.set(outputDir)
            }
        }
    }
}
