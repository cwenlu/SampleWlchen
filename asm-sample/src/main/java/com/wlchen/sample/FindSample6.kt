package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.util.Printer

/**
 * 查找方法中调用了哪些方法
 * 找哪些方法调用了某个方法是一样的原理,只要每个方法里判断如果存在指定方法的调用则记录下来
 * @Author cwl
 * @Date 2023/11/3 9:58 AM
 */

class MethodFindInvokeVisitor : ClassVisitor(Opcodes.ASM9, null) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        //return super.visitMethod(access, name, descriptor, signature, exceptions)
        //可以对方法进行过滤,比如native,abstract方法不处理
        return MethodFindInvokeAdapter(name + descriptor)
    }

    private class MethodFindInvokeAdapter(private val anchorMethod: String) :
        MethodVisitor(Opcodes.ASM9) {
        private val methodList = arrayListOf<String>()
        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            val info = "${Printer.OPCODES[opcode]} ${owner}.${name}${descriptor}"
            if (!methodList.contains(info)) {
                methodList.add(info)
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }

        override fun visitEnd() {
            println("$anchorMethod{")
            methodList.forEach {
                println("   $it")
            }
            println("}\n")
            super.visitEnd()
        }
    }
}

fun findSample6() {
    val cr = ClassReader(readClass("Sample6.class"))
    cr.accept(MethodFindInvokeVisitor(), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
}