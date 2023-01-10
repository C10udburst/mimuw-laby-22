/**
 * "GameOfLife"
 *
 * Program symulujący Życie Conway'a. Program wczytuje początkowy stan oraz w pętli wykonuje polecenia od użytkownika.
 *
 * Opcje kompilacji:
 * -D WIERSZE=22 - liczba wierszy
 * -D KOLUMNY=80 - liczba kolumn
 *
 * autor: Tomasz Wilkins <tomasz@wilkins.ml>
 * wersja: 1.0.0
 * data: 13 grudnia 2022
 */

/* Wymagane, abym mógł używać #pragma region */
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunknown-pragmas"

#pragma region Biblioteki

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>

#pragma endregion

#pragma region Stałe

#ifndef WIERSZE
# define WIERSZE 22
#endif

#ifndef KOLUMNY
# define KOLUMNY 80
#endif

/*
    DEBUG=0 - wersja kodu do wysłania
    DEBUG=1 - wyświetlaj martwe, ale zainicjalizowane komórki jako ,
    DEBUG=2 - pokazuj liczbę sąsiadów
    DEBUG=3 - pokazuj adresy komórek jako znaki od / do ~
*/
#define DEBUG 0

const char  alive_sym = '0';
const char   dead_sym = '.';

/* dead1_sym oznacza martwe komórki istniejące w pamięci */
#if DEBUG == 1
const char   dead1_sym = ',';
#else
const char   dead1_sym = '.';
#endif

const char seprarator = '=';

const char exit_command = '.';

/* https://pl.wikipedia.org/wiki/Koniec_linii */
const char CF = 0x0D;
const char LF = 0x0A;

/* Komórki są realokowane o wiele więcej razy, zatem pula dla komórek jest większa */
const size_t cell_pool_sz = 1024;
const size_t row_pool_sz = 256;

#pragma endregion

#pragma region Implementcja puli

typedef struct {
    void** stack;
    size_t end; /* top = end-1 */
    size_t size; /* Rozmiar puli */
} pool;

inline static int pool_empty(pool* p) {
    return p->end < 1;
}

/* Funkcja usuwa top stosu */
void* pool_pop(pool* p) {
    if (pool_empty(p)) return NULL;
    return p->stack[--(p->end)];
}

/* Funkcja dodaje do stosu lub wykonuje free */
void pool_push(pool* p, void* data) {
    if (p->end >= p->size)
        free(data);
    else
        p->stack[p->end++] = data;
}

pool* new_pool(size_t size) {
    pool* p = (pool*)malloc(sizeof(pool));
    p->stack = (void**)calloc(size, sizeof(void*));
    p->end = 0;
    p->size = size;
    return p;
}

void free_pool(pool* p) {
    while (!pool_empty(p)) free(pool_pop(p));
    free(p->stack);
}

/* Funkcja zwraca top ze stosu lub tworzy nowy element, jeśli stos jest pusty */
#define pool_alloc(p, type) (type *)_pool_alloc(p, sizeof(type))
inline static void* _pool_alloc(pool* p, size_t size) {
    if (p == NULL || pool_empty(p)) return malloc(size);
    else return pool_pop(p);
}

#pragma endregion

#pragma region Definicje struktur

typedef struct cell {
    /* informacje na temat komórki */
    bool alive: 1;
    char neighbors: 4; /* pomiędzy 0 a 8 */
    int x;

    /* informacje na temat elementu listy */
    struct cell* next;
} cell;


typedef struct row {
    /* informacje na temat wiersza */
    int y;
    cell* cells; /* lista komórek w wierszu */

    /* informacje na temat elementu listy */
    struct row* next;
} row;

/*
    Globalne założenia do struktury:
    - zarówno wiersze jak i komórki są posortowane ściśle rosnąco
*/
typedef struct {
    row* rows;
    struct { int x; int y; } window; /* Oznacza pozycje początku okna */
    struct {
        pool* rows;
        pool* cells;
    } pools; /* Pule używane do tworzenia nowych elementów list */
} board;

#pragma endregion

#pragma region Obsługa struktur

