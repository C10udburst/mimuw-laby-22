package pojemniki;

public class ZłyRozmiar extends Exception {
    public ZłyRozmiar(int rozmiar) {
        super("Błędny rozmiar w konstruktorze: " + rozmiar);
    }
}
