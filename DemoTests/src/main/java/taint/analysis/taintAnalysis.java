package taint.analysis;

public class taintAnalysis {

    private static int calls = 0;
    private static int recursions = 0;

    public static void main(String[] args) throws Exception {
        int[] te = new int[]{1,2,3};
        int x = secret();
        te[1] = x;
        int y = 6;
        System.out.println(y);
        foo(te[1]);
        testLoop(y);
        y = foo(x);
        System.out.println(y);
        testRecursion(y);
        y = foo(x);
        int z = te[1];
        foo(z);
//        print(y);
        testBitwiseOperator();

    }

//    private static void print(int y) {
//        System.out.println(y);
//    }

    private static int secret() {
        return 8;
    }
    private static int foo(int p){
        return p;
    }

    private static void testLoop(int num) {
        int z = 0;
        for(int l = 0; l < num; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }
        CalleeFunction.testCallLoop();
    }

    private static void testRecursion(int num) throws Exception {
        fib(num);
        System.out.println("I made "+calls+" static calls");
        a();
        y();
        CalleeFunction.testCallRecustion();

    }

    private static void testBitwiseOperator() {
        //转化为二进制：0101
        int num1 = 5;
        //转化为二进制：1001
        int num2 = 9;
        //与运算，二进制结果为 0001，打印结果为 1
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

    private static void x() {
        recursions++;
        y();
    }

    private static void y() {
        if(recursions < 5) {
            recursions++;
            x();
        }
    }

    private static void a() {
        recursions++;
        b();
    }

    private static void b() {
        recursions++;
        c();
    }

    private static void c() {
        if(recursions < 10) {
            recursions++;
            a();
        }
    }

    public static int fib(int n) throws Exception {
        if (n < 0)
            throw new Exception("参数不能为负！");
        else if (n == 0)
            return n;
        else {
            calls++;
            return fib(n - 1);
        }
    }

}