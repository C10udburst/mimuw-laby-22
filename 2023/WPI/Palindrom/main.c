#include <stdio.h>
#include <stdbool.h>

#ifndef WIERSZE
# define WIERSZE 8
#endif

#ifndef KOLUMNY
# define KOLUMNY 8
#endif

#ifndef DLUGOSC
# define DLUGOSC 5
#endif

typedef int Plansza[WIERSZE][KOLUMNY];
typedef const int Kierunek[2];

/* stałe oznaczające kierunki w postaci {x, y} */
Kierunek poziomo = {1, 0};
Kierunek pionowo = {0, 1};
Kierunek skos_nw_es = {1, 1};
Kierunek skos_sw_ne = {1, -1};

const char puste_pole = '-';

/*
    Funkcja wypisuje obecny stan planszy na stdout
    Argumenty:
        p - plansza do wypisania
*/
void wypisz_plansze(Plansza p) {

    // stan
    for (int y = 0; y < WIERSZE; y++) {
        for (int x = 0; x < KOLUMNY; x++)
            printf(" %c", p[y][x]);
        putchar('\n');
    }

    // lista kolumn
    for (char i = 'a'; i < (KOLUMNY + 'a'); i++)
        printf(" %c", i);

    putchar('\n');
}

/*
    Funkcja inicjalizuje plansze (wypełnia ją symbolem 'puste_pole')
    Argumenty:
        p - plansza do inicjalizacji
*/
void inicjalizuj_plansze(Plansza p) {
    for (int y = 0; y < WIERSZE; y++) {
        for (int x = 0; x < KOLUMNY; x++) {
            p[y][x] = puste_pole;
        }
    }
}

/*
    Funkcja wczytuje ruch ze stdin.
    Wczytuje znaki ze stdin dopóki dostanie małej litery lub '.'
*/
void wczytaj_ruch(char *c, int gracz) {
    printf("%d:\n", gracz);

    char tmp;
    do { tmp = (char) getchar(); }
    while (tmp != '.' && (tmp < 'a' || tmp > 'z'));

    *c = tmp;
}

/*
    Funkcja sprawdza czy w punkcie (sx, sy) znajduje się palindrom o długości DLUGOSC w kierunku k
*/
bool palindrom(Plansza p, int sx, int sy, Kierunek k) {
    // jeśli punkt (sx, sy) jest poza tablicą to napewno nie tworzy palindromu
    if (sx < 0 || sx >= KOLUMNY) return false;
    if (sy < 0 || sy >= WIERSZE) return false;

    // wyliczanie ostatniego punktu szukanego palindromu
    int ex = sx + k[0]*(DLUGOSC - 1);
    int ey = sy + k[1]*(DLUGOSC - 1);

    // jeśli punkt (ex, ey) jest poza tablicą to napewno nie tworzy palindromu
    if (ex < 0 || ex >= KOLUMNY) return false;
    if (ey < 0 || ey >= WIERSZE) return false;

    for (int _ = 0; _ < DLUGOSC/2; _++) { //pętla wykonuje się do połowy, gdyż jednocześnie skracam z początku jak i końca
        // Sprawdzam czy pierwszy wyraz jest równy ostatniemu, jeśli tak to sprawdzam czy nie są puste
        if (p[sy][sx] != p[ey][ex] || p[sy][sx] == puste_pole)
            return false;

        sx += k[0]; sy += k[1]; // skracam od początku
        ex -= k[0]; ey -= k[1]; // skracam od końca
    }

    return p[sy][sx] != puste_pole; // sprawdzanie czy środkowy element (w wypadku nieparzystej długości) nie jest pusty
}

/*
    Funkcja sprawdza czy w planszy p znajduje się choć jeden palindrom w kierunku k przechodzący przez (x, y)
*/
bool palindromy(Plansza p, int x, int y, Kierunek k) {
    // sprawdzanie czy (x, y) jest na i-tym miejscu palindromu
    for (int i = 0; i < DLUGOSC; i++) {
        if (palindrom(p, x-k[0]*i, y-k[1]*i, k))
            return true;
    }
    return false;
}

/*
    Funkcja sprawdza czy w planszy p znajduje się choć jeden palindrom przechodzący przez (x, y)
*/
bool jakikolwiek_palindrom(Plansza p, int x, int y) {
    // sprawdzam każdy z kierunków po kolei
    if (palindromy(p, x, y, poziomo))       return true;
    if (palindromy(p, x, y, pionowo))       return true;
    if (palindromy(p, x, y, skos_nw_es))    return true;
    if (palindromy(p, x, y, skos_sw_ne))    return true;

    return false;
}

/*
    Funkcja wykonuje ruch i sprawdza czy palindrom w tablicy
    Argumenty:
        p - obecny stan gry, plansza
        gracz - nr gracza
        ruch - kolumna gdzie wlozono pion
    Założenia:
        - ruch jest poprawny
        - ruch ∈ {a, b, …, z}
    Zwraca:
        true jeśli w planszy jest palindrom po ruchu
        false wpw
*/
bool wykonaj_ruch(Plansza p, int gracz, char ruch) {
    int col = ruch - 'a'; // liczy indeks kolumny

    // szukamy wolnego wiersza
    int row = WIERSZE -1; // zaczynamy od ostatniego (dołu)
    while (p[row][col] != puste_pole)
        row--;

    char sign = (char)gracz + '0'; // liczy symbol oznaczający gracza

    p[row][col] = sign;

    return jakikolwiek_palindrom(p, col, row);
}

/*
    Funkcja na podstawie poprzedniego gracza liczy następnego i ustawia wartość
    Założenia:
        - *gracz ∈ {1, 2}
*/
void wybierz_gracza(int *gracz) {
    switch (*gracz) {
        case 1: *gracz = 2; break;
        case 2: *gracz = 1; break;
    }
}

/*
    Funkcja wykonuje krok gry i sprawdza czy gra się zakończyła i ewentualnie wypisuje zwycięzce
    Argumenty:
        p - obecny stan gry, plansza
        gracz - nr gracza
        ruch - polecenie '.' lub litera oznaczająca kolumne
    Założenia:
        - ruch jest poprawny
        - ruch ∈ {., a, b, … z}
    Zwraca:
        true jeśli gra została zakończona
        false wpw

*/
bool krok_gry(Plansza p, int gracz, char ruch) {
    if (ruch == '.') return true;
    else {
        if(wykonaj_ruch(p, gracz, ruch)) { // true jeśli ruch zakończył gre
            wypisz_plansze(p);
            printf("%d!\n", gracz);
            return true;
        }
    }
    return false;
}

void graj(Plansza p) {
    bool koniec_gry = false; // Czy gra zostala zakończona
    int gracz = 1; // nr gracza który ma nastepny ruch
    char ruch;

    while (!koniec_gry) {
        wypisz_plansze(p);

        // Wczytaj ruch od użytkownika
        wczytaj_ruch(&ruch, gracz);

        koniec_gry = krok_gry(p, gracz, ruch);

        // Wylicz czyj ruch będzie następny
        wybierz_gracza(&gracz);
    }
}

int main(void) {
    Plansza p;  // W zmiennej znajduje się obecny stan gry

    inicjalizuj_plansze(p);

    graj(p);

    return 0;
}
