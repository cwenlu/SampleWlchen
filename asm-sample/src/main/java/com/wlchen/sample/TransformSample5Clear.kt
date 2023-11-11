package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 *
 * 清空方法体
 * planA: 将指令一条条移除掉,留下return
 * planB: 忽略原来的,从新生成
 * @Author cwl
 * @Date 2023/11/1 10:09 AM
 */

class MethodClearVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {

    /**
     * 构造方法,抽象方法,native方法都跳过
     * 构造函数第一句必须是super();
     */
    private fun check(mv: MethodVisitor?, access: Int, name: String?): Boolean {
        mv ?: return false
        return name != "<init>"
                && (access and Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT
                && (access and Opcodes.ACC_NATIVE) != Opcodes.ACC_NATIVE
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        //就简单判断Sample5 中的方法名
        if (mv != null && name == "dumpFun") {
            generateMethodBody(mv, access, descriptor)
            //返回null可以阻断后面的执行（因为这里已经生成了全新的,后面再处理没意义）。当然也可以返回mv
            return null
        }
        return mv
    }

    private fun generateMethodBody(
        mv: MethodVisitor,
        access: Int,
        descriptor: String?
    ) {
        val type = Type.getMethodType(descriptor)
        val returnType = type.returnType
        var slotIndex = if ((access and Opcodes.ACC_STATIC) != 0) 0 else 1
        type.argumentTypes.forEach {
            slotIndex += it.size
        }
        mv.visitCode()
        when (type.sort) {
            Type.VOID -> mv.visitInsn(Opcodes.RETURN)
            in (Type.BOOLEAN..Type.INT) -> {
                mv.visitInsn(Opcodes.ICONST_0)
                mv.visitInsn(Opcodes.IRETURN)
            }

            Type.LONG -> {
                mv.visitInsn(Opcodes.LCONST_0)
                mv.visitInsn(Opcodes.LRETURN)
            }

            Type.FLOAT -> {
                mv.visitInsn(Opcodes.FCONST_0)
                mv.visitInsn(Opcodes.FRETURN)
            }

            Type.DOUBLE -> {
                mv.visitInsn(Opcodes.DCONST_0)
                mv.visitInsn(Opcodes.DRETURN)
            }

            else -> {
                mv.visitInsn(Opcodes.ACONST_NULL)
                mv.visitInsn(Opcodes.ARETURN)
            }
        }
        //因为只有一个返回值,所以最大栈就是返回值的大小
        //所有参数的大小为局部方法表大小
        mv.visitMaxs(returnType.size, slotIndex)
        mv.visitEnd()
    }


}

fun transformSample5Clear() {
    val cr = ClassReader(readClass("Sample5.class"))
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(MethodClearVisitor(cw), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample5.class", cw.toByteArray())
}