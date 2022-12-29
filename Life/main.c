/* Wymagane, abym mógł używać #pragma region */
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunknown-pragmas"

#pragma region Biblioteki
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h> /* memset */
#include <stdbool.h>
#pragma endregion

#pragma region Wektor
/* Ten region zawiera logike dynamicznej tablicy o generycznym typie
    Tablica ta składa się z:
    - nagłówka zawierającego informacje o rozmiarze, pojemności oraz rozmiarze komórki.
    - danych
    Uwagi:
    - Wszystkie funkcje oczekują że tablica zostanie przekazana jako wskaźnik do pierszego elementu, a nie do początku nagłowka.
    - Ta implementacja gwarantuje, że wartości przy tworzeniu i rozszerzaniu będą zerowane, ale jedynie do rozmiaru, a nie pojemności.
    - Funkcje zaczynające się od podłogi ('_') używane są wewnętrznie i nie powinny być uruchamiane przez użytkownika.
*/
const size_t VEC_DEFAULT_CAP = 1;
const size_t VEC_RESIZE_FACTOR = 2;

/* Zawiera informacje o budowie nagłówka tablicy */
typedef struct {
    size_t capacity; /* dostępny/zalokowany rozmiar tablicy */
    size_t size; /* ilość elementów */
    size_t stride; /* rozmiar pojedynczego elementu */
} vec_header;

/* Funkcja zwraca wskaźnik do nagłówka tablicy */
static inline vec_header* vec(void* arr) { return ((vec_header*)arr) - 1; }

/* Funkcja ustawia fragment tablicy od start do końca na 0 */
void vec_clear(void* arr, size_t start) {
    vec_header* info = vec(arr);

    /* Nie trzeba nic czyścić, start poza granicami tablicy */
    if(info->size <= start) return;

    /* sizeof(char)==1 zatem w ten sposób możemy manipulować bajtami niezależnie od stride */
    char* arr_start = (char*)(void*)(info+1);
    arr_start += start*(info->stride); /* ignorujemy początkowe elementy */
    size_t mem_len = (info->size - start)*(info->stride); /* liczba bajtów, które trzeba wyczyścić */
    memset(arr_start, 0, mem_len);
}

/* Funkcje tworzą nowy wektor, vec_create ustala rozmiar na VEC_DEFAULT_CAP, a vec_create_prealloc na dowolną.
    Pierwszy argument jest typem. 
*/
#define vec_create_prealloc(type, capacity) (type *)_vec_create(capacity, sizeof(type))
#define vec_create(type) vec_create_prealloc(type, VEC_DEFAULT_CAP)
void* _vec_create(size_t init_cap, size_t stride) {
    size_t header_size = sizeof(vec_header);
    size_t arr_size = init_cap * stride;
    
    vec_header* arr = (vec_header*) malloc(header_size + arr_size);
    assert(arr != NULL);
    
    arr->capacity = init_cap;
    arr->size = 0;
    arr->stride = stride;

    vec_header* arr_content = arr+1;
    vec_clear((void*)arr_content, 0);

    return (void*) arr_content;
}

/* Funkcja zwalnia pamięć tablicy (z nagłówkami włącznie). */
void vec_free(void* arr) {
    if (arr != NULL) {
        free(vec(arr));
    }
}

/*
    Funkcja rozszerza tablice, bierze pod uwagę VEC_RESIZE_FACTOR
    Argumenty:
    - arr: wskażnik do tablicy
    - n: ile nowych elementów ma być utworzonych
*/
#define vec_expand(arr, n) _vec_expand((void**)arr, n)
void _vec_expand(void** arr, size_t n) {
    assert(arr == NULL);
    vec_header* info = vec(*arr);
    size_t new_length = info->size + n;
    size_t old_end = info->size;

    /* Jeśli zmieści się w tablicy, nie trzeba jej rozszerzać */
    if (new_length <= info->capacity) {
        info->size = new_length;
        vec_clear(*arr, old_end+1);
    }

    /* Oblicz nowy wymagany rozmiar tablicy */
    size_t new_capacity = info->capacity;
    while (new_capacity < new_length) new_capacity *= VEC_RESIZE_FACTOR;
    
    size_t real_size = sizeof(vec_header) + new_capacity*info->stride; /* Rozmiar z nagłowkiem oraz rozmiarem komórki wziętym pod uwagę */
    *arr = (vec_header*)realloc(info, real_size) + 1; /* +1, bo *arr ma wskazywać na pierwszy element, a nie na nagłówek */
    assert(*arr != NULL);
    
    info = vec(*arr);
    info->size = new_length;
    info->capacity = new_capacity;

    vec_clear(*arr, old_end+1);
}


