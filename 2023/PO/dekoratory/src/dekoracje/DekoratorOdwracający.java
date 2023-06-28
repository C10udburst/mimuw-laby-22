package dekoracje;

public class DekoratorOdwracający extends DekoratorAbstrakcyjny {

    public DekoratorOdwracający(Plik plik) {
        super(plik);
    }

    @Override
    public void wypiszWiersz(String wiersz) {
        plik.wypiszWiersz(new StringBuilder(wiersz).reverse().toString());
    }
}
