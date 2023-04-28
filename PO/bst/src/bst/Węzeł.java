package bst;

public class Węzeł<T extends Comparable<T> > {
    // dane
    T wartość;
    Węzeł<T>  lewe, prawe;

    // techniczne

    public Węzeł(T wartość) {
        this.wartość = wartość;
        this.lewe = this.prawe = null; // I tak by się wykonało
    }

    @Override
    public String toString() {
        return "(" + wartość + "; "
                + (lewe==null?"":lewe) + "; "
                + (prawe==null?"":prawe) + ")";
    }

    // operacje

    public void wstaw(T elt) {
        // wersja iteracyjna jest efektywniejsza pamięciowo
        if (elt.compareTo(wartość) <= 0)
            if (lewe == null)
                lewe = new Węzeł<T>(elt);
            else
                lewe.wstaw(elt);
        else if (prawe == null)
            prawe = new Węzeł<>(elt);
        else
            prawe.wstaw(elt);
    }

    public int ileWęzłów() {
        return 1 +
               (lewe==null?0:lewe.ileWęzłów()) +
               (prawe==null?0:prawe.ileWęzłów());
    }

    public boolean czyJest(T elt) {
        if (elt.compareTo(wartość)==0)
            return true;
        else
            if (elt.compareTo(wartość)<0)
                return lewe!=null && lewe.czyJest(elt);
            else
                return prawe!=null && prawe.czyJest(elt);
    }


}
