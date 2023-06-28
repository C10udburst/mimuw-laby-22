package bst;

public class BST<T extends Comparable<T>> {
    // dane
    //private Węzeł<T> korzeń;
    private Węzeł korzeń;

    // technicze
    public BST(){
        korzeń = null;  // I tak by się wykonało
    }

    @Override
    public String toString(){
        return korzeń == null ? "BST()" :
                                "BST(" + korzeń + ")";
    }

    // operacje
    public void wstaw(T elt){
        if (korzeń == null)
            korzeń = new Węzeł(elt);
        else
            korzeń.wstaw(elt);
    }
        // duplikaty do lewego drzewa
    public boolean czyJest(T elt){
        if (korzeń == null)
            return false;
        else
            return korzeń.czyJest(elt);
    }

    public boolean czyPuste(){
        return korzeń == null;
    }

    public int ileWęzłów(){
        if (korzeń == null)
            return 0;
        else
            return korzeń.ileWęzłów();
    }

    // usuń
    // nie dbamy czy jest zrównoważone

    public void usuń(T elt){
        korzeń.usuń(elt);
    }

}


/*

  if w1
    I1
  else
   if w2
     I2
   else
    if w3
      I3
  i w1, w2, w3 się wykluczają
  if w1
    I1
  if w2
    I2
  if w3
    I3

 */