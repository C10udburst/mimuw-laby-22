package zadanie;

import java.util.Arrays;
import java.util.List;

public class TestujSamochody {

    public static List<Samochod> zróbSamochody() {
        return Arrays.asList(
            new Samochod("Toyota", "Yaris", 5, 76_900),
            new Samochod("Renault", "Clio", 5, 71_900),
            new Samochod("Porsche", "Cayenne", 5, 428_000),
            new Samochod("Tesla", "Model Y", 5, 229_990),
            new Samochod("Renault", "Traffic", 6, 210_150),
            new Samochod("Tesla", "Model 3", 5, 219_990),
            new Samochod("Tesla", "Model X", 5, 592_990),
            new Samochod("Renault", "Kangoo", 5, 100_737),
            new Samochod("Porsche", "Taycan", 5, 448_000),
            new Samochod("Tesla", "Model S", 5, 553_990),
            new Samochod("Toyota", "Aygo", 3, 63_900),
            new Samochod("Toyota", "Corolla", 4, 116_900),
            new Samochod("Hyundai", "Tucson", 5, 127_400),
            new Samochod("Porsche", "911 Turbo", 5, 1_084_000),
            new Samochod("Hyundai", "I10", 5, 52_000)
        );
    }

    static void test2(){
        System.out.print("\nMetoda: "+new Object(){}.getClass().getEnclosingMethod().getName()+"\n--------------");
        List<Samochod> samochody = zróbSamochody();

        System.out.println("\n==> Wg ceny:");
        //ListSorter.SortowaniePrzezWybieranie(samochody,
        //        ...);
        samochody.forEach(System.out::println);

        System.out.println("\n==> Wg liczby:");
        // sortujemy!
        //ListSorter.SortowaniePrzezWybieranie(samochody,
        //        ...);
        samochody.forEach(System.out::println);

        System.out.println("\n==> Wg marki i modelu:");
        // sortujemy!
        //ListSorter.SortowaniePrzezWybieranie(samochody,
        //        ...);
        samochody.forEach(System.out::println);
    }

    public static void main(String[] args) {
        test2();
        //test3();
        //test5();
        //test6();
        //test7();
        //test8();
        //test9();
    }
}

