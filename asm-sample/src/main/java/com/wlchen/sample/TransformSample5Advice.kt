package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @Author cwl
 * @Date 2023/11/1 10:09 AM
 */

class MethodDumpAdviceVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {


    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (mv != null && (access and Opcodes.ACC_ABSTRACT) == 0
            && (access and Opcodes.ACC_NATIVE) == 0
        ) {
            mv = MethodDumpAdviceAdapter(mv, access, name, descriptor)
        }
        return mv
    }

    private class MethodDumpAdviceAdapter(
        mv: MethodVisitor,
        access: Int,
        name: String?,
        descriptor: String?
    ) : AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
        override fun onMethodEnter() {
            super.onMethodEnter()
            printMessage("method enter: $name$methodDesc")
            argumentTypes.forEachIndexed { index, type ->
                //将参数推入栈顶
                loadArg(index)
                //将参数弄成封装类型,因为我们打印接收obj
                box(type)
                print("(Ljava/lang/Object;)V")
            }
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            printMessage("method exit: $name$methodDesc")
            if (opcode == ATHROW) {
                super.visitLdcInsn("abnormal return")
            } else if (opcode == Opcodes.RETURN) {
                super.visitLdcInsn("return void")
            } else if (opcode == Opcodes.ARETURN) {
                dup()
            } else {
                if (opcode == LRETURN || opcode == DRETURN) {
                    dup2()
                } else {
                    dup()
                }
                box(returnType)
            }
            print("(Ljava/lang/Object;)V")
        }

        private fun printMessage(msg: String) {
            super.visitLdcInsn(msg)
            print("(Ljava/lang/Object;)V")
        }

        private fun print(desc: String) {
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/wlchen/sample/PrintUtil",
                "print",
                desc,
                false
            )
        }
    }
}

fun transformSample5Advice() {
    val cr = ClassReader(readClass("Sample5.class"))
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(MethodDumpAdviceVisitor(cw), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample5.class", cw.toByteArray())
}