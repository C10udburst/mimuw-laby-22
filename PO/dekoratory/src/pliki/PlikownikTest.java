package pliki;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class PlikownikTest {

    @Test
    void ileWierszy() throws FileNotFoundException {
        Plikownik plikownik = new Plikownik("test.txt");
        assertEquals(2, plikownik.ileWierszy());
    }

    @Test
    void ileZnaków() throws FileNotFoundException {
        Plikownik plikownik = new Plikownik("test.txt");
        assertEquals(31, plikownik.ileZnaków());
    }

    @Test
    void wypisz() {
    }
}