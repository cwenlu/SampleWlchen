package com.wlchen.sample.temp;

/**
 * @Author cwl
 * @Date 2023/11/11 9:25 AM
 */
public class Merge1 {
    static {
        System.out.println("this is Merge1 static block");
    }

    private String name;
    private int age;

    public Merge1() {
        this("cwl", 27);
    }

    public Merge1(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void dump() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("Merge1 { name=%s, age=%d}", name, age);
    }
}
