package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Author cwl
 * @Date 2023/11/1 10:09 AM
 */
abstract class MethodPatternAdapter(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {
    protected companion object {
        /**
         * 啥都没找到
         */
        const val FOUND_NOTHING = 0
    }

    /**
     * 当前找到的字节码特征状态
     */
    protected var state: Int = FOUND_NOTHING

    /**
     * 1.子类复写相关方法对需要关注的特征处理 (状态机方式)
     * 2.找到后记录下来,拦截指令的写入
     *
     * 3.由于在找特征的时候会暂时阻止,指令的写入同时标记指令
     * 如果最后发现不满足则会将之前缓存的指令全部写入
     * 下面凡事对特征寻找有影响的方法都调用这个方法以便触发将缓存的指令写入
     *
     */
    protected abstract fun visitInsn()

    override fun visitInsn(opcode: Int) {
        visitInsn()
        super.visitInsn(opcode)
    }

    override fun visitFrame(
        type: Int, numLocal: Int, local: Array<out Any>?, numStack: Int, stack: Array<out Any>?
    ) {
        visitInsn()
        super.visitFrame(type, numLocal, local, numStack, stack)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        visitInsn()
        super.visitIntInsn(opcode, operand)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
        visitInsn()
        super.visitVarInsn(opcode, `var`)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        visitInsn()
        super.visitTypeInsn(opcode, type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        visitInsn()
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        visitInsn()
        super.visitMethodInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int, owner: String?, name: String?, descriptor: String?, isInterface: Boolean
    ) {
        visitInsn()
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
        visitInsn()
        super.visitInvokeDynamicInsn(
            name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments
        )
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        visitInsn()
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label?) {
        visitInsn()
        super.visitLabel(label)
    }

    override fun visitLdcInsn(value: Any?) {
        visitInsn()
        super.visitLdcInsn(value)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        visitInsn()
        super.visitIincInsn(`var`, increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        visitInsn()
        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        visitInsn()
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
        visitInsn()
        super.visitMultiANewArrayInsn(descriptor, numDimensions)
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        visitInsn()
        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        visitInsn()
        super.visitMaxs(maxStack, maxLocals)
    }
}


/**
 * 将+0 去掉
 */
class RemoveAddZeroVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (mv != null && (access and Opcodes.ACC_ABSTRACT) == 0 && (access and Opcodes.ACC_NATIVE) == 0) {
            mv = RemoveAddZeroAdapter(mv)
        }
        return mv
    }

    private class RemoveAddZeroAdapter(mv: MethodVisitor) : MethodPatternAdapter(mv) {
        //定义的值不能和FOUND_NOTHING 重复
        companion object {
            const val FOUND_ICONST_0 = 1
        }

        /**
         * 指令特征
         *
         * ICONST_0
         *
         * IADD
         */
        override fun visitInsn(opcode: Int) {
            when (state) {
                FOUND_NOTHING -> {
                    if (opcode == Opcodes.ICONST_0) {
                        state = FOUND_ICONST_0
                        return
                    }
                }

                FOUND_ICONST_0 -> {
                    if (opcode == Opcodes.IADD) {
                        state = FOUND_NOTHING
                        return
                    } else if (opcode == Opcodes.ICONST_0) {
                        //这里有个特殊的就是连续2个ICONST_0,这个时候我们不能复位,但是我们要以新的那个作为开始标记
                        //不知道什么情况会连续2个ICONST_0
                        //所以我们提前放入了之前缓存的ICONST_0
                        //注意这里不能用super
                        mv.visitInsn(Opcodes.ICONST_0)
                        return
                    }
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitInsn() {
            //走到这说明在执行新指令时没有被拦截(既没有匹配到部分特征)
            //然后判断当前时匹配到多少部分的特征,将缓存的指令全部写入,重置state
            if (state == FOUND_ICONST_0) {
                //注意这里不能用super,这样会导致死循环,因为父类中指令相关放啊调用了自己
                mv.visitInsn(Opcodes.ICONST_0)
            }
            state = FOUND_NOTHING
        }

    }
}

/**
 * 删除打印string的语句
 */
class RemovePrintVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (mv != null && (access and Opcodes.ACC_ABSTRACT) == 0 && (access and Opcodes.ACC_NATIVE == 0)) {
            mv = RemovePrintAdapter(mv)
        }
        return mv
    }

    private class RemovePrintAdapter(mv: MethodVisitor) : MethodPatternAdapter(mv) {
        companion object {
            const val FOUND_GETSTATIC = 1
            const val FOUND_GETSTATIC_LDC = 2
        }

        private var message: String? = null
        override fun visitFieldInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?
        ) {
            val flag = opcode == Opcodes.GETSTATIC && owner == "java/lang/System"
                    && name == "out" && descriptor == "Ljava/io/PrintStream;"
            when (state) {
                FOUND_NOTHING -> {
                    if (flag) {
                        state = FOUND_GETSTATIC
                        return
                    }
                }

                FOUND_GETSTATIC -> {
                    //原因和上面删除+0类似
                    //遇到连续相同的特征开头指令需要以最后面的为准,所以这里放一个出去
                    if (flag) {
                        mv.visitFieldInsn(opcode, owner, name, descriptor)
                        return
                    }
                }
            }
            super.visitFieldInsn(opcode, owner, name, descriptor)
        }

        override fun visitLdcInsn(value: Any?) {
            if (state == FOUND_GETSTATIC) {
                if (value is String) {
                    state = FOUND_GETSTATIC_LDC
                    message = value
                    return
                }
            }
            super.visitLdcInsn(value)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?
        ) {
            val flag = opcode == Opcodes.INVOKEVIRTUAL && owner == "java/io/PrintStream"
                    && name == "println" && descriptor == "(Ljava/lang/String;)V"
            if (state == FOUND_GETSTATIC_LDC) {
                if (flag) {
                    state = FOUND_NOTHING
                    return
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            val flag = opcode == Opcodes.INVOKEVIRTUAL && owner == "java/io/PrintStream"
                    && name == "println" && descriptor == "(Ljava/lang/String;)V"
            if (state == FOUND_GETSTATIC_LDC) {
                if (flag) {
                    state = FOUND_NOTHING
                    return
                }
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }

        override fun visitInsn() {
            when (state) {
                FOUND_GETSTATIC -> {
                    mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;"
                    )
                }
                FOUND_GETSTATIC_LDC -> {
                    mv.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "java/lang/System",
                        "out",
                        "Ljava/io/PrintStream;"
                    )
                    mv.visitLdcInsn(message)
                }
            }
            state = FOUND_NOTHING
        }

    }
}

fun transformSample7() {
    val cr = ClassReader(readClass("Sample7.class"))
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(RemovePrintVisitor(RemoveAddZeroVisitor(cw)), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample7.class", cw.toByteArray())
}