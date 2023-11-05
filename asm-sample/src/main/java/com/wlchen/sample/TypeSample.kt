package com.wlchen.sample

import org.objectweb.asm.Type

/**
 * @Author cwl
 * @Date 2023/10/31 1:49 PM
 */

fun typeSample() {
    Type.getType("Ljava/lang/String;").run {
        println(sort)
        println(className)
        println(descriptor)
    }

    println("================")

    println(Type.getType(Int::class.java))
    println(Type.FLOAT_TYPE)
    println(Type.getMethodType("(ZI)Ljava/lang/String;"))

    println("================")

    Type.getType("[Z").run {
        //数组纬度
        println(dimensions)
        //数字里元素类型
        println(elementType)
    }

    println("================")

    //getType 可以平替getMethodType
    Type./*getMethodType*/getType("(Ljava/lang/String;I)Z").run {
        println(returnType)
        argumentTypes.forEach {
            println(it)
        }
        argumentsAndReturnSizes.let{
            //参数大小
            println(it shr 2)
            //返回值大小
            println(it and 0x03)
        }
    }

    println("================")
    //获取所需slot大小
    println(Type.INT_TYPE.size)
    println(Type.LONG_TYPE.size)
}