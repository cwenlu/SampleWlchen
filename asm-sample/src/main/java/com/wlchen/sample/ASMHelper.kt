package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceClassVisitor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter

/**
 * @Author cwl
 * @Date 2023/10/28 3:12 PM
 */
fun printAsm(className: String, asmCode: Boolean = true) {
    val printer = if (asmCode) ASMifier() else Textifier()
    val traceClassVisitor = TraceClassVisitor(null, printer, PrintWriter(System.out, true))
    val parseOptions = ClassReader.SKIP_DEBUG
    ClassReader(className).accept(traceClassVisitor, parseOptions)
}

fun saveClass(className: String, bytes: ByteArray) {
    val path = "asm-sample/build/classes/java/main/com/wlchen/sample/temp/${className}"
    FileOutputStream(path).use { it.write(bytes) }
}

fun saveTransClass(className: String, bytes: ByteArray) {
    val file = File("asm-sample/build/classes/java/main/com/wlchen/sample/temp/trans")
    if (!file.exists()) {
        file.mkdirs()
    }
    FileOutputStream(File(file, className)).use { it.write(bytes) }
}

fun readClass(className: String): ByteArray {
    val path = "asm-sample/build/classes/java/main/com/wlchen/sample/temp/${className}"
    return FileInputStream(path).use { it.readBytes() }
}

fun execMethod(className: String, methodName: String) {
    val clazz = Class.forName("com.wlchen.sample.temp.trans.${className}")
    val ins = clazz.getDeclaredConstructor().newInstance()
    val method = clazz.getDeclaredMethod(methodName)
    method.invoke(ins)
}