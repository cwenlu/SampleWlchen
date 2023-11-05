package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Author cwl
 * @Date 2023/10/31 10:01 AM
 */
class TransformSampleVisitor(cw: ClassWriter) : ClassVisitor(Opcodes.ASM9, cw) {
    var has_add_field = false
    var owner: String? = null
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        owner = name
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        //因为代理的ClassVisitor 是 ClassWriter,返回null,则表示不关注这个field,反映到ClassWriter上就是不会生成该字段
        //删除这个属性
        if (name == "non_const_field" && descriptor == "I") {
            return null
        }
        //判断是否存在这个字段,对于field,判断名字就可以了,因为不允许同名
        if (name == "add_field" && descriptor == "Z") {
            has_add_field = true
        }

        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>" && descriptor == "()V") {
            //也可以在这个时候添加字段,甚至可以在构造函数里久直接加,前提是明确知道内部已有字段,或者写特殊名字保证不重复
            //if (!has_add_field) {
            //    super.visitField(Opcodes.ACC_PUBLIC, "add_field", "Z", null, null)?.run {
            //        visitEnd()
            //    }
            //}
            mv = object : MethodVisitor(Opcodes.ASM9, mv) {
                override fun visitInsn(opcode: Int) {
                    if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW) {
                        super.visitVarInsn(Opcodes.ALOAD, 0)
                        //对于boolean ICONST_0 为false ICONST_1 为true
                        super.visitInsn(Opcodes.ICONST_1)
                        //这个提前设置值,在生成之前不会检测的,相当于写代码提前写了赋值代码,后面再声明
                        super.visitFieldInsn(Opcodes.PUTFIELD, owner, "add_field", "Z")
                    }
                    super.visitInsn(opcode)
                }
            }
        }
        return mv
    }

    override fun visitEnd() {
        if (!has_add_field) {
            super.visitField(Opcodes.ACC_PUBLIC, "add_field", "Z", null, null)?.run {
                visitEnd()
            }
        }
        super.visitEnd()
    }
}

fun transformSample() {
    val bytes = readClass("Sample.class")
    val cr = ClassReader(bytes)
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    cr.accept(TransformSampleVisitor(cw), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
    saveTransClass("Sample.class", cw.toByteArray())
}

