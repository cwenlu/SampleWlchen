package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.options.OptionValues
import javax.inject.Inject

/**
 * [定义task](https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:configuring_tasks)
 * @Author cwl
 * @Date 2023/11/8 1:51 PM
 */

fun defineTask(target: Project) {
    locatingTask(target)
    passingArg(target)
    cmdPassingArg(target)
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

/**
 * [命令行传递参数](https://docs.gradle.org/current/userguide/custom_tasks.html#sec:declaring_and_using_command_line_options)
 */


private fun cmdPassingArg(target: Project) {
    target.tasks.register("verifyUrl", UrlVerify::class.java)
    target.tasks.register("processUrl", UrlProcess::class.java)
}

//必须加open，不能是final的类
open class UrlVerify : DefaultTask() {
    init {
        group = "sample"
    }
    //列出指定任务的帮助信息
    //gradlew -q help --task verifyUrl 执行help任务 利用--task 指定任务

    //gradlew -q verifyUrl --url=http://www.google.com/
    var url: String? = null
        @Option(option = "url", description = "Configures the URL to be verified.") set
        @Input get

    @TaskAction
    fun verify() {
        //org.slf4j.Logger 设置的日志启用的级别 LogLevel.QUIET
        //所以只能使用quiet 和 error
        logger.quiet("启用QUIET级别以上日志" + logger.isEnabled(LogLevel.QUIET).toString())
        logger.quiet("Verifying URL '{}'", url)
    }
}

/**
 * gradlew -q help --task processUrl
 *
 * gradlew -q processUrl --url=http://www.google.com/ --output-type=FILE --http
 */
abstract class UrlProcess : DefaultTask() {
    init {
        group = "sample"
        //这里可以设置默认值
        //http.set(false)
    }

    //文档上说对于boolean的会生成对应的--no-http，这里没成功，可能说本地版本低了
    @get:Optional
    @get:Input
    @get:Option(option = "http", description = "Configures the http protocol to be allowed.")
    abstract val http: Property<Boolean>

    var url: String = ""
        @Option(option = "url", description = "Configures the URL to send the request to.")
        set(value) {
            if (!http.getOrElse(true) && url.startsWith("http://")) {
                throw IllegalArgumentException("HTTP is not allowed")
            } else {
                field = value
            }
        }
        @Input get


    var outputType: OutputType? = null
        @Option(option = "output-type", description = "Configures the output type.") set
        //注意这里的写法，不能写成@get:Input
        @Input get

    @get:OptionValues("output-type")
    val availableOutputTypes = OutputType.values().toList()

    @TaskAction
    fun process() {
        logger.quiet("Writing out the URL response from '{}' to '{}'", url, outputType)
    }

    enum class OutputType {
        CONSOLE, FILE
    }
}
