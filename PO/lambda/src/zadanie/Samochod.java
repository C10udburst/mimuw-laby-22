package zadanie;

public class Samochod {
    final private String marka;
    final private String model;
    final private int liczbaDrzwi;
    final private int cena;

    public Samochod(String marka, String model, int liczbaDrzwi, int cena) {
        this.marka = marka;
        this.model = model;
        this.liczbaDrzwi = liczbaDrzwi;
        this.cena = cena;
    }

    public String getMarka() {
        return marka;
    }

    public String getModel() {
        return model;
    }

    public int getLiczbaDrzwi() {
        return liczbaDrzwi;
    }

    public int getCena() {
        return cena;
    }

    @Override
    public String toString() {
        return marka + ' ' + model + ", drzwi: " + liczbaDrzwi + ", cena: " + cena;
    }


}
