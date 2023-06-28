package p48.cotomoze.przyklad6;

class SprytniejszaTablica6 {
    String[] tablica;

    public SprytniejszaTablica6(String... tablica) {
        this.tablica = tablica;
    }

    int szalonyKonik(KodParamWynik6 m) {
        int ile = 0;
        int i = 0;
        while (0 <= i && i < tablica.length) {
            i += m.oblicz(tablica[i]);
            ile++;
        }
        return ile;
    }

}