void init_board(board* b) {
    b->rows = NULL;

    b->window.x = 1;
    b->window.y = 1;

    b->pools.rows = new_pool(row_pool_sz);
    b->pools.cells = new_pool(cell_pool_sz);
}

cell* new_cell(int x, pool* p) {
    cell* ncell = pool_alloc(p, cell);
    ncell->alive = false;
    ncell->neighbors = 0;
    ncell->x = x;
    ncell->next = NULL;
    return ncell;
}

row* new_row(int y, pool* p) {
    row* nrow = pool_alloc(p, row);
    nrow->y = y;
    nrow->cells = NULL;
    nrow->next = NULL;
    return nrow;
}

/* Funkcja usuwa całą zawartość board */
void free_board(board* b) {
    row* curr_row = b->rows;
    while (curr_row != NULL) {
        row* next_row = curr_row->next;
        cell* curr_cell = curr_row->cells;
        while (curr_cell != NULL) {
            cell* next_cell = curr_cell->next;
            free(curr_cell);
            curr_cell = next_cell;
        }
        free(curr_row);
        curr_row = next_row;
    }

    b->rows = NULL;

    free_pool(b->pools.rows);
    free(b->pools.rows);
    free_pool(b->pools.cells);
    free(b->pools.cells);
}

#pragma endregion

#pragma region Tworzenie sąsiadów

/*
    Tworzy komórke, jeśli nie istnieje
    Załozenia:
        - jeśli target != NULL to y==target->null
        - jeśli komórka ma istnieć to musi być target lub jego sąsiadem
*/
cell* row_push_nexist(cell* target, int x, pool* cell_pool) {
    if (target == NULL) {
        return new_cell(x, cell_pool);
    }

    if (target->x == x) return target;

    if (target->x < x) {
        cell* ncell = new_cell(x, cell_pool);

        ncell->next = target->next;
        target->next = ncell;

        return target;
    }

    if (target->x > x) {
        cell* ncell = new_cell(x, cell_pool);

        ncell->next = target;

        return ncell;
    }

    return NULL;
}

/*
    Tworzy sąsiadów (x-1, x, x+1) jeśli nie istnieją
    Założenia:
    - (*prev) != NULL
*/
void row_create_neighbors(cell** prev, int x, pool* cell_pool) {
    cell* curr = (*prev)->next;

    while (curr != NULL && (curr)->x < (x-1)) {
        (*prev) = curr;
        (curr) = (curr)->next;
    }

    if (curr == NULL) { /* Doszliśmy do końca listy */
        (*prev)->next = new_cell(x-1, cell_pool);
        curr = (*prev)->next;
        (*prev)->next->next = new_cell(x, cell_pool);
        (*prev)->next->next->next = new_cell(x+1, cell_pool);
        return;
    }

    if (curr->x == (x-1)) {
        curr->next = row_push_nexist(curr->next, x, cell_pool);
        curr->next->next = row_push_nexist(curr->next->next, x+1, cell_pool);
        return;
    }

    if (curr->x == x) { /* x-1 na pewno nie istnieje */
        cell* ncell = new_cell(x-1, cell_pool);
        ncell->next = curr;
        (*prev)->next = ncell;
        curr->next = row_push_nexist(curr->next, x+1, cell_pool);
        return;
    }

    if (curr->x == x+1) { /* x-1 oraz x na pewno nie istnieją */
        (*prev)->next = new_cell(x-1, cell_pool);
        (*prev)->next->next = new_cell(x, cell_pool);
        (*prev)->next->next->next = curr;
        return;
    }

    if (curr->x > x+1) {/* x-1 oraz x oraz x+1 na pewno nie istnieją */
        (*prev)->next = new_cell(x-1, cell_pool);
        (*prev)->next->next = new_cell(x, cell_pool);
        (*prev)->next->next->next = new_cell(x+1, cell_pool);
        (*prev)->next->next->next->next = curr;
        return;
    }
}

