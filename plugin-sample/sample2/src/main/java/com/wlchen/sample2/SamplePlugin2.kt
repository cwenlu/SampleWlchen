package com.wlchen.sample2

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SamplePlugin2:Plugin<Project> {
    override fun apply(target: Project) {
        println("====="+target.name)
    }
}