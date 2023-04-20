package wyrażenia;

import pojemniki.BrakMiejsca;
import pojemniki.PustyPojemnik;
import pojemniki.Stos;
import pojemniki.ZłyRozmiar;

public class ONP {
    public int policzWartość(String wyr, int rozmiar)
               throws ZłyRozmiar, NiepoprawneWyrażenie, BrakMiejsca {
        // Zakładam, że wyr zawiera tylko pojedyncze cyfry (jako argumenty) i operatory +,-,*,/.

        Stos stos = new Stos(rozmiar);

        try {
            for(int i = 0; i<wyr.length(); i++){
                char c = wyr.charAt(i);

                int arg1, arg2;
                switch (c) {  // chcemy użyć switch zagnieżdżone ify pewnie by były lepsze
                    case '+':
                        arg2 = stos.pobierz();
                        arg1 = stos.pobierz();
                        stos.wstaw(arg1 + arg2);
                        break;
                    case '-':
                        arg2 = stos.pobierz();
                        arg1 = stos.pobierz();
                        stos.wstaw(arg1 - arg2);
                        break;
                    case '*':
                        arg2 = stos.pobierz();
                        arg1 = stos.pobierz();
                        stos.wstaw(arg1 * arg2);
                        break;
                    case '/':
                        arg2 = stos.pobierz();
                        if (arg2 == 0)
                           throw new NiepoprawneWyrażenie();
                        arg1 = stos.pobierz();
                        stos.wstaw(arg1 /arg2);
                        break;
                    case '0': case '1': case '2': case '3':
                    case '4': case '5': case '6': case '7':
                    case '8': case '9':
                        // Lepiej by było w default dać if, ale analizujemy wady i większe wady switcha
                        // liczba będąca cyfrą
                        stos.wstaw(c-'0');
                        break;
                    default:
                        throw new NiepoprawneWyrażenie();
                } // switch
            } // for
            int wyn = stos.pobierz();
            if(!stos.pusty())
                throw new NiepoprawneWyrażenie();
            return wyn;
        } // try
        catch (PustyPojemnik w){
                throw new NiepoprawneWyrażenie();
        } //catch

    } // policzWartość
}
