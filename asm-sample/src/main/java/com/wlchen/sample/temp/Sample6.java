package com.wlchen.sample.temp;

/**
 * @Author cwl
 * @Date 2023/11/3 9:47 AM
 */
public class Sample6 {
    public void findCallMethod(int a, int b) {
        int c = Math.addExact(a, b);
        String ret = String.format("%d + %d = $d", a, b, c);
        System.out.println(ret);
    }
}
