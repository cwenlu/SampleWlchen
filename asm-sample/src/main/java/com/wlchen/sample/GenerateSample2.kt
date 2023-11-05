package com.wlchen.sample

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * 用asm生成 [com.wlchen.sample.temp.Sample2]
 * @Author cwl
 * @Date 2023/10/28 3:31 PM
 */

fun generateSample2() {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    //生成类
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
        "com/wlchen/sample/temp/Sample2Asm",
        null,
        "java/lang/Object",
        null
    )

    //生成字段
    val fv = cw.visitField(0, "stu", "Ljava/lang/String;", null, null)
    //生成注解
    val av = fv.visitAnnotation("Lcom/wlchen/sample/temp/Tag;", false)
    av.visit("name", "cwl")
    av.visit("age", 22)
    av.visitEnd()
    fv.visitEnd()

    //生成方法
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(1, 1)
    mv.visitEnd()


    cw.visitEnd()
    val bytes = cw.toByteArray()
    saveClass("Sample2Asm.class", bytes)
}