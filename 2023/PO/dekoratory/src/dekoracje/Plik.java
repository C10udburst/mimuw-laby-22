package dekoracje;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLOutput;
import java.util.Scanner;

public abstract class Plik {
    // dane
    protected int ileZnaków = 0;
    protected int ileWierszy = 0;

    // techniczne

    // operacje
    public void wypiszZPliku(String ścieżkaWy){
        try(Scanner sk = new Scanner(new File(ścieżkaWy))){
            while(sk.hasNextLine()){
                wypiszWiersz(sk.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Nie udało się skopiować pliku " + ścieżkaWy);
        }

    }
    public void wypiszWiersz(String wiersz) {
        ileWierszy++;
        ileZnaków += wiersz.length();
    }
    public  int ileWypisanoWierszy(){
        return ileWierszy;
    }
    public int ileWypisanoZnaków(){
        return ileZnaków;
    }
}
