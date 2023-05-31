package p48.cotomoze.przyklad5;

// Co to może? Może brać parametry
// Ale po co? Żeby być używaną wiele razy z różnymi parametrami, np. z kolekcji.

// Kolekcja zajmuje się "przechodzeniem po sobie" (w być może dziwnej kolejności),
// a lambda-wyrażenie zajmuje się operacją na elementach.

// Różną kolejność przechodzenia można by było osiągnąć za pomocą (sprytnych) iteratorów.
// Ale takiego zgrabnego połączenia coDrugiOd0 i coDrugiOd1 już nie!
// No i składnia byłaby ciężkawa, bo for(var x : lista) działa tylko dla standardowego iterator() )

public class Przyklad5 {

    public static void main(String[] args) {
        SprytnaTablica5 t = new SprytnaTablica5("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        System.out.println("Od 0:");
        t.coDrugiOd0(s -> System.out.println(s));

        System.out.println("Jeszcze raz od 0");
        t.coDrugiOd0(System.out::println);    // skrótowa składnia jak lambda to istniejąca metoda
                                              // przed :: jest klasa lub obiekt
        System.out.println("Od 1:");
        t.coDrugiOd1(s -> System.out.println(s + " " + s.length()));

        System.out.println("Jedne, drugie...");
        t.jedneDrugie(s -> {
            System.out.print("A teraz: ");
            System.out.println(s);}
        );
    }
}

