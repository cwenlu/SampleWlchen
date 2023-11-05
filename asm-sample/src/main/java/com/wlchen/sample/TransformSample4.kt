package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Author cwl
 * @Date 2023/10/31 10:01 AM
 */

class Sample4MethodEnterVisitor(cw: ClassWriter) : ClassVisitor(Opcodes.ASM9, cw) {

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

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        println(name)
        //改包名 方便和原始文件区分存放 配合execMethod 运行测试
        val newName = name?.let {
            val index = it.lastIndexOf("/")
            StringBuilder(name).insert(index, "/trans").toString()
        }
        println(newName)

        super.visit(version, access, newName, signature, superName, interfaces)
    }


    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (check(mv, access, name)) {
            mv = MethodEnterAdapter(mv)
        }
        return mv
    }

    private class MethodEnterAdapter(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {
        override fun visitCode() {
            //也可以用mv,但是需要加非空判断,所以我们直接用super
            super.visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            super.visitLdcInsn("Method Enter....")
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

class Sample4MethodExitVisitor(cw: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cw) {

    /**
     * 构造方法,抽象方法,native方法都跳过
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
        if (check(mv, access, name)) {
            mv = MethodExitAdapter(mv)
        }
        return mv
    }

    private class MethodExitAdapter(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {

        override fun visitInsn(opcode: Int) {
            //抛出异常或者正常退出
            if (opcode and Opcodes.ATHROW == Opcodes.ATHROW
                || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
            ) {
                super.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;"
                )
                super.visitLdcInsn("Method exit ....")
                super.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/String;)V",
                    false
                )
            }
            super.visitInsn(opcode)
        }
    }
}

//利用串联的方式
fun transformSample4PlanA() {
    val bytes = readClass("Sample4.class")
    val cr = ClassReader(bytes)
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    val mvEnter = Sample4MethodEnterVisitor(cw)
    val mvExit = Sample4MethodExitVisitor(mvEnter)
    cr.accept(mvExit, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample4.class", cw.toByteArray())
}

//一个xxxAdapter就可以
fun transformSample4PlanB() {

}