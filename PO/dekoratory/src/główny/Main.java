package główny;

import pliki.Budowniczy;
import pliki.Plikownik;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hej Bob!");

        Plikownik plikownik = new Plikownik("test.txt");
       // public void wypisz(String ścieżkaWy, boolean czyNumerować,
       // int ileZaTabulacje, char coZaTabulacje,
       // boolean czyOdwracaćWiersze, String kodowanie) throws FileNotFoundException, UnsupportedEncodingException


        // tylko kopia
        plikownik.wypisz("wyn1.txt", false, -1,
                      ' ', false, "UTF-8");

        // numerowanie
        plikownik.wypisz("wyn2.txt", true, -1,
                      ' ', false, "UTF-8");

        // Wersja z budowniczym

        // tylko kopia
        plikownik.wypisz(new Budowniczy("wyn3.txt").produkuj());

        // odwracam wiersze
        plikownik.wypisz(new Budowniczy("wyn4.txt").czyNumerować(true).produkuj());

        System.out.println("Koniec");
    }
}