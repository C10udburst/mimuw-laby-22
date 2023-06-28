package p23.cotojest.przyklad2;

// Co to jest? Wersja pełna

public class Przyklad2 {
    static void testowanaMetoda(int a, int b){
        int x = a/b;
    }

    private static class MojKod implements JakisKod2 {
        @Override
        public void rób() {
            testowanaMetoda(1, 0);
        }
    }

    public static void main(String[] args) {
        boolean b = Testowanie2.rzucaCoś(new MojKod());
        System.out.println(b);
    }
}

