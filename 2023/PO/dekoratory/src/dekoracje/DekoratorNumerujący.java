package dekoracje;

public class DekoratorNumerujący extends DekoratorAbstrakcyjny{

    public DekoratorNumerujący(Plik plik) {
        super(plik);
    }

    @Override
    public void wypiszWiersz(String wiersz) {
        plik.wypiszWiersz((plik.ileWypisanoWierszy() + 1) + ": " + wiersz);
    }
}
