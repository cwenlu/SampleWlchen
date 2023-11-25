package com.wlchen.playground.util

import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
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
fun MethodNode.filterLambda(predicate: (InvokeDynamicInsnNode) -> Boolean): List<InvokeDynamicInsnNode> {
    if (instructions == null || instructions.size() == 0) {
        return emptyList()
    }
    return instructions.filterIsInstance<InvokeDynamicInsnNode>().filter(predicate)
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



