package p48.cotomoze.przyklad8;

// Co to może? Może brać parametry i/lub obliczać wynik
// Zwykle służy bardziej przyziemnym celom...


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Przyklad8 {

    public static void main(String[] args) {
        String[] t = new String[]{"Ala", "Ela", "Jaś", "Józio", "Zuzia"};
        Arrays.sort(t, (s1, s2) -> s1.charAt(1) - s2.charAt(1));
        System.out.println(Arrays.toString(t));

        List<String> l = Arrays.asList("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        l.forEach(s -> System.out.println("My name is "+s+", "+s+" Bond."));

        l = new ArrayList<>(l);
        l.removeIf( s -> s.length() < 5 );
        System.out.println(l);

        l.replaceAll( s -> s.toUpperCase() );
        System.out.println(l);

        l.replaceAll(String::toLowerCase);
        System.out.println(l);

        //BTW: Trzy odsłony "method reference"
        l = new ArrayList<>(Arrays.asList("Jaś", "Ala", "Zuzia", "Józio", "Ela"));
        var o = new Przyklad8();
        l.removeIf(o::mniejsze);        // s -> o.mniejsze(s)
        l.forEach(Przyklad8::wypisz);   // s -> Przyklad8.wypisz(s);
        System.out.println();

        l.sort(String::compareTo);      // s1 -> s1.compareTo(s2);

        // a to jest która wersja? :)
        l.forEach(System.out::println);






        KodZTrzech8<Przyklad8, String> k = Przyklad8::duzoParametrow;
        k.rób(o, "ala", 17);            // (obj, s, i) -> obj.duzoParametrow(s, i)



        jedenSzybkiStrumień();
    }



    private final String atrybut = "Ala ma kota";

    private boolean mniejsze(String s){
        return atrybut.compareTo(s) > 0;
    }

    static void wypisz(String s){
        System.out.print(s+", ");
    }


    private void duzoParametrow(String drugi, int trzeci){
        System.out.println(this.atrybut+", "+drugi+", "+trzeci);
    }











    static void jedenSzybkiStrumień(){
        int m = IntStream.range(-10, 10)
                .filter(x -> x % 2 != 0)
                .map(x -> 2*x*x - 10*x +7)
                .max().orElse(0);

        System.out.println(m);
    }


}



