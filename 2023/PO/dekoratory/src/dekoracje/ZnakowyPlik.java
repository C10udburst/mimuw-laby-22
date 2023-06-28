package dekoracje;

public class ZnakowyPlik extends Plik {
    @Override
    public void wypiszWiersz(String wiersz) {
        ileWierszy++;
        ileZnaków += wiersz.length();
    }
}
