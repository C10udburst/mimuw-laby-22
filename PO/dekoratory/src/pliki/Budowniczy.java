package pliki;

public class Budowniczy {
    // Dane
    Parametry parametry;

    // Technicze

    public Budowniczy(String ścieżka) {
        parametry = new Parametry(ścieżka);
    }

    @Override
    public String toString() {
        return "Budowniczy{parametry=" + parametry + '}';
    }

// Operacje
    public Budowniczy czyNumerować(boolean czyNum){
        parametry.setCzyNumerować(czyNum);
        return this;
    }

    public Budowniczy ileZaTabulacje(int i){
        parametry.setIleZaTabulacje(i);
        return this;
    }

    public Budowniczy coZaTabulacje(char c) {
        parametry.setCoZaTabulacje(c);
        return this;
    }

    public Budowniczy czyOdwracaćWiersze(boolean b) {
        parametry.setCzyOdwracaćWiersze(b);
        return this;
    }

    public Budowniczy kodowanie(String s){
        parametry.setKodowanie(s);
        return this;
    }

    private boolean poprawne(){
        // Możemy sprawdzić poprawność danych
        return parametry.getCoZaTabulacje() == ' ' || parametry.getIleZaTabulacje() > 0;
    }

    public Parametry produkuj() throws Exception {
        if(poprawne())
            return parametry;
        else
            throw new Exception("Niepoprawne parametry" + parametry);
    }

}
