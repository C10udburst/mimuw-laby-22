package dekoracje;

public class WirtualnyPlik extends Plik {

    private final StringBuilder zawartość = new StringBuilder();

    @Override
    public void wypiszWiersz(String wiersz) {
        zawartość.append(wiersz).append("\n");
        super.wypiszWiersz(wiersz);
    }

    public String pobierzZawartość() {
        return zawartość.toString();
    }
}
