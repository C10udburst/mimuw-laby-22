package zadanie.input;

import zadanie.Samochod;

public class Metody {
    
    public static SamochodPorownywacz porownywaczZIntWyciagacza(IntWyciagacz wyciagacz) {
        return (Samochod s1, Samochod s2) -> {
            int i1 = wyciagacz.wyciagnij(s1);
            int i2 = wyciagacz.wyciagnij(s2);
            return Integer.compare(i1, i2);
        };
    }

    public static <O,W> Porownywacz<O> zamieniacz(Wyciagacz<O, W> wyciagacz, Porownywacz<W> porownywacz) {
        return (O o1, O o2) -> {
            W w1 = wyciagacz.wyciągnij(o1);
            W w2 = wyciagacz.wyciągnij(o2);
            return porownywacz.compare(w1, w2);
        };
    }
}
