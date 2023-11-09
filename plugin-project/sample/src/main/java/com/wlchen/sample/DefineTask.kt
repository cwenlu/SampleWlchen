package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.provideDelegate
import javax.inject.Inject

/**
 * 定义task[https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:configuring_tasks]
 * @Author cwl
 * @Date 2023/11/8 1:51 PM
 */

fun defineTask(target: Project) {
    locatingTask(target)
    passingArg(target)
}

/**
 * 定位task
 */
private fun locatingTask(target: Project) {
    println(target.tasks.getByPath("dumpExtension2").name)
    println(target.tasks.named("dumpExtension").get().name)
    println(
        target.tasks.named("writeInfo", WriteInfoToFileTask::class.java)
            .get().destination.get().asFile.path
    )
    target.tasks.withType(WriteInfoToFileTask::class.java).configureEach {
        println(it.destination.get().asFile.absolutePath)
    }

}


/**
 * 给任务传输参数
 */
private fun passingArg(target: Project) {
    target.tasks.register("passingArg", PassingArgTask::class.java, "msg", 1).configure {
        it.group = "sample"
        it.description = "task introduce"
        it.doLast { _ ->
            it.dump()
        }
    }
}

abstract class PassingArgTask @Inject constructor(
    private val message: String,
    private val number: Int
) : DefaultTask() {
    fun dump() {
        println("message: ${message}, number: ${number}")
    }
}
