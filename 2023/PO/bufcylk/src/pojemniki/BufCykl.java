package pojemniki;

import java.util.Arrays;

public class BufCykl{
    // dane
    int[] dane;
    int pocz, dł;

    // tech.

    public BufCykl(int n) {
        assert n > 0 : "Zły rozmiar tablicy w konstr. n = " + n;

        this.dane = new int[n];
        this.pocz = 0;
        this.dł = 0;
    }

    @Override
    public String toString() {
        return "BufCykl{" +
                "dane=" + Arrays.toString(dane) +
                ", pocz=" + pocz +
                ", dł=" + dł +
                '}';
    }

    // usługi
    public void wstawK(int m){ // wstaw na koniec
        // Czy jest miejsce?
        if(dł >= dane.length){
            // tworzymy nową tablicę
            int[] nowa = new int[2*dł];
            for(int i = 0; i < dł; i++){
                nowa[i] = dane[(pocz+i)%dane.length];
            }
            pocz = 0;
            dane = nowa;
        }

        // Jest miejsce, wstawiamy
        dane[(pocz+dł) % dane.length] = m;
        dł++;
    }


    /*
     * Metoda wstawia element na początek bufora
     * @param i element do wstawienia
     */
    public void wstawP(int i) { // wstaw na początek
        // Czy jest miejsce?
        if(dł >= dane.length){
            // tworzymy nową tablicę
            int[] nowa = new int[2*dł];
            for(int j = 0; j < dł; j++)
                nowa[j] = dane[(pocz+j)%dane.length];
            pocz = 0;
            dane = nowa;
        }

        // Jest miejsce, wstawiamy
        pocz = (pocz - 1 + dane.length) % dane.length; // przesuwamy początek, upewniając się, że jest on dodatni i mniejszy od długości tablicy
        dane[pocz] = i;
        dł++;
    }

    /*
     * Metoda usuwa element z początku bufora
     * i zwraca go
     */
    public int pobierzP() { // pobierz z początku
        if (dł == 0)
            throw new RuntimeException("Bufor jest pusty");
        int wynik = dane[pocz];
        pocz = (pocz + 1) % dane.length; // przesuwamy początek
        dł--;
        return wynik;
    }

    /*
     * Metoda usuwa element z końca bufora
     * i zwraca go
     */
    public int pobierzK() { // pobierz z końca
        if (dł == 0)
            throw new RuntimeException("Bufor jest pusty");
        int wynik = dane[(pocz+dł-1) % dane.length]; // ostatni element to ten o indeksie początku + długość - 1
        dł--;
        return wynik;
    }
}
