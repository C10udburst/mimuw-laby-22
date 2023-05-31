package zadanie.input;

import java.util.Arrays;
import java.util.List;

public class ListIntSorter {

    public static void SortowaniePrzezWybieranie(List<Integer> tab, IntPorownywacz por){
        for (int i = 0; i < tab.size()-1; i++) {
            // niezmiennik: od 0 do i-1 jest posortowane
            // wybieramy najmniejszy element spośród i..len-1
            // i stawiamy go na pozycji i (zamieniając)
            int i_min = i;
            for(int j = i+1; j < tab.size(); j++) {
                if (por.compare(tab.get(j), tab.get(i_min)) < 0) i_min = j;
            }
            if (i_min != i){ // zamieniamy jeśli trzeba
                int tmp = tab.get(i);
                tab.set(i, tab.get(i_min));
                tab.set(i_min, tmp);
            }
        }
    }
}
