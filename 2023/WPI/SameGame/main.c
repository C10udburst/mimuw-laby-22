/**
 * "SameGame"
 *
 * Program wykonujący jedno polecenie usunięcia klocków wydane przez grającego w SameGame.
 * 
 * Program jest wywoływany z dwoma argumentami - współrzędnymi pola wybranego przez gracza.
 * Pierwszy argument to numer wiersza, liczony od 0.
 * Drugi argument to numer kolumny, liczony od 0.
 * Pole z wiersza 0 i kolumny 0 jest w lewym górnym rogu planszy.
 * 
 * Ograniczenia:
 * - Rozmiar tablicy z góry ustalony w trakcie kompilacji
 * 
 * autor: Tomasz Wilkins <tomasz@wilkins.ml>
 * wersja: 1.0.0
 * data: 13 grudnia 2022
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <assert.h>

#ifndef WIERSZE
#define WIERSZE 10 
#endif

#ifndef KOLUMNY
#define KOLUMNY 15
#endif

#ifndef RODZAJE
#define RODZAJE 4
#endif

typedef char Plansza[WIERSZE][KOLUMNY];
const char puste = '.';

/**
 * Funkcja odczytuje pojedynczy znak z wejścia użytkownika dopóki nie jest to prawidłowe wejście dla planszy gry.
 * Poprawnym wejściem jest albo znak spacji, albo cyfra z przedziału od 0 do RODZAJE.
 */
void wczytaj_pole(char *n) {
    char in;
    do {
        in = (char) getchar();
    } while (in != puste && (in < '0' || in > '9' || in >= RODZAJE+'0'));

   *n = in;
}

/**
 * Funkcja wykorzystuje funkcję wczytaj_pole() do odczytania znaków dla każdej komórki planszy gry i przechowuje je w tablicy p.
 */
void wczytaj_plansze(Plansza p) {
    for (int wiersz = 0; wiersz < WIERSZE; wiersz++) {
        for (int kolumna = 0; kolumna < KOLUMNY; kolumna++) {
            wczytaj_pole(&p[wiersz][kolumna]);
        }
    }
}

/**
 * Funkcja wypisuje planszę do gry na ekran
 */
void wypisz_plansze(Plansza p) {
    for (int wiersz = 0; wiersz < WIERSZE; wiersz++) {
        for (int kolumna = 0; kolumna < KOLUMNY; kolumna++) {
            putchar(p[wiersz][kolumna]);
        }
        putchar('\n');
    }
}

/**
 * Funkcja sprawdza czy element na pozycji (x, y) tworzy grupę
 * Robi to poprzez sprawdzenie, czy istnieją jakiekolwiek poziomo lub pionowo sąsiadujące bloki o tym samym kolorze co blok w danej pozycji.
 * Założenia:
 *  - 0 ≤ x < KOLUMNY
 *  - 0 ≤ y < WIERSZE
 */
bool czy_grupa(Plansza p, int x, int y) {
    char curr = p[y][x];
    return (
        // elementy puste nie tworzą grupy
        curr != puste && (
             // warunki poniżej sprawdzają czy któryś z sąsiadów ma ten sam znak
            (x > 0 && p[y][x-1] == curr)
            || (y > 0 && p[y-1][x] == curr)
            || (x < KOLUMNY-1 && p[y][x+1] == curr)
            || (y < WIERSZE-1 && p[y+1][x] == curr)
        )
    );
}

/**
 * Funkcja _usun_grupe jest rekurencyjną pomocniczą funkcją używaną w funkcji usun_grupe.
 * Jej zadaniem jest usunięcie z planszy p wszystkich pól o współrzędnych (x, y)
 * oraz sąsiednich tych pól, które mają wartość symbol.
 * Przyjmuje ona następujące argumenty:
 *  - p: plansza, na której znajdują się pola do usunięcia
 *  - x: współrzędna x pola, od którego rozpocznie się usuwanie
 *  - y: współrzędna y pola, od którego rozpocznie się usuwanie
 *  - symbol: symbol, który oznacza pola do usunięcia
 */
void _usun_grupe(Plansza p, int x, int y, char symbol) {
    if (x >= 0 && y >= 0
     && x < KOLUMNY && y < WIERSZE
     && p[y][x] == symbol) {
        p[y][x] = puste;
        
        _usun_grupe(p, x-1, y  , symbol);
        _usun_grupe(p, x+1, y  , symbol);
        _usun_grupe(p, x,   y-1, symbol);
        _usun_grupe(p, x,   y+1, symbol);
    }
}

/**
 * Funkcja usun_grupe jest funkcją główną do usuwania grup pól z planszy.
 * Jej zadaniem jest wywołanie funkcji _usun_grupe dla wskazanego pola na planszy
 * oraz symbolu, który oznacza pola do usunięcia.
 * Przyjmuje ona następujące argumenty:
 *  - p: plansza, na której znajdują się pola do usunięcia
 *  - x: współrzędna x pola, od którego rozpocznie się usuwanie
 *  - y: współrzędna y pola, od którego rozpocznie się usuwanie
 */
