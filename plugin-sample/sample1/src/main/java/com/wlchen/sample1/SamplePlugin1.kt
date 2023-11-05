package com.wlchen.sample1

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SamplePlugin1:Plugin<Project> {
    override fun apply(target: Project) {
        println("====="+target.name)
    }
}