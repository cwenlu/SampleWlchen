package com.wlchen.sample

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes

/**
 * 用asm生成 [com.wlchen.sample.temp.Sample3]
 * @Author cwl
 * @Date 2023/10/28 3:31 PM
 */

fun generateSample3() {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
    //生成类
    cw.visit(
        Opcodes.V1_8,
        Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
        "com/wlchen/sample/temp/Sample3Asm",
        null,
        "java/lang/Object",
        null
    )

    //构造函数
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
    mv.visitCode()
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "Ljava/lang/Object;", "<init>", "()V", false)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(1, 1)
    mv.visitEnd()

    testIf(cw)
    testSwitch(cw)
    testFor(cw)
    testTryCatch(cw)

    cw.visitEnd()
    val bytes = cw.toByteArray()
    saveClass("Sample3Asm.class", bytes)
}

private fun testIf(cw: ClassVisitor) {
    //test方法
    val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "testIf", "(Z)Ljava/lang/String;", null, null)
    mv.visitCode()

    //if部分
    mv.visitVarInsn(Opcodes.ILOAD, 1)
    val elseLabel = Label()
    //IFEQ 当栈顶int型数值等于0(false)时跳转
    mv.visitJumpInsn(Opcodes.IFEQ, elseLabel)
    mv.visitLdcInsn("visible")
    mv.visitVarInsn(Opcodes.ASTORE, 2)
    val returnLabel = Label()
    mv.visitJumpInsn(Opcodes.GOTO, returnLabel)

    //构造else部分
    mv.visitLabel(elseLabel)
    mv.visitLdcInsn("gone")
    mv.visitVarInsn(Opcodes.ASTORE, 2)

    //return部分
    mv.visitLabel(returnLabel)
    mv.visitVarInsn(Opcodes.ALOAD, 2)
    mv.visitInsn(Opcodes.ARETURN)
    mv.visitMaxs(1, 3)
    mv.visitEnd()
}

private fun testSwitch(cw: ClassVisitor) {
    val mv = cw.visitMethod(Opcodes.ACC_PRIVATE, "testSwitch", "(I)V", null, null)
    mv.visitCode()

    //switch
    mv.visitVarInsn(Opcodes.ILOAD, 1)
    val lable1 = Label()
    val lable2 = Label()
    val lableDflt = Label()
    val labelReturn = Label()
    // default的label, case 值和对应的label执行块
    mv.visitLookupSwitchInsn(lableDflt, intArrayOf(1, 2), arrayOf(lable1, lable2))

    //label1
    mv.visitLabel(lable1)
    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    mv.visitLdcInsn("key is 1")
    mv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        "(Ljava/lang/String;)V",
        false
    )
    mv.visitJumpInsn(Opcodes.GOTO, labelReturn)

    //label2
    mv.visitLabel(lable2)
    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    mv.visitLdcInsn("key is 2")
    mv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        "(Ljava/lang/String;)V",
        false
    )
    mv.visitJumpInsn(Opcodes.GOTO, labelReturn)

    //labelDflt
    mv.visitLabel(lableDflt)
    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    mv.visitLdcInsn("key is unknown")
    mv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        "(Ljava/lang/String;)V",
        false
    )
    //labelReturn
    mv.visitLabel(labelReturn)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(2, 2)

    mv.visitEnd()
}

/**
 * if_icmpeq	比较栈顶两int类型数值大小，当前者等于后者时跳转
 * if_icmpne	比较栈顶两int类型数值大小，当前者不等于后者时跳转
 * if_icmplt	比较栈顶两int类型数值大小，当前者小于后者时跳转
 * if_icmple	比较栈顶两int类型数值大小，当前者小于等于后者时跳转
 * if_icmpgt	比较栈顶两int类型数值大小，当前者大于后者时跳转
 * if_icmpge	比较栈顶两int类型数值大小，当前者大于等于后者时跳转
 * if_acmpeq	比较栈顶两int类型数值大小，当结果相等时跳转
 * if_acmpne	比较栈顶两int类型数值大小，当结果不相等时跳转
 *
 */
