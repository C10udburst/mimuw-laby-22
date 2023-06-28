package p01.ocochodzi.przyklad0;

// Pamiętacie assertThrows z JUnit?

public class Przyklad0 {
    static void testowanaMetoda(int a, int b){
        int x = a/b;
    }

    static int metodaZWynikiem(int a, int b){
        return a/b;
    }

    public static void main(String[] args) {
        boolean b = false;
        //b = Testowanie.rzucaCoś(testowanaMetoda(1, 0)); // problem z typowaniem!
        System.out.println(b);

        b = Testowanie.rzucaCośInt(metodaZWynikiem(1,0));
        System.out.println(b);
    }
}

class Testowanie {
/*
    // Problemy z kompilacją...
    static boolean rzucaCoś(void kod){
        try {
            kod;
            return false;
        } catch (Exception e) {
            return true;
        }
    }
*/

    static boolean rzucaCośInt(int kod){
        try {
            int dummy = kod;
            return false;
        } catch (Exception e) {
            return true;
        }
    }

}
















