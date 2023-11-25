package com.wlchen.playground.click

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.wlchen.playground.util.filterLambda
import com.wlchen.playground.util.isInterface
import com.wlchen.playground.util.nameWithDesc
import com.wlchen.playground.util.slotIndex
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * @Author cwl
 * @Date 2023/11/23 2:26 PM
 */

internal interface ViewClickParameters : InstrumentationParameters {
    @get:Classpath
    val bootClasspath: ListProperty<RegularFile>
}

internal abstract class ViewClickClassVisitorFactory :
    AsmClassVisitorFactory<ViewClickParameters> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ViewClickClassVisitor(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        //一般要排除自己插桩所需的一些代码，这里比如 playground-hook-lib 库下的东西
        return true
    }
}

private class ViewClickClassVisitor(
    private val nextClassVisitor: ClassVisitor
) : ClassNode(Opcodes.ASM9) {
    private val clickName = "onClick"
    private val clickInterface = "android/view/View\$OnClickListener"
    private val clickInterfaceDesc = "L${clickInterface};"
    private val clickMethodNameDesc = "onClick(Landroid/view/View;)V"
    override fun visitEnd() {
        super.visitEnd()
        //exclude interface.class,resolving the java8 default method
        if (isInterface(access)) {
            accept(nextClassVisitor)
            return
        }

        hook()
        accept(nextClassVisitor)
    }

    private fun hook() {
        val shouldHookMethodList = mutableSetOf<MethodNode>()
        val hasImpl = interfaces.contains(clickInterface)
        methods.forEach {
            if (hasImpl && it.nameWithDesc == clickMethodNameDesc) {
                shouldHookMethodList.add(it)
            }

            collectMethodOfLambda(it, shouldHookMethodList)
        }

        shouldHookMethodList.forEach {
            println("处理了${name}下${it.nameWithDesc}")
            //preventFastClick(it)
            preventFastClick2(it)
        }

    }

    /**
     * 收集lambda方法
     */
    private fun collectMethodOfLambda(methodNode: MethodNode, collectTo: MutableSet<MethodNode>) {
        val dynamicNodes = methodNode.filterLambda {
            /**
             * 1.JDK 9  字符串拼接使用inDy指令;此时bsm.owner=java/lang/invoke/StringConcatFactory
             * 2.JDK 11 动态常量使用inDy指令;此时bsm.owner=java/lang/invoke/ConstantBootstraps
             * 3.JDK 17 switch的模式匹配使用inDy指令;此时bsm.owner=java/lang/runtime/SwitchBootstraps
             */
            val isLambda = "java/lang/invoke/LambdaMetafactory" == it.bsm.owner
            /**
             * name: onClick
             * desc: ()Landroid/view/View$OnClickListener; 这里为啥是没有参数的方法？测试发现这里无论多少参数都不会体现
             */
            isLambda && it.name == clickName && it.desc.endsWith(clickInterfaceDesc)
        }

        //BootstrapMethods:
        //0: #43 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
        //Method arguments:
        //#32 (Landroid/view/View;)V
        //#36 REF_invokeStatic com/example/asmtest/MainActivity."onCreate$lambda-0":(Landroid/view/View;)V
        //#32 (Landroid/view/View;)V

        //bsmArgs[1] 为org.objectweb.asm.Handle  是自动生成的方法
        //其余两个为org.objectweb.asm.Type
        dynamicNodes.forEach {
            val handle = it.bsmArgs[1] as? Handle
            if (handle != null) {
                val nameWithDesc = handle.name + handle.desc
                methods.filterTo(collectTo) { nameWithDesc == it.nameWithDesc }
            }
        }
    }

    /**
     * 利用handler延迟方式禁用使能进行防连点
     */
    private fun preventFastClick(methodNode: MethodNode) {
        val instructions = methodNode.instructions
        if (instructions.size() > 0) {
            val list = InsnList().apply {
                add(VarInsnNode(Opcodes.ALOAD, methodNode.slotIndex(0)))
                add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "com/wlchen/sample/PreventFastClick",
                        "onClick",
                        "(Landroid/view/View;)V",
                        false
                    )
                )
            }
            instructions.insert(list)
        }
    }

    /**
     * view 结合时间处理
     */
    private fun preventFastClick2(methodNode: MethodNode){
        val instructions = methodNode.instructions
        if(instructions.size() > 0){
            val list = InsnList().apply {
                add(VarInsnNode(Opcodes.ALOAD, methodNode.slotIndex(0)))
                add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "com/wlchen/sample/PreventFastClick2",
                        "isFastClick",
                        "(Landroid/view/View;)Z",
                        false
                    )
                )
                val label = LabelNode()
                //相等执行括号的代码，不想等跳转label
                add(JumpInsnNode(Opcodes.IFEQ,label))
                add(InsnNode(Opcodes.RETURN))
                add(label)

            }
            instructions.insert(list)
        }
    }
}