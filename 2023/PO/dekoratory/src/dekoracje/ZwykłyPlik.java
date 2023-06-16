package dekoracje;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ZwykłyPlik extends Plik implements Closeable {
    // dane
    PrintWriter plik;
    String nazwaWy;

    // techniczne

    public ZwykłyPlik(String nazwaWy) throws IOException {
        this.nazwaWy = nazwaWy;
        this.plik = new PrintWriter(nazwaWy, StandardCharsets.UTF_8);
    }

    @Override
    public void wypiszWiersz(String wiersz) {
        plik.println(wiersz);
        super.wypiszWiersz(wiersz);
    }

    @Override
    public void close() throws IOException {
      plik.close();
    }
}
