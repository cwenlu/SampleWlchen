package com.wlchen.playground.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes

/**
 * @Author cwl
 * @Date 2023/11/24 10:39 AM
 */

fun isInterface(access: Int) = access and Opcodes.ACC_INTERFACE != 0
fun isAbstract(access: Int) = access and Opcodes.ACC_ABSTRACT != 0
fun isStatic(access: Int) = access and Opcodes.ACC_STATIC != 0
fun isNative(access: Int) = access and Opcodes.ACC_NATIVE != 0

fun isInit(name: String) = name == "<init>"

fun isCinit(name: String) = name == "<cinit>"

fun isObject(className: String) = className == "java/lang/Object"

fun className2InternalName(className: String) = className.replace('/', '.')

fun internalName2className(internalName: String) = internalName.replace('/', '.')
fun ClassReader(classLoader: ClassLoader, className: String): ClassReader? {
    return classLoader.getResourceAsStream("${className}.class")?.use {
        ClassReader(it)
    }
}
