package p23.cotojest.przyklad3;

// Co to jest? Wersja trochę skrócona
// (dobra też dla interfejsów z kilkoma metodami)
// i innych klas (np. abstrakcyjnych), których
// podklasę (i obiekt) tej podklasy chcemy stworzyć do
// "jednorazowego" użycia; albo jak potrzebujemy lokalnego atrybutu
// (np. tester z licznikiem ile razy go używano)
// Szukać hasła: anonymous inner classes
// Ale lambdy są zapisywane krócej i implementowane bardziej efektywnie

public class Przyklad3 {
    static void testowanaMetoda(int a, int b){
        int x = a/b;
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        boolean b = Testowanie3.rzucaCoś(new JakisKod3() {
            @Override
            public void rób() {
                testowanaMetoda(1, 0);
            }
        });
        System.out.println(b);
















        if (true) return;

        System.out.println("Dygresja...");
        JakisKod3 k = new JakisKod3() {   // jeśli JakisKod3 to klasa, to w () można przekazać parametr jej konstruktorowi!
            int licznik = 0;    // tego się lambdą nie zrobi!
            @Override
            public void rób() {
                if (licznik++ > 0) System.out.println("JESZCZE RAZ ????");
                testowanaMetoda(1, 0);
            }
        };

        System.out.println("Pierwszy raz: " + Testowanie3.rzucaCoś(k));  // pierwszy raz
        System.out.println("Drugi raz:    " + Testowanie3.rzucaCoś(k));  // drugi raz
    }
}

