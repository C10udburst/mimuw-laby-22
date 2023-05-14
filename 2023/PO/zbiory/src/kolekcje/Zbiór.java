package kolekcje;

import java.util.ArrayList;
import java.util.Collection;

public class Zbiór<E> {
  // dane

    // Możliwe implementacje (poza haszowaniem, drzewami, ...)
    // 1) tablica ze wstawianiem na koniec bez sprawdzania istnienia
    // 2) tablica ze wstawianiem ze sprawdzeniem istnienia
    // 3) tablica posortowanych elementów (wymaga odpowiedniego typu elementów)
    // Wybieramy impl. nr 2

    protected Collection<E> elty; // implementacja nr 2

  // techniczne
    public Zbiór(){
        elty = new ArrayList<>();
    }

    public Zbiór(Collection<? extends E> dane) { // i inne możliwości, Zbiór, tablica, element, ...
       elty = new ArrayList<>(dane);
    }

    @Override
    public String toString(){
        StringBuilder repr = new StringBuilder("Zbiór{");
        for(E elt: elty){
            repr.append(elt).append(",");
        }
        if (elty.size() > 0) repr.delete(repr.length()-1, repr.length()); // usuń ostatni przecinek
        repr.append("}");
        return repr.toString();
    }

  // operacje

    public boolean wstaw(E elt){
           if (czyIstnieje(elt))
               return false;
           else{
               elty.add(elt);
               return true;
           }

    }

    public boolean czyIstnieje(Object elt){
    //public boolean czyIstnieje(E elt){
        // 1) E
        // 2) Object jak w Collection (metoda przecięcie wymaga tego)
      return elty.contains(elt);
    }

    public boolean usuń(E elt){
        // 1) usuń z elty.
        // 2) znajdź i zamień z ostatnim.
        return elty.remove(elt);
    }

    public boolean czyPusty(){
      return elty.isEmpty();
    }

    public int rozmiar(){
      return elty.size();
    }

    // maximum (ale wymaga dobrego typu elementów)
    // suma
    //public Zbiór<E> przecięcie(Zbiór<? extends E> zb2){
    public Zbiór<E> przecięcie(Zbiór<? extends Object> zb2){
        // Efektem jest przecięcie this z zb2 (modyfikujemy this).
        // Wynikiem jest this.
        // Przy przecięciu argument może być Zbiorem dowolnych obiektów przy sumie już nie

        // Poniższe generuje java.util.ConcurrentModificationException
        // for (E elt: elty) {
        //    if(!zb2.czyIstnieje(elt)) // Tu wymagamy w czyIstnieje Object
        //        usuń(elt);
        // }

        Collection<E> wynik = new ArrayList<>();
        for (E elt: elty)
            if(zb2.czyIstnieje(elt))
                wynik.add(elt);

        elty = wynik;

        return this;
    }

    public Zbiór<E> różnica(Zbiór<? extends Object> zb2){
        Collection<E> wynik = new ArrayList<>();
        for (E elt: elty)
            if(!zb2.czyIstnieje(elt))
                wynik.add(elt);

        elty = wynik;

        return this;
    }

    public boolean zawiera(Zbiór<? extends Object> zb2){
        for (Object elt: zb2.elty)
            if(!czyIstnieje(elt))
                return false;
        return true;
    }

    public boolean equals(Object obj){
        if (obj instanceof Zbiór<?> zbior) {
            return zawiera(zbior) && zbior.zawiera(this);
        } else return false;
    }

    // (?) dopełnienie o ile mamy skończoną dziedziną
}
