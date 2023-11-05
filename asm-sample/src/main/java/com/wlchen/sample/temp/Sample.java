package com.wlchen.sample.temp;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Author cwl
 * @Date 2023/10/28 5:39 PM
 */
public class Sample {
    public static final int const_field = 10;
    private int non_const_field;
    String str = "abc";

    public void test() throws FileNotFoundException, IOException {
        String a = "sdada";
        a += 1000;

    }

}