/*
    Funkcja zwraca wskaźnik do elementu na indeksie lub null
*/
static inline void* vec_get(void *arr, size_t index) {
    assert(arr != NULL);
    if (vec(arr)->size >= index) return NULL;
    
    return ((char*)arr)+(index*(vec(arr)->stride));
}

/*
    Funkcja:
        - nie robi nic, jeśli element o tym indeksie istnieje
        - rozszerza wektor aby dany element istniał wpw
*/
#define vec_set(arr, index) _vec_set((void**)(arr), index)
static inline void _vec_set(void** arr, size_t index) {
    if (vec(arr)->size <= index) {
        vec_expand(arr, index+1-vec(arr)->size);
    }
}

/*
    Funkcja usuwa n elementów z końca tablicy.
    Argumenty:
    - arr: wskaźnik do tablicy
    - n: ilość elementów do usunięcia
*/
#define vec_shrink(arr, n) _vec_shrink((void**)(arr), n)
void _vec_shrink(void** arr, size_t n) {
    vec_header* info = vec(*arr);

    assert(info->size >= n);
    info->size -= n;

    if ((info->capacity)/VEC_RESIZE_FACTOR <= info->size) return;

    size_t new_capacity = info->capacity;
    while (new_capacity/VEC_RESIZE_FACTOR >= info->size) new_capacity /= VEC_RESIZE_FACTOR;

    size_t real_size = sizeof(vec_header) + new_capacity*info->stride; /* Rozmiar z nagłowkiem oraz rozmiarem komórki wziętym pod uwagę */
    *arr = (vec_header*)realloc(info, real_size) + 1; /* +1, bo *arr ma wskazywać na pierwszy element, a nie na nagłówek */
    assert(*arr != NULL);

    info = vec(*arr);
    info->capacity = new_capacity;
}
#pragma endregion

#pragma region Wczytywanie danych
/*
 Typ zawierający informacje o komórce.
 Wartości:
    - alive: Zmienna która mówi, czy komórka jest żywa.
        Jest to 1bitowa wartość 1 lub 0.
    - neighbors: zmienna która mówi, ile żywych sąsiadów ma dana komórka.
        Jest to 4bitowa wartość od 0 do 8.
    - raw: pole pozwalające na podejrzenie wartości bitowej struktury bezpośrednio.
        W szczególności cell.raw == 0 jeśli komórka jest martwa i nie ma żywych sąsiadów.
*/
typedef union {
    struct {
        unsigned char alive:1;
        unsigned char neighbors:4;
    };
    unsigned char raw;
} Cell;

/* Plansza jest wektorem wektorów typu Cell */
typedef Cell* Row;
typedef Row* Grid;

static inline bool is_digit(char c) {
    return (
        c >= '0' && c <= '9'
    );
}

size_t read_size_t(char* str) {
    char* curr = str;
    size_t num = 0;

    while (is_digit(*curr)) {
        num *= 10;
        num += (size_t)(*curr - '0');
        curr++;
    }
    
    return num;
}

bool read_row(Grid* g, char* str) {
    char* curr = str;

    /* pomiń znaki nie będące cyframi */
    while (!is_digit(*curr) && *curr != '\0') { curr++; }

    if (*curr == '\0' ) return false; /* koniec wiersza */

    size_t row_idx = read_size_t(curr);

    /* pomiń wszystkie cyfry składające się na wczytaną liczbę */
    while (is_digit(*curr) && *curr != '\0') { curr++; }

    if (vec_get(*g, row_idx) == NULL) {
        vec_set(g, row_idx);
        (*g)[row_idx] = vec_create(Cell);
    }

    /* pomiń znaki nie będące cyframi */
    while (!is_digit(*curr) && *curr != '\0') { curr++; }

    size_t col_idx;
    while (*curr != '\0') {
        col_idx = read_size_t(curr);
        vec_set((*g) + row_idx, col_idx);
        (*g)[row_idx][col_idx].alive = true;

        /* pomiń wszystkie cyfry składające się na wczytaną liczbę */
        while (is_digit(*curr) && *curr != '\0') { curr++; }

        /* pomiń znaki nie będące cyframi */
        while (!is_digit(*curr) && *curr != '\0') { curr++; }
    }

    return true;
}

void read_init(Grid* g) {
    char str[1024];
    do {
        fgets(str, sizeof str, stdin);
        assert(str != NULL);
        if (is_digit(str[1])) {
            read_row(g, str);
        }
    } while (is_digit(str[1]));
}

void free_rows(Grid g) {
    for (size_t i = 0; i < vec(g)->size; i++)
        vec_free(g[i]);
}

#pragma endregion

int main() {
    Grid g = vec_create(Row);

    read_init(&g);

    free_rows(g);
    vec_free(g);

    return 0;
}

#pragma GCC diagnostic pop