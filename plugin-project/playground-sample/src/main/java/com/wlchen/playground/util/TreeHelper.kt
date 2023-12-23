package com.wlchen.playground.util

import org.objectweb.asm.Handle
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * @Author cwl
 * @Date 2023/11/23 10:47 AM
 */

val ClassNode.isInterface: Boolean get() = isInterface(access)

val ClassNode.isAbstract: Boolean get() = isAbstract(access)

val ClassNode.simpleName: String get() = name.substringAfterLast('/')

val ClassNode.isObject: Boolean get() = isObject(name)

val MethodNode.isAbstract: Boolean get() = isAbstract(access)

val MethodNode.isStatic: Boolean get() = isStatic(access)

val MethodNode.isNative: Boolean get() = isNative(access)

val MethodNode.isInit: Boolean get() = isInit(name)

val MethodNode.isCinit: Boolean get() = isCinit(name)

/**
 * 组合name+desc 可以用于唯一定位方法
 */
val MethodNode.nameWithDesc: String get() = name + desc

/**
 * 是否需要访问方法
 *
 * 不对抽象方法、native方法、桥接方法、合成方法进行织入
 */
val MethodNode.isNeedVisit: Boolean get() = isNeedVisit(access)

/**
 * 是否包含某个注解,包含返回true 反之返回false
 *
 * [desc]为注解描述,[visible]表示是否运行时可见
 * 如要判断是否包含androidx.annotation.Nullable,则desc为 Landroidx/annotation/Nullable;
 */
fun MethodNode.hasAnnotation(desc: String, visible: Boolean): Boolean {
    val annotations = if (visible) visibleAnnotations else invisibleAnnotations
    return annotations?.find { it.desc == desc } != null
}

/**
 * java 1.8 引入的type annotation
 */
fun MethodNode.hasTypeAnnotation(desc: String, visible: Boolean): Boolean {
    val annotations = if (visible) visibleTypeAnnotations else invisibleTypeAnnotations
    return annotations?.find { it.desc == desc } != null
}

/**
 * 不关心运行时可见性，看是否有指定注解
 */
fun MethodNode.hasAnnotation(desc: String): Boolean {
    return hasAnnotation(desc, true) || hasAnnotation(desc, false)
}

fun MethodNode.hasTypeAnnotation(desc: String): Boolean {
    return hasTypeAnnotation(desc, true) || hasTypeAnnotation(desc, false)
}

/**
 * 筛选符合条件的[InvokeDynamicInsnNode]node
 */
fun MethodNode.filterIndy(predicate: (InvokeDynamicInsnNode) -> Boolean): List<InvokeDynamicInsnNode> {
    if (instructions == null || instructions.size() == 0) {
        return emptyList()
    }
    return instructions.filterIsInstance<InvokeDynamicInsnNode>().filter(predicate)
}

/**
 * 收集lambda方法
 */
fun MethodNode.collectMethodOfLambda(
    methods: List<MethodNode>,
    predicate: (InvokeDynamicInsnNode) -> Boolean
): Set<MethodNode> {
    val dynamicNodes = filterIndy {
        /**
         * 1.JDK 9  字符串拼接使用inDy指令;此时bsm.owner=java/lang/invoke/StringConcatFactory
         * 2.JDK 11 动态常量使用inDy指令;此时bsm.owner=java/lang/invoke/ConstantBootstraps
         * 3.JDK 17 switch的模式匹配使用inDy指令;此时bsm.owner=java/lang/runtime/SwitchBootstraps
         */
        val isLambda = "java/lang/invoke/LambdaMetafactory" == it.bsm.owner
        /**
         * name: onClick
         * desc: ()Landroid/view/View$OnClickListener;
         */
        isLambda && predicate(it)
    }
    val ret = mutableSetOf<MethodNode>()
    //BootstrapMethods:
    //0: #43 REF_invokeStatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
    //Method arguments:
    //#32 (Landroid/view/View;)V
    //#36 REF_invokeStatic com/example/asmtest/MainActivity."onCreate$lambda-0":(Landroid/view/View;)V
    //#32 (Landroid/view/View;)V
    dynamicNodes.forEach {
        val handle = it.bsmArgs[1] as? Handle
        if (handle != null) {
            val nameWithDesc = handle.name + handle.desc
            methods.filterTo(ret) { nameWithDesc == it.nameWithDesc }
        }
    }
    return ret
}


/**
 * 获取[desc]方法描述中[index]位置的参数的slot
 * 如:静态方法 (Ljava/lang/String;L)V string占1个slot double占2个slot 所以返回值索引为3-1
 */
fun MethodNode.slotIndex(
    index: Int = 0,
    argumentTypes: Array<Type> = Type.getArgumentTypes(desc)
): Int {
    var ret = if (isStatic) 0 else 1
    repeat(index) {
        ret += argumentTypes[it].size
    }
    return ret
}

fun MethodNode.newSlotIndex(desc: String, type: Type): Int {
    val argumentTypes = Type.getArgumentTypes(desc)
    return slotIndex(argumentTypes.size) + type.size
}

fun MethodInsnNode.appendArgument(appendArgumentDesc: String) {
    val type = Type.getMethodType(desc)
    val argumentTypes = type.argumentTypes
    val returnType = type.returnType
    desc = "(" + argumentTypes.joinToString("") + appendArgumentDesc + ")" + returnType.descriptor
}

fun MethodInsnNode.appendArgument(argumentType: Class<*>) {
    //appendArgument(Type.getType(argumentType).descriptor)

    val type = Type.getMethodType(desc)
    val argumentTypes = type.argumentTypes
    val returnType = type.returnType
    val newArgumentTypes = arrayOfNulls<Type>(argumentTypes.size + 1)
    System.arraycopy(argumentTypes, 0, newArgumentTypes, 0, argumentTypes.size - 1 + 1)
    newArgumentTypes[newArgumentTypes.size - 1] = Type.getType(argumentType)
    desc = Type.getMethodDescriptor(returnType, *newArgumentTypes)
}