/*
    Funkcja tworzy sąsiadów dla żywych komórek
*/
void create_neighbors(board* b) {
    if (b->rows == NULL) return;

    row rows;
    cell cells, prev_cells, next_cells;

    /* atrapa dla listy wierszy */
    rows.next = b->rows;
    rows.y = b->rows->y - 2;

    row* prev_row = &rows;
    row* curr_row = b->rows;

    cell* prev_cell;
    cell* curr_cell;
    cell* prev_row_el;
    cell* next_row_el;

    while (curr_row != NULL) {
        if (curr_row->cells == NULL) {
            prev_row = curr_row;
            curr_row = curr_row->next;
            continue;
        }

        /* atrapa dla listy komórek w obecnym wierszu */
        cells.next = curr_row->cells;
        cells.x = curr_row->cells->x - 2;
        prev_cell = &cells;

        curr_cell = curr_row->cells;

        /* atrapa dla listy komórek w poprzednim wierszu */
        prev_cells.next = (prev_row->y == curr_row->y-1) ? prev_row->cells : NULL;
        prev_cells.x = curr_cell->x - 2;
        prev_row_el = &prev_cells;

        /* atrapa dla listy komórek w następnym wierszu */
        next_cells.next = (curr_row->next != NULL && curr_row->next->y == curr_row->y+1) ? curr_row->next->cells : NULL;
        next_cells.x = curr_cell->x - 2;
        next_row_el = &next_cells;


        while (curr_cell != NULL) {
            if (curr_cell->alive) {
                row_push_nexist(prev_cell, curr_cell->x-1, b->pools.cells); /* lewo */
                curr_cell->next = row_push_nexist(curr_cell->next, curr_cell->x+1, b->pools.cells); /* prawo */
                row_create_neighbors(&prev_row_el, curr_cell->x, b->pools.cells); /* góra */
                row_create_neighbors(&next_row_el, curr_cell->x, b->pools.cells); /* dół */
            }

            prev_cell = curr_cell;
            curr_cell = curr_cell->next;
        }
        curr_row->cells = cells.next;

        /* aktualizacja poprzedniego wiersza lub tworzenie go jeśli nie istnieje */
        if (prev_row->y == curr_row->y-1)
            prev_row->cells = prev_cells.next;
        else {
            row* nrow = new_row(curr_row->y - 1, b->pools.rows);
            nrow->cells = prev_cells.next;

            nrow->next = prev_row->next;
            prev_row->next = nrow;
        }

        /* aktualizacja następnego wiersza lub tworzenie go jeśli nie istnieje */
        if (curr_row->next != NULL && curr_row->next->y == curr_row->y+1)
            curr_row->next->cells = next_cells.next;
        else if (next_cells.next != NULL) {
            row* nrow = new_row(curr_row->y + 1, b->pools.rows);
            nrow->cells = next_cells.next;

            nrow->next = curr_row->next;
            curr_row->next = nrow;
        }

        prev_row = curr_row;
        curr_row = curr_row->next;
    }

    b->rows = rows.next;
}

#pragma endregion

#pragma region Obliczanie ilości sąsiadów

inline static int abs_v(int a) {
    if (a>=0) return a;
    else return -a;
}
inline static int max(int a, int b) {
    if (a>=b) return a;
    else return b;
}

/* Funkcja zwraca odległość pomiędzy dwoma komórkami. Uwaga: skosy liczą się jako długość 1
    Założenia:
    - a != NULL & b != NULL;
*/
inline static int dist(cell* a, int a_y, cell* b, int b_y) {
    assert(a != NULL && b != NULL);
    return max(abs_v(a->x - b->x), abs_v(a_y - b_y));
}

/* Funkcja znajduje w liście row pierwszy element, którego można uznać za sąsiada, lub NULL gdy go nie znajdzie */
cell* find_neighbor(cell* from, int y_from, cell* curr_row, int curr_y) {
    if (from == NULL) return NULL;
    while (curr_row != NULL) {
        if (dist(from, y_from, curr_row, curr_y) > 1) {
            curr_row = curr_row->next;
        } else {
            return curr_row;
        }
    }
    return NULL;
}

/*
    Funkcja aktualizuje licznik sąsiadów dla komórki target w wierszu row
    Założenia:
    - target != NULL i row != NULL
*/
void calculate_row_neighbors(cell* target, int target_y, cell** curr_row, int curr_y) {
    assert(target != NULL && curr_row != NULL);
    *curr_row = find_neighbor(target, target_y, *curr_row, curr_y);
    assert(*curr_row != NULL);
    cell* curr = *curr_row;

    while (curr != NULL && dist(target, target_y, curr, curr_y) <= 1) {
        curr->neighbors += 1;
        curr = curr->next;
    }
}

