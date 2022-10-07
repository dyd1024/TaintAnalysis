package taint.analysis;

public class taintAnalysisDemo2Loop {

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
        testLoop(y);
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
