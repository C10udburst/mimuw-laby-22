package p48.cotomoze.przyklad6;

// Co to może? Może brać parametry i/lub obliczać wynik

// Np kolejność "przechodzenia" zależy od wyniku operacji na poprzednich elementach

// Tego już zupełnie nie da się zrobić iteratorem :)
// Poza tym kontrola w trakcie chodzenia po strukturze pozostaje w kodzie struktury - a nie po stronie wołającej

public class Przyklad6 {

    public static void main(String[] args) {
        SprytniejszaTablica6 t = new SprytniejszaTablica6("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        System.out.println(t.szalonyKonik(s -> {
            return 6-s.length();
        }));

        System.out.println(t.szalonyKonik(s -> 6-s.length()));

        System.out.println(t.szalonyKonik(s -> {
            System.out.println(s);
            return 6-s.length();
        }));
    }
}

