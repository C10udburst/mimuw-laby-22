package p48.cotomoze.przyklad5;

class SprytnaTablica5 {
    String[] tablica;

    public SprytnaTablica5(String... tablica) {
        this.tablica = tablica;
    }

    void coDrugiOd0(KodParam5 m) {
        for (int i = 0; i < tablica.length; i += 2) {
            m.zjedz(tablica[i]);
        }
    }

    void coDrugiOd1(KodParam5 m) {
        for (int i = 1; i < tablica.length; i += 2) {
            m.zjedz(tablica[i]);
        }
    }

    void jedneDrugie(KodParam5 m) {
        coDrugiOd0(m);
        coDrugiOd1(m);
    }

}
