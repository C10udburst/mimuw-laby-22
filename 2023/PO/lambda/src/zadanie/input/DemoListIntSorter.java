package zadanie.input;

import java.util.Arrays;
import java.util.List;

public class DemoListIntSorter {

    public static void main(String[] args) {
        List<Integer> lista = Arrays.asList(1,4,2,10,4,6,3,1);
        System.out.println("Początkowo:  "+ lista);
        ListIntSorter.SortowaniePrzezWybieranie(lista, (a,b) -> a-b);
        System.out.println("Posortowane: "+ lista);
    }


}
