package com.wlchen.sample;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @Author cwl
 * @Date 2023/11/16 3:34 PM
 */
public class LambdaSample {

    public static void main(String[] args) throws Throwable {
        String className = "com.wlchen.sample.Operation";
        String methodName = "operate";

        Class<?> clazz = Class.forName(className);
        Operation operation = (Operation) clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getDeclaredMethod(methodName, String.class, int.class);

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        //指定方法不以反射运行
        MethodHandle methodHandle = lookup.unreflect(method);
        MethodType methodType = methodHandle.type();
        //查找或创建一个返回值为Operator 参数类型为 operate 方法第0个参数的类型 (这里就是Operation)
        MethodType factoryType = MethodType.methodType(Operator.class, methodType.parameterType(0));
        //删掉operate类型
        methodType = methodType.dropParameterTypes(0,1);
        /**
         *  MethodHandles.Lookup caller         方法执行查找上下文
         *  String interfaceMethodName,         要实现的方法名称
         *  MethodType factoryType,             CallSite的预期签名 (String,int)Operator
         *  MethodType interfaceMethodType,     接口方法的类型
         *  MethodHandle implementation,        实现类 执行句柄  其实就是类型匹配的方法
         *  MethodType dynamicMethodType        带泛型的描述类型  没有泛型时与interfaceMethodName 一致
         */
        Operator operator =(Operator) LambdaMetafactory.metafactory(lookup, "toOperate", factoryType, methodType, methodHandle, methodType).getTarget().invokeExact(operation);
        String operate = operator.toOperate("ss", 12);
        System.out.println(operate);
    }

    /**
     * 演示利用MethodHandles.Lookup执行String.valueOf(int)
     * @throws Throwable
     */
    private void methodExecute() throws Throwable{
        MethodType methodType = MethodType.methodType(String.class, int.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.findStatic(String.class, "valueOf", methodType);
        String result = (String) methodHandle.invoke(99);
        System.out.println(result);
    }
}

/**
 * 定义函数式接口，用lambda调用
 */
@FunctionalInterface
interface Operator {
    /**
     * 入参和返回值应该和被lambda调用的方法一致
     *
     * @param str
     * @param value
     * @return
     */
    String toOperate(String str, int value);
}

/**
 * 被lambda调用的类和方法
 */
class Operation {
    public String operate(String str, int value) {
        return str + value;
    }
}
