package pliki;

import java.io.*;
import java.util.Scanner;

public class Plikownik {
  // dane
  private String ścieżka;

  // techniczne
  public Plikownik(String ścieżka){
    this.ścieżka = ścieżka;  // Nie wykonujemy operacji we/wy w konstruktorze
  }

  @Override
  public String toString(){
    return "Plikownik("+ ścieżka + ")";
  }

  // operacje
  public int ileWierszy() throws FileNotFoundException {  // lub long lub Integer
    try(Scanner sk = new Scanner(new File(ścieżka), "UTF-8")){
      int wynik = 0;
      while(sk.hasNextLine()){
          wynik++;
          sk.nextLine();
      }
      return wynik;
    }
  }

  public int ileZnaków() throws FileNotFoundException {  // lub long lub Integer
    try(Scanner sk = new Scanner(new File(ścieżka),"UTF-8")){
      int wynik = 0;
      while(sk.hasNextLine())
        wynik += sk.nextLine().length();
      return wynik;
    }
  }

  public void wypisz(String ścieżkaWy, boolean czyNumerować,
                     int ileZaTabulacje, char coZaTabulacje,
                     boolean czyOdwracaćWiersze, String kodowanie) throws FileNotFoundException, UnsupportedEncodingException {
    try(Scanner sk = new Scanner(new File(ścieżka), "UTF-8");
        PrintWriter wy = new PrintWriter(ścieżkaWy, kodowanie)){
      int nrWiersza = 0;
      String zaTabulację = "";
      if(ileZaTabulacje>0)
        zaTabulację = (""+coZaTabulacje).repeat(ileZaTabulacje);
      while(sk.hasNextLine()) {
        String wiersz = sk.nextLine();
        nrWiersza++;

        if (ileZaTabulacje >= 0){
          wiersz = wiersz.replace("\t", zaTabulację);
        }

        if (czyOdwracaćWiersze)
          wiersz = new StringBuffer(wiersz).reverse().toString();

        if (czyNumerować)
          wiersz = nrWiersza + ": " + wiersz;

        wy.println(wiersz);
      }
    }  // try

  } //wypisz

    //-------- Wersja z budowniczym: początek ----------------
    public void wypisz(Parametry par) throws FileNotFoundException, UnsupportedEncodingException {
     wypisz(par.getŚcieżkaWy(), par.isCzyNumerować(), par.getIleZaTabulacje(),
            par.getCoZaTabulacje(), par.isCzyOdwracaćWiersze(),
            par.getKodowanie());
    }
    //-------- Wersja z budowniczym: koniec ----------------


    //-------- Wersja z teleskopowym wywoływaniem: początek ----------------
 public void wypisz(String ścieżkaWy, boolean czyNumerować,
                     int ileZaTabulacje, char coZaTabulacje,
                     boolean czyOdwracaćWiersze) throws FileNotFoundException, UnsupportedEncodingException {
    wypisz(ścieżkaWy, czyNumerować, ileZaTabulacje, coZaTabulacje, czyOdwracaćWiersze, "UTF-8");
 }

  public void wypisz(String ścieżkaWy, boolean czyNumerować,
                     int ileZaTabulacje, char coZaTabulacje) throws FileNotFoundException, UnsupportedEncodingException {
    wypisz(ścieżkaWy, czyNumerować, ileZaTabulacje, coZaTabulacje, false);
  }

  public void wypisz(String ścieżkaWy, boolean czyNumerować, int ileZaTabulacje) throws FileNotFoundException, UnsupportedEncodingException {
    wypisz(ścieżkaWy, czyNumerować, ileZaTabulacje, ' ');
  }

  public void wypisz(String ścieżkaWy, boolean czyNumerować) throws FileNotFoundException, UnsupportedEncodingException {
    wypisz(ścieżkaWy, czyNumerować, 4);
  }

  public void wypisz(String ścieżkaWy) throws FileNotFoundException, UnsupportedEncodingException {
    wypisz(ścieżkaWy, false);
  }
        //-------- Wersja z teleskopowym wywoływaniem: koniec----------------

  }
