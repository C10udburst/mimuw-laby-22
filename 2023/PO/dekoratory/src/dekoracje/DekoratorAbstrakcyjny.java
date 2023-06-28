package dekoracje;

public abstract class DekoratorAbstrakcyjny extends Plik {
    protected Plik plik;

    public DekoratorAbstrakcyjny(Plik plik) {
        this.plik = plik;
    }

    @Override
    public int ileWypisanoWierszy(){
        return plik.ileWypisanoWierszy();
    }

    @Override
    public int ileWypisanoZnaków(){
        return plik.ileWypisanoZnaków();
    }
}
