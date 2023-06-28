package p48.cotomoze.przyklad7;

// Co to może? Można napisać explicite typ argumentu
// To czasami się przydaje, żeby rozpoznać o który interfejs
// i w związku z tym o którą (np. przeciążoną) metodę chodzi...
// Czasami to nie wystarcza - trzeba podać nazwę interfejsu (ale to przy każdym wyrażeniu tak można - cast).

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

@FunctionalInterface
interface IntZInta {
    int obliczI(int s);
}

@FunctionalInterface
interface IntZeStringa {
    int obliczS(String s);
}

public class Przyklad7 {

    static List<Integer> rób(IntZInta k) {
        List<Integer> l1 = List.of(1, 3, 2, 5, 6);
        List<Integer> l2 = new ArrayList<>(l1.size());
        for (var a : l1) l2.add(k.obliczI(a));
        return l2;
    }

    static List<Integer> rób(IntZeStringa k) {
        List<String> l1 = List.of("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        List<Integer> l2 = new ArrayList<>(l1.size());
        for (var a : l1) l2.add(k.obliczS(a));
        return l2;
    }

    public static void main(String[] args) {
        // var lista1 = rób( x -> 2*x );  // Ambiguous method call.
        var lista1 = rób((int x) -> 2 * x);
        System.out.println(lista1);
        // var lista2 = rób( x -> x.length()+1 );    // Ambiguous method call.
        var lista2 = rób((String x) -> x.length() + 1);
        System.out.println(lista2);
    }
}
/*

















        // var lista3 = rób( (String x) -> x.length()+1 );       // Ambiguous method call.
        var lista3 = rób((IntZCzegos<String>) x -> x.length()+1);  // Można też podać nazwę interfejsu
        System.out.println(lista3);


        ostatnia();
    }

    static List<Integer> rób(IntZCzegos<String> k){
        List<String> l1 = List.of("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        List<Integer> l2 = new ArrayList<>(l1.size());
        for (var a: l1) l2.add(k.oblicz(a+a));
        return l2;
    }


    static List<Integer> rób(FunkcjaPodwojna<String, Integer, Integer> k){
        List<String> l1 = List.of("Ala", "Ela", "Jaś", "Józio", "Zuzia");
        List<Integer> l2 = List.of(1, 3, 2, 5, 6);
        int s = min(l1.size(), l2.size());
        List<Integer> l3 = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            l3.add(k.oblicz2(l1.get(i), l2.get(i)));
        }
        return l3;
    }

    private static void ostatnia() {
        var lista4 = rób((s, i) -> s.length() + i);
        System.out.println(lista4);
        lista4 = rób((String s, Integer i) -> s.length() + i);   // tak też oczywiście można
        System.out.println(lista4);
    }

}


@FunctionalInterface
interface IntZCzegos<E> {
    int oblicz(E s);
}













@FunctionalInterface
interface FunkcjaPodwojna<E, F, G> {
    G oblicz2(E e, F f);
}

*/