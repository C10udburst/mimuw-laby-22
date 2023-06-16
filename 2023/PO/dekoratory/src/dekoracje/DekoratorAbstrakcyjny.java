package dekoracje;

public abstract class DekoratorAbstrakcyjny extends Plik {
    protected Plik plik;

    public DekoratorAbstrakcyjny(Plik plik) {
        this.plik = plik;
    }
}
