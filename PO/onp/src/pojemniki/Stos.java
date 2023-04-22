package pojemniki;

public class Stos implements Pojemnik{
    // dane
    private int elty[];  // elty[0..wierzchołekStosu-1] to zawartość stosu,
                         // podstawa od indeksu 0
    private int wierzchołekStosu; // Pierwszy wolny element

    // techniczne

    public Stos(int rozmiar) throws ZłyRozmiar {
        if (rozmiar < 0)
            throw new ZłyRozmiar(rozmiar);
        elty = new int[rozmiar];
        wierzchołekStosu = 0;
    }

    @Override
    public String toString(){
        // dylemat: reprezentacja ładna czy dla programisty
        String res = "Stos("+elty.length+";"+wierzchołekStosu+": ";  // Lub StringBuilder
        for(int i=0; i<wierzchołekStosu; i++)
            res += elty[i] + (i<wierzchołekStosu-1?",":"");
        return res + ")";
    }

    // operacje

    @Override
    public int pobierz() throws PustyPojemnik {
        if(pusty())
            throw new PustyPojemnik();

        return elty[--wierzchołekStosu];
    }

    @Override
    public void wstaw(int elt) throws BrakMiejsca {
        if(wierzchołekStosu >= elty.length)
            throw new BrakMiejsca("Wykorzystano cały rozmiar stosu " +
                                  elty.length);

        elty[wierzchołekStosu++] = elt;
    }

    @Override
    public boolean pusty() {
        return wierzchołekStosu <= 0;
    }
}
