package taint.analysis;

public class taintAnalysisDemo6Expression {

    public static void main(String[] args) throws Exception {


        int x = secret();
        int y = 6;
        int z = x + y;
        int k = foo(z);
        testLoop(k);

//        int[] te = new int[]{1,2,3};
//        int x = secret();
//        te[1] = x;
//        testLoop(te[1]);
    }


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

}