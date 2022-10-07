package taint.analysis;

public class taintAnalysisForParaKill {
    public static void main(String[] args) throws Exception {


        int x = secret();

        A test = new A();
        test.a = x;
        testLoop(test);
        testLoop2(test.a);

//        int[] te = new int[]{1,2,3};
//        te[1] = x;
//        testLoop3(te);
//        testLoop4(te[1]);
    }

    private static void testLoop(A num) {
        int z = 0;
        for(int l = 0; l < num.a; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }

        num.a = 5;
        System.out.println(num);
    }

    private static void testLoop2(int num2) {
        int z = 0;
        for(int l = 0; l < num2; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }

        System.out.println(num2);

    }


    private static void testLoop4(int num4) {
        int z = 0;
        for(int l = 0; l < num4; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }

        System.out.println(num4);
    }

    private static void testLoop3(int[] num3) {
        int z = 0;
        for(int l = 0; l < num3[1]; l++) {
            z+=l;
        }
        if(z > 5) {
            System.out.println("the res = " + z);
        }

        num3[1] = 5;
        System.out.println(num3);
    }

    static class A{
        int a = 1;
        int b = 2;
    }



    private static int secret() {
        return 8;
    }



}