/* Funkcja zeruje licznik sąsiadów dla każdej komórki */
void clear_neighbors(board* b) {
    row* curr_row = b->rows;
    while (curr_row != NULL) {
        cell* curr_cell = curr_row->cells;
        while (curr_cell != NULL) {
            curr_cell->neighbors = 0;
            curr_cell = curr_cell->next;
        }
        curr_row = curr_row->next;
    }
}

/*
    Funkcja liczy dla każdej komórki ile ma sąsiadów
    Założenia:
    - każda żywa komórka ma zadeklarowanych sąsiadów w liście, z tego wynika:
        - jeśli tablica nie jest pusta, to ma co najmniej 3 wiersze i 3 kolumny
*/
void calculate_neighbors(board* b) {
    if (b->rows == NULL) return;

    clear_neighbors(b);

    row* prev_row = b->rows; assert(prev_row != NULL);
    row* curr_row = prev_row->next; assert(curr_row != NULL);

    cell* prev_row_el = NULL;

    cell* next_row_el = NULL;

    cell* prev_el = NULL;
    cell* curr_el = NULL;

    while (curr_row->next != NULL) {
        if (curr_row->cells == NULL) {
            prev_row = curr_row;
            curr_row = curr_row->next;
            continue;
        }

        prev_el = curr_row->cells;
        curr_el = prev_el->next;
        prev_row_el = prev_row->cells;
        next_row_el = curr_row->next->cells;

        while (curr_el->next != NULL) {
            if (curr_el->alive) {
                prev_el->neighbors += 1; /* lewo */
                curr_el->next->neighbors += 1; /* prawo */

                calculate_row_neighbors(curr_el, curr_row->y, &prev_row_el, curr_row->y-1); /* góra */
                calculate_row_neighbors(curr_el, curr_row->y, &next_row_el, curr_row->y+1); /* dół */
            }
            prev_el = curr_el;
            curr_el = curr_el->next;
        }
        prev_row = curr_row;
        curr_row = curr_row->next;
    }

}
#pragma endregion

#pragma region Aktualizowanie stanu

/*
    Funkcja przechodzi przez planszę i aktualizuje stany kormórek biorąc pod uwagę tylko ilość sąsiadów stosując domyślne zasady gry w życie Conway'a
*/
void update_state(board* b) {
    row* curr_row = b->rows;
    while (curr_row != NULL) {
        cell* curr_cell = curr_row->cells;
        while (curr_cell != NULL) {
            if (curr_cell->neighbors < 2 || curr_cell->neighbors > 3) curr_cell->alive = false;
            else if (curr_cell->neighbors == 3) curr_cell->alive = true;
            curr_cell = curr_cell->next;
        }
        curr_row = curr_row->next;
    }
}

#pragma endregion

#pragma region Usuwanie komórek bez sąsiadów

/*
    Funkcja usuwa komórki, które są martwe i nie mają żadnego sąsiada
*/
void clear_alone(board* b) {
    if (b->rows == NULL) return;
    row rows;
    rows.y = b->rows->y - 2;
    rows.next = b->rows;

    row* curr_row = b->rows;

    cell cells;
    while (curr_row != NULL) {
        if (curr_row->cells == NULL) {
            curr_row = curr_row->next;
            continue;
        }

        cells.x = curr_row->cells->x - 2;
        cells.next = curr_row->cells;

        cell* curr_cell = curr_row->cells;
        cell* prev_cell = &cells;

        while (curr_cell != NULL) {
            if (!curr_cell->alive && curr_cell->neighbors == 0) {
                prev_cell->next = curr_cell->next;
                pool_push(b->pools.cells, curr_cell); /* Zamiast free wstawiamy pamięć do puli */
                curr_cell = prev_cell->next;
            }
            else {
                prev_cell = curr_cell;
                curr_cell = curr_cell->next;
            }
        }
        curr_row->cells = cells.next;
        curr_row = curr_row->next;
    }
    b->rows = rows.next;
}

