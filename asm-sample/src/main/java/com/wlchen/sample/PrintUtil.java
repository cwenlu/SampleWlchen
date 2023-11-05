package com.wlchen.sample;

/**
 * @Author cwl
 * @Date 2023/11/3 7:29 PM
 */
public class PrintUtil {
    public static void printObj(Object value) {
        if (value == null) {
            System.out.println("null");
        } else if (value instanceof Long) {
            System.out.println(((Long) value).longValue());
        } else if (value instanceof Float) {
            System.out.println(((Float) value).floatValue());
        } else if (value instanceof String) {
            System.out.println(value);
        } else {
            System.out.println(value.getClass() + "----" + value);
        }
    }
}
