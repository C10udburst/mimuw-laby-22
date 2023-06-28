package pojemniki;

// Przyjmujemy, że mamy pojemniki tylko na liczby całkowite

public interface Pojemnik {
    int pobierz() throws PustyPojemnik;
    void wstaw(int elt) throws BrakMiejsca;
    boolean pusty();
}
