package taint.analysis;

public class CalleeFunction {
    public static int calls = 0;

    static void testCallBitwiseOperators() {
        int x = 1;
        int y = 2;
        System.out.println(x & y);
        CalleeCalleeFunction.testCallBitwiseOperators();
    }

    static void testCallRecustion() throws Exception {
        fib(5);
        System.out.println("I made "+calls+" static calls");
        CalleeCalleeFunction.testCallRecustion();
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

    static void testCallLoop() {

        int z = 0;
        int m = 0;
        while(m < 5) {
            m++;
            z+=m;
        }

        if(z > 15) {
            System.out.println("the res = " + z);
        }

        CalleeCalleeFunction.testCallLoop();

    }
}