package pliki;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class Parametry {
    // Dane
    private String ścieżkaWy;
    private boolean czyNumerować;
    private int ileZaTabulacje;
    private char coZaTabulacje;
    private boolean czyOdwracaćWiersze;
    private String kodowanie;


    // Techniczne

    public Parametry(String ścieżkaWy) {
        this.ścieżkaWy = ścieżkaWy; // parametr jest obowiązkowy
        // Wartości domyślne
        czyNumerować = false;
        ileZaTabulacje = 4;
        coZaTabulacje = ' ';
        czyOdwracaćWiersze = false;
        kodowanie = "UTF-8";
    }

    @Override
    public String toString() {
        return "Parametry{" +
                "ścieżkaWy='" + ścieżkaWy + '\'' +
                ", czyNumerować=" + czyNumerować +
                ", ileZaTabulacje=" + ileZaTabulacje +
                ", coZaTabulacje=" + coZaTabulacje +
                ", czyOdwracaćWiersze=" + czyOdwracaćWiersze +
                ", kodowanie='" + kodowanie + '\'' +
                '}';
    }

    // Operacje

    public String getŚcieżkaWy() {
        return ścieżkaWy;
    }

    public void setŚcieżkaWy(String ścieżkaWy) {
        this.ścieżkaWy = ścieżkaWy;
    }

    public boolean isCzyNumerować() {
        return czyNumerować;
    }

    public void setCzyNumerować(boolean czyNumerować) {
        this.czyNumerować = czyNumerować;
    }

    public int getIleZaTabulacje() {
        return ileZaTabulacje;
    }

    public void setIleZaTabulacje(int ileZaTabulacje) {
        this.ileZaTabulacje = ileZaTabulacje;
    }

    public char getCoZaTabulacje() {
        return coZaTabulacje;
    }

    public void setCoZaTabulacje(char coZaTabulacje) {
        this.coZaTabulacje = coZaTabulacje;
    }

    public boolean isCzyOdwracaćWiersze() {
        return czyOdwracaćWiersze;
    }

    public void setCzyOdwracaćWiersze(boolean czyOdwracaćWiersze) {
        this.czyOdwracaćWiersze = czyOdwracaćWiersze;
    }

    public String getKodowanie() {
        return kodowanie;
    }

    public void setKodowanie(String kodowanie) {
        this.kodowanie = kodowanie;
    }
}
