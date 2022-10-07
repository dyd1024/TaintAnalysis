package taint.analysis;

public class taintAnalysisDemo3Recursion {
    private static int calls = 0;
    private static int recursions = 0;

    public static void main(String[] args) throws Exception {
//        int[] te = new int[]{1,2,3};
//        te[1] = x;
//        foo(te[1]);
//        int z = te[1];
//        foo(z);

        int x = secret();
        int y = 6;
        System.out.println(y);
        int t = 3;
        y = foo(x) + t;
        System.out.println(y);
        testRecursion(y);
//        testRecursion(x);

    }


    private static int secret() {
        return 8;
    }
    private static int foo(int p){
        return p;
    }

    private static void testRecursion(int num) throws Exception {
        fib(num);
        System.out.println("I made "+calls+" static calls");
        a();
        y();
        CalleeFunction.testCallRecustion();

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
        if(recursions < 5) {
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
