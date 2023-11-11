package com.wlchen.sample

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import org.objectweb.asm.commons.StaticInitMerger
import org.objectweb.asm.tree.ClassNode

/**
 * 合并类 不能存在重复的接口，字段，方法
 * @Author cwl
 * @Date 2023/11/11 9:51 AM
 */
class ClassMergeVisitor(cv: ClassVisitor, private val anotherClass: ClassNode) :
    ClassVisitor(Opcodes.ASM9, cv) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        //合并接口
        val set = mutableSetOf<String>()
        interfaces?.let {
            set.addAll(it)
        }
        set.addAll(anotherClass.interfaces)
        super.visit(version, access, name, signature, superName, set.toTypedArray())
    }

    override fun visitEnd() {
        //处理field 复制字段
        anotherClass.fields.forEach {
            it.accept(this)
        }

        //处理method 复制方法
        anotherClass.methods.forEach {
            //构造方法跳过不处理
            if (it.name == "<init>") {
                return@forEach
            }
            it.accept(this)
        }
        super.visitEnd()
    }
}

/**
 *
 * 写visitor传递的心得
 *
 * read  --->  visitor  ---> write
 *
 * 按这个思路我们倒着写。如我们这的流程  read  --> 修改类名  --> 处理合并  --> 处理static块合并 --> write
 *
 * 所以步骤就是(代码倒着写，用啥前面补啥):
 * 1. ClassReader accept
 * 2. ClassRemapper 修改类名
 * 3. ClassMergeVisitor 处理合并  这里由于还需要ClassNode,同理我们需要 ClassReader accept ---> ClassNode
 * 4. StaticInitMerger 处理static块合并
 * 5.ClassWriter
 *
 *
 */
fun mergeSample() {
    val parseOptions = ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
    val cr1 = ClassReader(readClass("Merge1.class"))
    //将需要合入的变成node
    val cr2 = ClassReader(readClass("Merge2.class"))
    val cn2 = ClassNode()
    cr2.accept(cn2, parseOptions)

    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    //可以合并静态块
    val initMerger = StaticInitMerger("class_init$",cw)

    val cmv = ClassMergeVisitor(initMerger, cn2)

    //可以简单修改类名，字段名，方法名等
    val remapper = SimpleRemapper(
        cr2.className,
        cr1.className
    )
    val classRemapper = ClassRemapper(cmv, remapper)

    cr1.accept(classRemapper, parseOptions)
    saveClass("Merge1.class",cw.toByteArray())
}