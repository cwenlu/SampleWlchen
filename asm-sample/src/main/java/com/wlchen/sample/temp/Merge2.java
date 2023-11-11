package com.wlchen.sample.temp;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author cwl
 * @Date 2023/11/11 9:25 AM
 */
public class Merge2 implements Comparable<Integer> {
    static {
        System.out.println("this is Merge1 static block");
    }

    private int sort;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public int compareTo(@NotNull Integer o) {
        return sort - o;
    }

    public void printDate() {
        Date now = new Date();
        String str = df.format(now);
        System.out.println(str);
    }
}
