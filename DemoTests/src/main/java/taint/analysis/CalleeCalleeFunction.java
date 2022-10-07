package taint.analysis;

public class CalleeCalleeFunction {
    public static int calls = 0;

    static void testCallBitwiseOperators() {
        int x = 1;
        int y = 2;
        System.out.println(x | y);
    }

    static void testCallRecustion() throws Exception {
        fib(7);
        System.out.println("I made "+calls+" static calls");
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

    public static void testCallLoop() {
        int m = 0;
        int z = 0;
        do{
            z+=m;
            m++;
        }while(m < 7);

        if(z > 25) {
            System.out.println("the res = " + z);
        }

    }
}