private fun testFor(cw: ClassVisitor) {
    val mv = cw.visitMethod(0, "testFor", "()Ljava/lang/String;", null, null)
    mv.visitCode()
    //new 一个对象
    mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
    //dup 复制一个new构造的对象,因为接下来执行init需要它
    mv.visitInsn(Opcodes.DUP)
    //执行init,执行完后dup的那个对象就消耗掉了(出栈)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
    //将生成的对象放到局部变量表索引1的位置
    mv.visitVarInsn(Opcodes.ASTORE, 1)
    //将常量0推入栈顶
    mv.visitInsn(Opcodes.ICONST_0)
    //将0存到索引2的位置
    mv.visitVarInsn(Opcodes.ISTORE, 2)
    val labelFor = Label()
    val labelReturn = Label()
    mv.visitLabel(labelFor)
    //将索引2的数据(0)推入栈顶
    mv.visitVarInsn(Opcodes.ILOAD, 2)
    //将常量4推入栈顶
    mv.visitInsn(Opcodes.ICONST_4)
    //比较栈顶两个元素,前者大于等于后者时跳转
    mv.visitJumpInsn(Opcodes.IF_ICMPGE, labelReturn)
    //把StringBuilder弄出来
    mv.visitVarInsn(Opcodes.ALOAD, 1)
    //把索引2的循环索引拿出来(0)
    mv.visitVarInsn(Opcodes.ILOAD, 2)
    //执行append
    mv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/lang/StringBuilder",
        "append",
        "(I)Ljava/lang/StringBuilder;",
        false
    )
    //将结果弹出
    mv.visitInsn(Opcodes.POP)
    //对于一个指定不知道该用哪个visit方法可以在Opcodes中去搜索对应指令
    //将指定int型变量增加指定值
    //第一个参数三数据在局部变量表的索引,这里是2
    //第2个参数是要加的值
    mv.visitIincInsn(2, 1)
    mv.visitJumpInsn(Opcodes.GOTO, labelFor)

    mv.visitLabel(labelReturn)
    //减索引1的数据(StringBuilder)推入栈顶
    mv.visitVarInsn(Opcodes.ALOAD, 1)
    mv.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/lang/StringBuilder",
        "toString",
        "()Ljava/lang/String;",
        false
    )
    mv.visitInsn(Opcodes.ARETURN)
    mv.visitMaxs(2, 3)

    mv.visitEnd()
}

private fun testTryCatch(cw: ClassVisitor) {
    val mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "testTryCatch", "()V", null, null)
    mv.visitCode()
    val startLabel = Label()
    val endLabel = Label()
    val handlerLabel = Label()
    val returnLabel = Label()

    mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "java/lang/Exception")
    mv.visitLabel(startLabel)
    mv.visitVarInsn(Opcodes.BIPUSH, 10)
    mv.visitVarInsn(Opcodes.ISTORE, 1)
    mv.visitVarInsn(Opcodes.SIPUSH, 1000)
    mv.visitVarInsn(Opcodes.ISTORE, 2)
    mv.visitVarInsn(Opcodes.ILOAD, 1)
    mv.visitVarInsn(Opcodes.ILOAD, 2)
    mv.visitInsn(Opcodes.IADD)
    mv.visitVarInsn(Opcodes.ISTORE, 3)

    mv.visitLabel(endLabel)
    mv.visitJumpInsn(Opcodes.GOTO, returnLabel)

    mv.visitLabel(handlerLabel)
    //将栈顶数据放到局部变量表1的位置,猜测发生异常的时候栈顶会产生一个错误的Exception
    mv.visitVarInsn(Opcodes.ASTORE, 1)
    mv.visitVarInsn(Opcodes.ALOAD, 1)
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,"java/lang/Exception","printStackTrace","()V",false)

    mv.visitLabel(returnLabel)
    mv.visitInsn(Opcodes.RETURN)
    mv.visitMaxs(2, 4)
    mv.visitEnd()
}