/* Funkcja usuwa puste wiersze */
void clear_empty(board* b) {
    if (b->rows == NULL) return;
    row rows;
    rows.y = b->rows->y - 2;
    rows.next = b->rows;

    row* prev_row = &rows;
    row* curr_row = b->rows;

    while (curr_row != NULL) {
        if (curr_row->cells == NULL) {
            prev_row->next = curr_row->next;
            pool_push(b->pools.rows, curr_row); /* Zamiast free wstawiamy pamięć do puli */
            curr_row = prev_row->next;
        }
        else {
            prev_row = curr_row;
            curr_row = curr_row->next;
        }
    }
    b->rows = rows.next;
}

#pragma endregion

#pragma region Wczytywanie początkowego stanu

/* Zwraca true jeśli od znaku c potencjalnie zaczyna się liczba */
inline static bool is_number(int c) {
    return ((c <= '9' && c >= '0') || c=='-');
}

/* Wczytuje znak z stdin nie usuwając go */
inline static int stdin_top() {
    int a = getchar();
    ungetc(a, stdin);
    return a;
}

/* Funkcja zwraca true jeśli znak c jest znakiem białym */
inline static bool is_white(int c) {
    /* https://pl.wikipedia.org/wiki/ASCII#Tabela_kod%C3%B3w_ASCII */
    return (c <= 0x20 || c == 0x7F );
}

/* Funkcja wczytuje i ignoruje wszystkie znaki białe */
void consume_white() {
    int chr;
    do { chr = getchar();
    } while (is_white(chr));
    ungetc(chr, stdin);
}

/* Funkcja wczytuje jeden wiersz ze stdin w formacie:
    /w k1 k2 ... kn

    Zwraca:
    - true jeśli wiersz został wczytany
    - false wpw

    Założenia:
    - kolumny są posortowane
    - prev_row != NULL
    - prev_row->next == NULL
*/
bool read_row(row* prev_row) {
    /* Nie kożystamy tutaj z puli, gdyż wiemy że będzie pusta bo jest to początkowe wczytywanie */
    assert(prev_row != NULL);
    assert(prev_row->next == NULL);
    consume_white();
    bool start = getchar() == '/';
    if (start && is_number(stdin_top())) {
        int y; scanf("%d", &y);
        consume_white();

        if (!is_number(stdin_top())) /* Gdyby ktoś złośliwie wpisał pusty wiersz */
            return true;

        row* curr_row = new_row(y, NULL);
        cell cells; cell* prev_cell = &cells;
        prev_row->next = curr_row;
        while (is_number(stdin_top())) {
            int x; scanf("%d", &x);
            prev_cell->next = new_cell(x, NULL);
            prev_cell = prev_cell->next;
            prev_cell->alive = true;
            consume_white();
        }
        curr_row->cells = cells.next;
        return true;
    }
    return false;
}

/* Funkcja wczytuje początkowe dane z stdin */
void read_init(board* b) {
    row rows; row* last_row = &rows;
    rows.next = NULL;
    while (read_row(last_row)) {
        if (last_row->next != NULL) last_row = last_row->next;
    }

    /* usuwanie ostatniej nowej linii https://pl.wikipedia.org/wiki/Koniec_linii */
    int g = getchar();
    if (g == CF) g = getchar(); /* jeśli CF to trzeba jeszcze usunąć LF,.jeśli ktoś ma MacOS 9, to jego problem  */
    b->rows = rows.next;
}

#pragma endregion

#pragma region Wykonywanie poleceń użytkownika

/* Funkcja liczy stan gry za n generacji */
void tick_n(board* b, int n) {
    while (n--) {
        create_neighbors(b);
        calculate_neighbors(b);
        update_state(b);
        clear_alone(b);
        clear_empty(b);
    }
}

/* Funkcja wypisuje wszystkie żywe komórki w podanym formacie */
void print_alive(board* b) {
    row* r = b->rows;
    while (r != NULL) {
        cell* c = r->cells;

        while (c != NULL && !c->alive) /* Szuka pierwszej żywej komórki w wierszu */
            c = c->next;

        if (c != NULL) { /* Znaleziono takową komórkę */
            printf("/%d", r->y);
            while (c != NULL) {
                if (c->alive) printf(" %d", c->x);
                c = c->next;
            }
            printf("\n");
        }

        r = r->next;
    }
    printf("/\n");
}

