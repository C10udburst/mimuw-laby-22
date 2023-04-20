package główny;

// Zakładamy, że wyrażenia nie zawierają żadnych separatorów,
// a liczby są jednoznakowe (nieujemne).

import pojemniki.BrakMiejsca;
import pojemniki.ZłyRozmiar;
import wyrażenia.NiepoprawneWyrażenie;
import wyrażenia.ONP;

import java.util.Scanner;

public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    public static void main(String[] args) {
        ONP onp = new ONP();
        Scanner sc = new Scanner(System.in);
        System.out.println("Podaj wyrażenie w ONP");
        String dane = sc.nextLine();
        while(!dane.equals("")) {
            try {
                System.out.println(onp.policzWartość(dane, 5));
                System.out.println("Podaj wyrażenie w ONP");
                dane = sc.nextLine();
            } catch (NiepoprawneWyrażenie niepoprawneWyrażenie) {
                System.out.println(ANSI_RED+"Niepoprawne wyrażenie"+ANSI_RESET);
                System.exit(0xA);
            } catch (BrakMiejsca e) {
                System.out.println(ANSI_RED+e.getLocalizedMessage()+ANSI_RESET);
                System.exit(0xB);
            } catch (Exception e) {
                System.out.println(ANSI_RED+"Nieznany błąd: "+e.getLocalizedMessage()+ANSI_RESET);
                System.exit(0xF);
            }
        }
    }
}