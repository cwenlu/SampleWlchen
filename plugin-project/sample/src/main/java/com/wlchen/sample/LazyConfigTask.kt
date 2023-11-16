package com.wlchen.sample

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * [延迟属性配置](https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_configuration)
 *
 * @Author cwl
 * @Date 2023/11/16 12:47 AM
 */

fun lazyConfigTask(target: Project) {
    producerConsumer(target)
    producerConsumerList(target)
    useMapProperty(target)
}

private fun producerConsumer(target: Project) {
    val producer = target.tasks.register("producer", Producer::class.java)
    val consumer = target.tasks.register("consumer", Consumer::class.java)

    consumer.configure {
        it.group = "sample"
        // Connect the producer task output to the consumer task input
        // Don't need to add a task dependency to the consumer task. This is automatically added
        it.inputFile.set(producer.flatMap { it.outputFile })
    }
    producer.configure {
        it.group = "sample"
        // Set values for the producer lazily
        // Don't need to update the consumer.inputFile property. This is automatically updated as producer.outputFile changes
        it.outputFile.set(target.layout.buildDirectory.file("file.txt"))
    }
}

private fun producerConsumerList(target: Project) {
    val producerOne = target.tasks.register("producerOne", Producer::class.java) {
        it.outputFile.set(target.layout.buildDirectory.file("one.txt"))
    }
    val producerTwo = target.tasks.register("producerTwo", Producer::class.java) {
        it.outputFile.set(target.layout.buildDirectory.file("two.txt"))
    }
    target.tasks.register("consumerList", ConsumerList::class.java) {
        it.inputFiles.add(producerOne.get().outputFile)
        it.inputFiles.add(producerTwo.get().outputFile)
    }
}

private fun useMapProperty(target: Project) {
    var b = 0
    var c = 0
    target.tasks.register("useMapProperty", UseMapProperty::class.java) {
        it.group = "sample"
        it.properties.put("a", 1)
        // Values have not been configured yet
        it.properties.put("b", target.providers.provider { b })
        it.properties.putAll(target.providers.provider { mapOf("c" to c, "d" to c + 1) })
    }

    // Configure the values. There is no need to reconfigure the task
    b = 2
    c = 3
}

abstract class Producer : DefaultTask() {

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun produce() {
        val message = "Hello, World!"
        val output = outputFile.get().asFile
        output.writeText(message)
        logger.quiet("Wrote '${message}' to ${output}")
    }
}

abstract class Consumer : DefaultTask() {

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    //附加到任务属性，以指示在进行最新检查时不考虑该属性
    @Internal
    val fileName: Provider<String> = inputFile.map { it.asFile.name }

    @TaskAction
    fun consume() {
        val input = inputFile.get().asFile
        val message = input.readText()
        logger.quiet("Read '${message}' from ${input}")
    }
}

abstract class ConsumerList : DefaultTask() {

    @get:InputFiles
    abstract val inputFiles: ListProperty<RegularFile>

    @TaskAction
    fun consume() {
        inputFiles.get().forEach {
            val input = it.asFile
            val message = input.readText()
            logger.quiet("Read '${message}' from ${input}")
        }
    }
}

abstract class UseMapProperty : DefaultTask() {

    @get:Input
    abstract val properties: MapProperty<String, Int>

    @TaskAction
    fun dump() {
        properties.get().forEach { (key, value) ->
            logger.quiet("${key} = ${value}")
        }
    }
}