/* Wypisuje dany znak n razy */
inline static void printn(int c, int n) {
    while(n--) putchar(c);
}

/* funkcja wypisuje jeden wiersz z okna na stdin
    Założenia:
    - r != NULL
*/ 
void print_window_row(board* b, row* r) {
    cell* c = r->cells;

    while (c != NULL && c->x < b->window.x) /* Szuka pierwszej komórki nie jest wcześniej niż okno */
        c = c->next;

    if (c == NULL) { /* cale okno nie istnieje w pamieci, zatem sa tylko martwe komorki */
        printn(dead_sym, KOLUMNY);
    } else { /* istnieją żywe komórki */
        for (int x = b->window.x; x < b->window.x+KOLUMNY; x++) {
            if (c == NULL || x < c->x) /* komórka nie istnieje, zatem jest martwa */
                putchar(dead_sym);
            else {
                #if DEBUG == 2
                    putchar(c->neighbors + (c->alive ? 'A': 'a'));
                #elif DEBUG == 3
                    putchar((int)((size_t)c) % ('~' - '/') + '/');
                #else
                    putchar(c->alive ? alive_sym : dead1_sym);
                #endif
                c = c->next;
            }
        }
    }
}

/* Funkcja wypisuje okno na stdin */
void print_window(board* b) {
    row* r = b->rows;

    while (r != NULL && r->y < b->window.y) /* Szuka pierwszego wiersza nie jest wcześniej niż okno */
        r = r->next;

    if (r == NULL) { /* cale okno nie istnieje w pamieci, zatem sa tylko martwe komorki */
        for (int y = 0; y < WIERSZE; y++) {
            printn(dead_sym, KOLUMNY);
            printf("\n");
        }
    } else { /* istnieją żywe komórki */
        for (int y = b->window.y; y < b->window.y+WIERSZE; y++) {
            if (r == NULL || y < r->y) /* wiersz nie istnieje, zatem wszystkie martwe */
                printn(dead_sym, KOLUMNY);
            else if (y == r->y) {
                print_window_row(b, r);
                r = r->next;
            }
            printf("\n");
        }
    }

    printn(seprarator, KOLUMNY);
    printf("\n");
}

/* Wykonaj jedno polecenie użytkownika */
void exec_cmd(board* b, char* command) {
    if (command == NULL) return;

    int argv[2];
    int argc = sscanf(command, "%d %d", argv, argv+1);
    if (argc < 1)  /* Pusty wiersz, zatem polecenie jednego kroku */
        tick_n(b, 1);
    else if (argc == 1 && argv[0] == 0) /* polecenie 0, zatem wypisanie żywych komórek */
        print_alive(b);
    else if (argc == 1) /* Jedna liczba, zatem polecenie n-kroków */
        tick_n(b, argv[0]);
    else { /* Dwie liczby, zatem polecenie przesunięcia okna */
        b->window.y = argv[0];
        b->window.x = argv[1];
    }
}

/* Funkcja wczytuje polecenia ze stdin dopóki nie dostanie symbolu kończoncego
    Założenia:
    - Każde polecenie ma mniej niż 512 znaków
    - polecenia są poprawne
*/
void loop_cmd(board* b) {
    char buffer[512];
    while (fgets(buffer, sizeof(buffer), stdin) != NULL) {
        if (buffer[0] == exit_command) return;
        exec_cmd(b, buffer);
        print_window(b);
    }

}

#pragma endregion

int main() {
    board b;

    init_board(&b);

    read_init(&b);

    clear_empty(&b);

    print_window(&b);

    /*
        Założenie, że polecenie ma mniej niż 512 znaków będzie zawsze spełnione, gdyż w specyfikacji napisano, że liczby na wejściu będą mieścić się w int.
        Napisano także że programy będą uruchamiane na serwerze students, zatem int będzie z przedziału -2147483647 do 2147483647. Oznacza to, że najduże polecenie to "-2147483647 -2147483647"
    */
    loop_cmd(&b);

    free_board(&b);
}

#pragma GCC diagnostic pop
