package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.CheckClassAdapter
import java.io.PrintWriter

/**
 * @Author cwl
 * @Date 2023/11/3 4:11 PM
 */

class TransformCheckClassVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "compute") {
            return MethodErrorAdapter(mv)
        }
        return mv
    }

    private class MethodErrorAdapter(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {
        override fun visitCode() {
            //这样写利用CheckMethodAdapter 在转换过程中检查会报错
            //Cannot visit instructions before visitCode has been called.
            super.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            //super.visitLdcInsn("init")
            //本来接收string这里故意写成int,则会出现下面的检查错误
            //Error at instruction 2: Argument 1: expected Ljava/lang/String;, but found I
            super.visitLdcInsn(213)
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
            )
            super.visitCode()
        }
    }
}

/**
 * 在过程中就检测
 */
fun transformCheckSample8() {
    val cr = ClassReader(readClass("Sample8.class"))
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    val ccw = CheckClassAdapter(cw)
    //这种过程中的检查有些限制,会导致立马报错
    //如:在方法开头加入打印语句,提示:Cannot visit instructions before visitCode has been called.
    //ClassReader  --->  TransformCheckClassVisitor ---> CheckClassAdapter  ---> ClassWriter
    cr.accept(TransformCheckClassVisitor(cw), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    //使用这个在操作结束后检测
    CheckClassAdapter.verify(ClassReader(cw.toByteArray()), true/*是否打印字节码*/, PrintWriter(System.out))
    saveTransClass("Sample8.class", cw.toByteArray())
}