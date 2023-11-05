package com.wlchen.sample.temp;

/**
 * @Author cwl
 * @Date 2023/10/30 11:35 AM
 */
public class Sample3 {
    public String testIf(boolean visible) {
        String ret;
        if (visible) {
            ret = "visible";
        } else {
            ret = "gone";
        }
        return ret;
    }

    private void testSwitch(int key) {
        switch (key) {
            case 1:
                System.out.println("key is 1");
                break;
            case 2:
                System.out.println("key is 2");
                break;
            default:
                System.out.println("key is unknown");
        }
    }

    String testFor() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            ret.append(i);
        }
        return ret.toString();
    }

    protected void testTryCatch() {
        try {
            int a = 10;
            int b = 1000;
            int ret = a + b;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
