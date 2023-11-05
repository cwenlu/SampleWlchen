package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * @Author cwl
 * @Date 2023/11/1 10:09 AM
 */

class MethodDumpVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {

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
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (mv != null && check(mv, access, name)) {
            mv = MethodDumpAdapter(mv, access, name, descriptor)
        }
        return mv
    }

    private class MethodDumpAdapter(
        mv: MethodVisitor,
        private val access: Int,
        private val name: String?,
        private val descriptor: String?
    ) : MethodVisitor(Opcodes.ASM9, mv) {
        override fun visitCode() {
            val isStatic = (access and Opcodes.ACC_STATIC) != 0
            //静态方法没有this所以slot从0开始
            var slotIndex = if (isStatic) 0 else 1
            val methodType = Type.getMethodType(descriptor)
            methodType.argumentTypes.forEach {
                val sort = it.sort
                val size = it.size
                val descriptor = it.descriptor
                //给定一类opcode得到数据类型相匹配的opcode,如float类型 给定ILOAD返回FLOAD
                val opcode = it.getOpcode(Opcodes.ILOAD)
                //将值load到栈顶
                super.visitVarInsn(opcode, slotIndex)
                printByType(sort,descriptor)
                slotIndex += size
            }
            super.visitCode()
        }

        override fun visitInsn(opcode: Int) {
            //在方法返回前插入
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                || opcode == Opcodes.ATHROW
            ) {
                //判定属于reference 返回.因为我们测试的返回的String ,所以这里只处理这个
                if (opcode == Opcodes.ARETURN) {
                    //复制栈顶一个值压入栈顶
                    //因为返回指令是ARETURN ,说明方法返回的reference,此时栈顶的值肯定是返回的值
                    super.visitInsn(Opcodes.DUP)
                    printByType(Type.OBJECT,"Ljava/lang/String;")
                }
            }
            super.visitInsn(opcode)
        }

        private fun printByType(sort: Int, descriptor: String) {
            when (sort) {
                //char,short,float等占一个slot的类似
                Type.BOOLEAN -> {
                    super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;"
                    )
                    //因为之前已经将对应值弄到栈顶,然后与我们后面加进去的方法指令进行交换
                    super.visitInsn(Opcodes.SWAP)
                    super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Z)V",
                        false
                    )
                }

                Type.OBJECT -> {
                    if ("Ljava/lang/String;" == descriptor) {
                        super.visitFieldInsn(
                            Opcodes.GETSTATIC,
                            "java/lang/System",
                            "out",
                            "Ljava/io/PrintStream;"
                        )
                        //因为之前已经将对应值弄到栈顶,然后与我们后面加进去的方法指令进行交换
                        super.visitInsn(Opcodes.SWAP)
                        super.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/PrintStream",
                            "println",
                            "(Ljava/lang/String;)V",
                            false
                        )
                    }
                }

                Type.LONG -> {
                    super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;"
                    )
                    //复制栈顶的值并插入到前面两个slot的位置
                    //因为long占两个slot,这个操作相当于把上面的方法指令放到了变量前面
                    //这个时候指令情况 out 变量 out
                    super.visitInsn(Opcodes.DUP_X2)
                    //栈顶的那个out不需要了,出栈
                    super.visitInsn(Opcodes.POP)
                    super.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(J)V",
                        false
                    )
                }
            }
        }
    }
}

fun transformSample5() {
    val cr = ClassReader(readClass("Sample5.class"))
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(MethodDumpVisitor(cw), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample5.class", cw.toByteArray())
}