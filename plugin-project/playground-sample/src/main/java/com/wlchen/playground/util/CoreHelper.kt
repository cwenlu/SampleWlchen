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

/**
 * 判断是否是桥接方法
 */
fun isBridge(access: Int) = access and Opcodes.ACC_BRIDGE != 0

/**
 * 判断是否是合成方法
 */
fun isSynthetic(access: Int) = access and Opcodes.ACC_SYNTHETIC != 0

/**
 * 是否需要访问方法
 *
 * 不对抽象方法、native方法、桥接方法、合成方法进行织入
 */
fun isNeedVisit(access: Int): Boolean =
    !(isAbstract(access) || isNative(access) || isBridge(access) || isSynthetic(access))

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
