package taint.analysis;

public class taintAnalysisDemo5Array {
    private static int calls = 0;
    private static int recursions = 0;

    public static void main(String[] args) throws Exception {
        int[] te = new int[]{1,2,3};
        int x = secret();
        te[1] = x;
        testLoop(te[2]);
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
