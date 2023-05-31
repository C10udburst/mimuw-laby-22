package p01.ocochodzi.przyklad1;

public class Przyklad1 {
    static void testowanaMetoda(int a, int b){
        int x = a/b;
    }

    public static void main(String[] args) {
        boolean b = Testowanie1.rzucaCoś( () -> testowanaMetoda(1, 0) );
        System.out.println(b);
    }
}

class Testowanie1 {
    static boolean rzucaCoś(JakisKod1 kod){
        try {
            kod.rób();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

@FunctionalInterface
interface JakisKod1 {
    void rób();
}

