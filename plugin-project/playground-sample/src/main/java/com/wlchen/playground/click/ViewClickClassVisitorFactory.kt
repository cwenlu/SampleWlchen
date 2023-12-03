package com.wlchen.playground.click

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.wlchen.playground.util.collectMethodOfLambda
import com.wlchen.playground.util.isInterface
import com.wlchen.playground.util.nameWithDesc
import com.wlchen.playground.util.slotIndex
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author cwl
 * @Date 2023/11/23 2:26 PM
 */


internal abstract class ViewClickClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {
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

/**
 * 因为是逐类扫描处理,所以对于方法引用方式(如果是其他类的方法)存在不能hook的漏洞
 *
 * [可以使用代理方法处理](https://juejin.cn/post/7127563086566785038?searchId=202311191219195B434422FB006831EE12#heading-9)
 */
private class ViewClickClassVisitor(
    private val nextClassVisitor: ClassVisitor
) : ClassNode(Opcodes.ASM9) {
    private val clickName = "onClick"
    private val clickInterface = "android/view/View\$OnClickListener"
    private val clickInterfaceDesc = "L${clickInterface};"
    private val clickMethodNameDesc = "onClick(Landroid/view/View;)V"
    private val counter = AtomicInteger()
    private val syntheticMethodList = arrayListOf<MethodNode>()
    override fun visitEnd() {
        super.visitEnd()
        //exclude interface.class,resolving the java8 default method
        if (isInterface(access)) {
            accept(nextClassVisitor)
            return
        }

        //hook()
        //测试代理lambda方法
        hook2()
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

    private fun hook2(){
        methods.forEach {
            //使用代理方法 https://zhuanlan.zhihu.com/p/159286720
            createLambdaProxyMethod(it)
        }
        //注意不要在循环中去给methods 添加元素
        methods.addAll(syntheticMethodList)
    }

    /**
     * 生成lambda代理方法
     *
     * 为了避免生成大量代理方法，可以判断只针对垮类的引用方式进行代理方法处理
     *
     */
    private fun createLambdaProxyMethod(methodNode: MethodNode) {

        val iterator = methodNode.instructions.iterator()
        while (iterator.hasNext()) {
            val node = iterator.next()
            if (node is InvokeDynamicInsnNode) {
                //sam函数式接口(Single Abstract Method)
                //()Landroid/view/View$OnClickListener;
                //it.desc 如果捕获了外部引用则会有参数
                val descType = Type.getType(node.desc)
                val samMethodType = node.bsmArgs[0] as Type
                //sam 实现方法实际参数描述符
                val implMethodType = node.bsmArgs[2] as Type
                //这里限定只代理点击事件的lambda 方法
                val hook = node.name == clickName && node.desc.endsWith(clickInterfaceDesc)
                if(!hook){
                    continue
                }
                //中间方法的名称
                val middleMethodName = "lambda$${node.name}\$cwl${counter.incrementAndGet()}"
                var middleMethodDesc = ""
                val descArgType = descType.argumentTypes
                //如果没有外部引用参数，则直接实用实际参数作为中间方法参数
                //如果有外部引用参数,则需要拼起来
                if (descArgType.isEmpty()) {
                    middleMethodDesc = implMethodType.descriptor
                } else {
                    middleMethodDesc = "("
                    descArgType.forEach {
                        middleMethodDesc += it.descriptor
                    }
                    middleMethodDesc += implMethodType.descriptor.replace("(", "")
                }
                val oldHandle = node.bsmArgs[1] as Handle
                val newHandle =
                    Handle(Opcodes.H_INVOKESTATIC, name, middleMethodName, middleMethodDesc, false)
                val newDynamicNode = InvokeDynamicInsnNode(
                    node.name,
                    node.desc,
                    node.bsm,
                    samMethodType,
                    newHandle,
                    implMethodType
                )
                iterator.remove()
                iterator.add(newDynamicNode)
                syntheticMethodList.add(
                    generateMiddleMethod(
                        oldHandle,
                        middleMethodName,
                        middleMethodDesc
                    )
                )
            }
        }
    }

    private fun generateMiddleMethod(
        oldHandle: Handle,
        middleMethodName: String,
        middleMethodDesc: String
    ): MethodNode {

        val methodNode =
            MethodNode(
                Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC/* + Opcodes.ACC_SYNTHETIC*/,
                middleMethodName,
                middleMethodDesc,
                null,
                null
            )
        methodNode.visitCode()
        //这里我们可以在调用原方法前插入代码

        // 此块 tag 具体可以参考: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic
        //https://stackoverflow.com/questions/27488642/java-asm-opcodes-h-prefixed-mnemonics-e-g-opcodes-h-getfield-vs-opcodes-g
        //The H_ constants in the Opcodes class are no actual opcodes,
        //they are used for building a MethodHandle (using ASMs Handle class) which can be used in InvokeDynamic instructions.
        //H_指定不是真实的指令，它们用于生成可在 InvokeDynamic 指令中使用的 MethodHandle（使用 ASM Handle 类）
        var accResult = oldHandle.tag
        when (accResult) {
            Opcodes.H_INVOKEINTERFACE -> accResult = Opcodes.INVOKEINTERFACE
            Opcodes.H_INVOKESPECIAL -> accResult =
                Opcodes.INVOKESPECIAL // private, this, super 等会调用
            Opcodes.H_NEWINVOKESPECIAL -> {
                // constructors
                accResult = Opcodes.INVOKESPECIAL
                methodNode.visitTypeInsn(Opcodes.NEW, oldHandle.owner)
                methodNode.visitInsn(Opcodes.DUP)
            }

            Opcodes.H_INVOKESTATIC -> accResult = Opcodes.INVOKESTATIC
            Opcodes.H_INVOKEVIRTUAL -> accResult = Opcodes.INVOKEVIRTUAL
        }
        val middleMethodType = Type.getType(middleMethodDesc)
        var slotIndex = 0
        middleMethodType.argumentTypes.forEach {
            val opcode = it.getOpcode(Opcodes.ILOAD)
            methodNode.visitVarInsn(opcode, slotIndex)
            slotIndex += it.size
        }
        methodNode.visitMethodInsn(
            accResult,
            oldHandle.owner,
            oldHandle.name,
            oldHandle.desc,
            false
        )
        methodNode.visitInsn(middleMethodType.returnType.getOpcode(Opcodes.IRETURN))
        methodNode.visitEnd()
        return methodNode
    }


    /**
     * 收集lambda方法
     */
    private fun collectMethodOfLambda(methodNode: MethodNode, collectTo: MutableSet<MethodNode>) {
        methodNode.collectMethodOfLambda(methods) {
            /**
             * name: onClick
             * desc: ()Landroid/view/View$OnClickListener;
             */
            it.name == clickName && it.desc.endsWith(clickInterfaceDesc)
        }.let {
            collectTo.addAll(it)
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
    private fun preventFastClick2(methodNode: MethodNode) {
        val instructions = methodNode.instructions
        if (instructions.size() > 0) {
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
                add(JumpInsnNode(Opcodes.IFEQ, label))
                add(InsnNode(Opcodes.RETURN))
                add(label)

            }
            instructions.insert(list)
        }
    }
}