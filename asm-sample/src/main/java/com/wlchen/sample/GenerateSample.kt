package com.wlchen.sample

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

/**
 * 用asm生成 [com.wlchen.sample.temp.Sample]
 * @Author cwl
 * @Date 2023/10/28 3:31 PM
 */

fun generateSample() {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    //生成类
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
        "com/wlchen/sample/temp/SampleAsm",
        null,
        "java/lang/Object",
        null
    )

    //生成字段
    var fv = cw.visitField(
        Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL or Opcodes.ACC_STATIC,
        "const_field",
        "I",
        null,
        10
    )
    fv.visitEnd()
    fv = cw.visitField(Opcodes.ACC_PRIVATE, "non_const_field", "I", null, null)
    fv.visitEnd()
    //非静态属性,这里给value传值没用,必须在构造方法里赋值
    fv = cw.visitField(0, "str", "Ljava/lang/String;", null, null)
    fv.visitEnd()

    //生成方法
    var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    mv.visitLdcInsn("abc")
    mv.visitFieldInsn(Opcodes.PUTFIELD, "com/wlchen/sample/temp/SampleAsm", "str", "Ljava/lang/Sting;")
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(2, 1)
    mv.visitEnd()
    mv = cw.visitMethod(
        Opcodes.ACC_PUBLIC,
        "test",
        "()V",
        null,
        arrayOf("java.io.FileNotFoundException", "java.io.IOException")
    )
    mv.visitCode()
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(0, 1)
    mv.visitEnd()

    cw.visitEnd()
    val bytes = cw.toByteArray()
    saveClass("SampleAsm.class", bytes)
}