package com.wlchen.playground.util

import java.io.File
import java.net.URLClassLoader
import java.util.Arrays

/**
 * @Author cwl
 * @Date 2023/11/25 1:47 PM
 */
object ClassLoaderUtil {
    fun getClassLoader(
        compileClasspath: List<File>,
        bootClasspath: List<File>
    ): URLClassLoader {
        println(Arrays.toString(compileClasspath.toTypedArray())+"=====")
        println(Arrays.toString(bootClasspath.toTypedArray())+"=====")
        val bootClassLoader =
            URLClassLoader(bootClasspath.map { it.toURI().toURL() }.toTypedArray(), null)
        return URLClassLoader(
            compileClasspath.map { it.toURI().toURL() }.toTypedArray(),
            bootClassLoader
        )
    }
}