void usun_grupe(Plansza p, int x, int y) {
    if (x >= 0 && y >= 0
     && x < KOLUMNY && y < WIERSZE
     && p[y][x] != puste) {
        _usun_grupe(p, x, y, p[y][x]);
    }
}

/**
 * Funkcja zmienia układ klocków na planszy gry tak, że wszelkie puste miejsca są przesuwane na szczyt planszy gry.
 * Robi to poprzez zapętlenie każdej kolumny planszy gry, zaczynając od dołu i przesuwając wszelkie
 * puste miejsca na dół kolumny poprzez przesunięcie niepustych bloków w dół.
 */
void porzadkuj_poziomo(Plansza p) {
    for (int kolumna = 0; kolumna < KOLUMNY; kolumna++) {
        for (int wiersz = WIERSZE - 1; wiersz >= 0; wiersz--) {
            if (p[wiersz][kolumna] == puste) {
                // Szukamy wiersza nad pustym wierszem, który nie jest pusty
                int wiersz_niepusty = wiersz;
                while(wiersz_niepusty >= 0 && p[wiersz_niepusty][kolumna] == puste)
                    wiersz_niepusty--;
                if (wiersz_niepusty < 0) // sprawdziliśmy wszystkie wiersze, a więc kolumna jest pusta
                    break;
                else { // znaleźliśmy niepusty wiersz, a więc przenosimy jego wartość w dół
                    p[wiersz][kolumna] = p[wiersz_niepusty][kolumna];
                    p[wiersz_niepusty][kolumna] = puste;
                }
            }
        }
    }
}

/**
 * Funkcja sprawdza czy dana kolumna jest pusta
 * Założenia:
 *  - 0 ≤ kolumna < KOLUMNY
 */
bool kolumna_pusta(Plansza p, int kolumna) {
    assert(kolumna >= 0 && kolumna < KOLUMNY);

    for (int wiersz = 0; wiersz < WIERSZE; wiersz++) {
        if (p[wiersz][kolumna] != puste) return false;
    }
    return true;
}

/**
 * Funkcja wstawia wszystkie wartości kolumny z_kol do kolumny do_kol oraz ustawia z_kol na puste wartości
 * Założenia:
 *  - 0 ≤ z_kol < KOLUMNY
 *  - 0 ≤ do_kol < KOLUMNY
 */
void przenies_kolumne(Plansza p, int z_kol, int do_kol) {
    assert(z_kol >= 0 && z_kol < KOLUMNY);
    assert(do_kol >= 0 && do_kol < KOLUMNY);

    for (int wiersz = 0; wiersz < WIERSZE; wiersz++) {
        p[wiersz][do_kol] = p[wiersz][z_kol];
        p[wiersz][z_kol] = puste;
    }
}

/**
 * Funkcja zmienia układ klocków na planszy gry tak, że wszelkie puste kolumny są przesuwane na prawo od planszy gry.
 * Robi to, zapętlając się przez każdą kolumne planszy gry, zaczynając od lewej strony, i przesuwając wszelkie puste kolumny na prawo od kolumny, przesuwając niepuste kolumny w lewo.
 */
void porzadkuj_pionowo(Plansza p) {
    for (int kolumna = 0; kolumna < KOLUMNY; kolumna++) {
        if (kolumna_pusta(p, kolumna)) {
            int kolumna_niepusta = kolumna;
            while(kolumna_niepusta < KOLUMNY && kolumna_pusta(p, kolumna_niepusta))
                kolumna_niepusta++;
            if (kolumna_niepusta >= KOLUMNY) // sprawdziliśmy wszystkie kolumny, a wszystko na prawo jest puste
                return;
            else { // znaleźliśmy niepustą kolumne, a więc przenosimy jej wartość
                przenies_kolumne(p, kolumna_niepusta, kolumna);
            } 
        }
    }
}

/**
 * Funkcja wykonaj_ruch wykonuje ruch w grze SameGame na podanej planszy p.
 * Ruch polega na usunięciu grupy kamieni o tej samej barwie, która znajduje się
 * w kolumnie kolumna i wierszu wiersz. Po usunięciu grupy, plansza jest porządkowana
 * w kierunku poziomym i pionowym, aby umożliwić dalsze ruchy.
 * Założenia:
 *  - 0 ≤ kolumna < KOLUMNY
 *  - 0 ≤ wiersz < WIERSZE
 */
void wykonaj_ruch(Plansza p, int kolumna, int wiersz) {
    if (czy_grupa(p, kolumna, wiersz)) {
        usun_grupe(p, kolumna, wiersz);
        porzadkuj_poziomo(p);
        porzadkuj_pionowo(p);
    }
}

int main(int argc, char *argv[]) {
    assert(argc == 3);

    Plansza p;

    int wiersz = atoi(argv[1]);
    int kolumna = atoi(argv[2]);

    wczytaj_plansze(p);

    wykonaj_ruch(p, kolumna, wiersz);

    wypisz_plansze(p);

    return 0;
}
