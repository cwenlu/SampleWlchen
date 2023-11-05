package com.wlchen.sample

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method
import java.io.PrintStream

/**
 * GeneratorAdapter 封装了一些常用指令方便快速调用
 * @Author cwl
 * @Date 2023/10/28 3:31 PM
 */

fun generateSample8Gen() {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    //生成类
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
        "com/wlchen/sample/temp/Sample8Asm",
        null,
        "java/lang/Object",
        null
    )

    val mi = Method.getMethod("void <init> ()")
    //method Generator
    var mg =
        GeneratorAdapter(Opcodes.ACC_PUBLIC, mi, null, null, cw)
    mg.loadThis()
    mg.invokeConstructor(Type.getType(Any::class.java), mi)
    mg.returnValue()
    mg.endMethod()

    val mGen = Method.getMethod("long gen (int,long)")
    mg = GeneratorAdapter(0, mGen, null, null, cw)
    mg.argumentTypes.forEachIndexed { index, type ->
        mg.loadArg(index)
        if (index == 0) {
            mg.visitInsn(Opcodes.I2L)
        }
    }
    mg.visitInsn(Opcodes.LADD)
    mg.returnValue()

    val mDump = Method.getMethod("void dump (String[])")
    mg = GeneratorAdapter(Opcodes.ACC_PROTECTED, mDump, null, null, cw)
    mg.getStatic(Type.getType(System::class.java), "out", Type.getType(PrintStream::class.java))
    mg.push("hello")
    mg.invokeVirtual(Type.getType(PrintStream::class.java),Method.getMethod("void println (String)"))
    mg.returnValue()
    cw.visitEnd()
    val bytes = cw.toByteArray()
    saveClass("Sample8Asm.class", bytes)
}