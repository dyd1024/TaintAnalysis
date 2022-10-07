package taint.analysis;

public class taintAnalysisDemo1Bitwise {

    public static void main(String[] args) throws Exception {
//        int[] te = new int[]{1, 2, 3};
//        te[1] = x;
//        foo(te[1]);
//        int z = te[1];
//        foo(z);
//        print(y);

        int x = secret();
        int t = 3;
        int y = foo(x) + t;
        int z = 6;
//        System.out.println(y);
//        y = foo(x);
//        System.out.println(y);
        testBitwiseOperator(y,z);

    }

    private static int secret() {
        return 8;
    }

    private static int foo(int p) {
        int q = p;
        return q;
    }

    private static void testBitwiseOperator(int num1, int num2 ) {
//        //转化为二进制：0101
//        int num1 = 5;
//        //转化为二进制：1001
//        int num2 = 9;
//        与运算，二进制结果为 0001，打印结果为 1

        // num1 is taint
        // num2 is no taint

        System.out.println(num1 & num2);
        //或运算，二进制结果为 1101，打印结果为 13
        System.out.println(num1 | num2);
        //异或运算，二进制结果为 1100，打印结果为 12
        System.out.println(num1 ^ num2);
        //非运算，二进制结果为 11111111111111111111111111111010，打印结果 -6
        System.out.println(~num1);

        //二进制 1111;
        int i = 15;
        //向右边移动两位，二进制结果为 0011，打印结果为 3
        System.out.println(i >> 2);
        //向左边移动两位，二进制结果为 111100，打印结果为 60
        System.out.println(i << 2);

        //无符号右移
        int j = -10;
        System.out.println(j >>> 2);

        CalleeFunction.testCallBitwiseOperators();
    }

}