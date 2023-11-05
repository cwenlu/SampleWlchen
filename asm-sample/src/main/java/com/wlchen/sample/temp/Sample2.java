package com.wlchen.sample.temp;

/**
 * @Author cwl
 * @Date 2023/10/29 12:53 PM
 */
public class Sample2 {
    @Tag(name = "cwl", age = 22)
    String stu;

}

@interface Tag {
    String name();

    int age();
}
