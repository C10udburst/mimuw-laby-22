package zadanie.input;

import java.util.List;

import zadanie.Samochod;

public class ListSorter {

    public static void SortowaniePrzezWybieranie(List<Samochod> tab, SamochodPorownywacz por){
        for (int i = 0; i < tab.size()-1; i++) {
            // niezmiennik: od 0 do i-1 jest posortowane
            // wybieramy najmniejszy element spośród i..len-1
            // i stawiamy go na pozycji i (zamieniając)
            int i_min = i;
            for(int j = i+1; j < tab.size(); j++) {
                if (por.compare(tab.get(j), tab.get(i_min)) < 0) i_min = j;
            }
            if (i_min != i){ // zamieniamy jeśli trzeba
                Samochod tmp = tab.get(i);
                tab.set(i, tab.get(i_min));
                tab.set(i_min, tmp);
            }
        }
    }

    public static void SortowaniePrzezWybieranie(List<Samochod> tab, Porownywacz<Samochod> por){
        for (int i = 0; i < tab.size()-1; i++) {
            // niezmiennik: od 0 do i-1 jest posortowane
            // wybieramy najmniejszy element spośród i..len-1
            // i stawiamy go na pozycji i (zamieniając)
            int i_min = i;
            for(int j = i+1; j < tab.size(); j++) {
                if (por.compare(tab.get(j), tab.get(i_min)) < 0) i_min = j;
            }
            if (i_min != i){ // zamieniamy jeśli trzeba
                Samochod tmp = tab.get(i);
                tab.set(i, tab.get(i_min));
                tab.set(i_min, tmp);
            }
        }
    }
}
