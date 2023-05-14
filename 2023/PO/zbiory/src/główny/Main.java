package główny;

import kolekcje.Zbiór;

import java.util.ArrayList;

public class Main {
    public void testuj(){
        Zbiór<String> zbs1 = new Zbiór<>();
        Zbiór<String> zbs2 = new Zbiór<>(new ArrayList<>());
        Zbiór<Integer> zbi = new Zbiór<>();

        System.out.println("zbi = " + zbi);
        System.out.println("zbs1 = " + zbs1);
        System.out.println("zbs2 = " + zbs2);

        String[] tab1 = new String[]{"1", "2", "1",
                "ala", "ola", "ala" };
        Zbiór<String> zb1 = new Zbiór<>();
        for (String s: tab1)
            zb1.wstaw(s);

        String[] tab2 = new String[]{"1", "2", "3",
                "ala", "ela" };
        Zbiór<String> zb2 = new Zbiór<>();
        for (String s: tab2)
            zb2.wstaw(s);

        System.out.println(zb1.przecięcie(zbi));

    }

    public static void main(String[] args) {
        System.out.println("Hello world of sets!");

        Main m = new Main();
        m.testuj();

        System.out.println("End");
    }
}