package com.wlchen.sample

import org.gradle.api.Plugin
import org.gradle.api.Project


abstract class SamplePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        useFileTask(target)
        extensionTask(target)

        defineTask(target)

    }
}
