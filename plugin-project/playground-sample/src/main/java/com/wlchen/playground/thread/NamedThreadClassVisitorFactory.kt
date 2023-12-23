package com.wlchen.playground.thread

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.wlchen.playground.util.appendArgument
import com.wlchen.playground.util.nameWithDesc
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode

/**
 * @Author cwl
 * @Date 2023/12/11 11:59 AM
 */
internal abstract class NamedThreadClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return NamedThreadClassVisitor(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }
}

private class NamedThreadClassVisitor(private val nextClassVisitor: ClassVisitor) :
    ClassNode(Opcodes.ASM9) {

    private companion object {
        const val threadClass = "java/lang/Thread"
        const val threadFactoryClass = "java/util/concurrent/ThreadFactory"
        const val threadFactoryNewThreadMethodDesc =
            "newThread(Ljava/lang/Runnable;)Ljava/lang/Thread;"
    }

    override fun visitEnd() {
        super.visitEnd()
        hook()

        accept(nextClassVisitor)
    }

    //不考虑Thread继承关系，如果考虑继承关系需要先扫描一遍
    private fun hook() {
        methods.forEach { methodNode ->
            methodNode.instructions.forEach { insnNode ->
                when (insnNode.opcode) {
                    Opcodes.NEW -> {
                        (insnNode as? TypeInsnNode)?.also {
                            if (it.desc == threadClass) {
                                //如果是在 ThreadFactory 内初始化线程，则不处理
                                if (!isThreadFactoryMethod(methodNode)) {
                                    transformNewThreadInstruction(methodNode, insnNode)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun transformNewThreadInstruction(
        methodNode: MethodNode,
        newInsnNode: TypeInsnNode,
    ) {
        val instructions = methodNode.instructions
        //这里不使用外部传进来的索引，是因为下面插入之后instructions指令发生了变化
        val newInsnIndex = instructions.indexOf(newInsnNode)
        for (index in newInsnIndex + 1 until instructions.size()) {
            val node = instructions[index]
            if (node is MethodInsnNode && node.isThreadInitMethod()) {
                //修改new 的类型描述
                newInsnNode.desc = "com/wlchen/sample/thread/NamedThread"
                //修改构造的thread
                node.owner = "com/wlchen/sample/thread/NamedThread"
                node.appendArgument("Ljava/lang/String;")
                instructions.insertBefore(node, LdcInsnNode(name))
                break
            }

        }
    }

    private fun MethodInsnNode.isThreadInitMethod(): Boolean {
        return owner == threadClass && name == "<init>"
    }

    private fun ClassNode.isThreadFactoryMethod(methodNode: MethodNode): Boolean {
        return interfaces.contains(threadFactoryClass) && methodNode.nameWithDesc == threadFactoryNewThreadMethodDesc
    }
}