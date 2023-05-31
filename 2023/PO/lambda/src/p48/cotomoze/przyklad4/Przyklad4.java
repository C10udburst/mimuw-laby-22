package p48.cotomoze.przyklad4;

// Co to może? Więcej kodu, na zmienną, operacje...

public class Przyklad4 {
    static void testowanaMetoda(int a, int b){
        int x = a/b;
    }

    public static void main(String[] args) {
        int a = 1;
        boolean b = Testowanie4.rzucaCoś(() -> testowanaMetoda(a, 0));
        System.out.println(b);


































        /*
        // Dygresja: wartości zmiennej w lambda-wyrażeniu nie można zmieniać: 
        // to się nie skompiluje:
        b = Testowanie4.rzucaCoś(() -> testowanaMetoda(++a, 0));
        System.out.println(b);
        */
        
        /* 
        // Ani nawet to:
        a = 2;
        b = Testowanie4.rzucaCoś(() -> testowanaMetoda(a, 0));
        System.out.println(b);
        */

        // Java pilnuje, żeby a było "final or effectively final"

        // ale jakby a było obiektem, to a.zmienObiekt() byłoby OK :)




















        if (true) return;





















        JakisKod4 k1 = () -> {
            System.out.println("A kuku");
            testowanaMetoda(1, 0);
        };
        boolean b1 = Testowanie4.rzucaCoś(k1);
        System.out.println(b1);


















        if (true) return;















        JakisKod4 k2 = skladajKody(
                    () -> System.out.println("Nie kuka"),
                    k1
            );
        boolean b2 = Testowanie4.rzucaCoś(k2);  // na podobnej zasadzie działa assertAll w JUnit
        System.out.println(b2);
    }

    static JakisKod4 skladajKody(JakisKod4 k1, JakisKod4 k2){
        return () -> {
            k1.rób();
            System.out.println("a drugi?");
            k2.rób();
        };  // tu mogłaby też być np. pętla po JakisKod[] tablica
    }

}

