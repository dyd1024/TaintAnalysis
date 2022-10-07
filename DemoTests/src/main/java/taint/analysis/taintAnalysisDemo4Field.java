package taint.analysis;

public class taintAnalysisDemo4Field {
    private static int calls = 0;
    private static int recursions = 0;

    public static void main(String[] args) throws Exception {

        A test = new A();
        test.a = 2;
        System.out.println(test.a);
        int x = secret();
        int y = foo(x);
        test.b = y;

        System.out.println(y);
        testLoop(test.b);

    }

    static class A{
        int a = 1;
        int b = 2;
    }

    private static int secret() {
        return 8;
    }
    private static int foo(int p){
        return p;
    }

    private static void testLoop(int num) {
        // num is taint
        int z = 0;
        for(int l = 0; l < num; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }
        CalleeFunction.testCallLoop();
    }

}
