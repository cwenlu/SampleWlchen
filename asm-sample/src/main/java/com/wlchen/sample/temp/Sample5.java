package com.wlchen.sample.temp;

import java.util.Random;

/**
 * @Author cwl
 * @Date 2023/10/31 8:58 PM
 */
public class Sample5 {

    public Sample5(long idCard, float tag, String name) {

    }

    public String dumpFun(boolean a, long b) {
        String ret = "";
        ret += a;
        ret += b;
        return ret;
    }

    public boolean dumpFunByAdvice() {
        int v = new Random().nextInt(100);
        if (v > 50) {
            throw new RuntimeException("v > 50");
        }
        return (v % 2) == 0;
    }

}
