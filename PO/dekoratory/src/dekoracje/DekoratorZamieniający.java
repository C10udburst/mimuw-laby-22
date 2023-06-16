package dekoracje;

public class DekoratorZamieniający extends DekoratorAbstrakcyjny {
    public final String naCoZamienić;

    public DekoratorZamieniający(Plik plik, String naCoZamienić) {
        super(plik);
        this.naCoZamienić = naCoZamienić;
    }


    @Override
    public void wypiszWiersz(String wiersz) {
        plik.wypiszWiersz(wiersz.replaceAll("\t", naCoZamienić));
    }
}
