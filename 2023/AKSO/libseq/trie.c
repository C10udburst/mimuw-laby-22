#include "trie.h"
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*
    "Drzewo trie 012"

    Biblioteka obsługująca drzewo trie
    mające składające się z ciągów znaków '0','1' lub '2'.
    Zakładamy, że użytkownik zaimplementuje
    struct trie_extra oraz trie_extra_free()

    autor: Tomasz Wilkins <tomasz@wilkins.ml>
    wersja: 1.0.0
    data: 02.04.2023
*/

static void trie_free_nodes(trie_node_t* node);
static trie_node_t* trie_find_node(trie_node_t* root, const char* name,
                                   size_t n);
static inline trie_node_t* trie_malloc_node();

/*
    Funkcja trie_invalid_name sprawdza,
    czy podany napis jest poprawną reprezentacją ciągu.
    Parametr funkcji:
        str – wskaźnik na napis reprezentujący nazwę ciągu.
    Wynik funkcji:
        1 – jeśli napis nie jest poprawną reprezentacją ciągu;
        0 – jeśli napis jest poprawną reprezentacją ciągu.
*/
int trie_invalid_name(const char* str) {
    if (str == NULL) return true;
    size_t len = 0;
    while (*str != '\0') {
        if (*str < '0' || *str > '2') // *str rożne od '0', '1' i '2'
            return true;
        str++;
        len++;
    }
    return len == 0 ? 1 : 0;
}

/*
    Funkcja trie_init tworzy nowe puste drzewo trie.
    Wynik funkcji:
        wskaźnik na strukturę reprezentującą trie lub
        NULL – jeśli wystąpił błąd alokowania pamięci;
            funkcja ustawia wtedy errno na ENOMEM.
*/
trie_root_t* trie_init(void) {
    trie_root_t* trie = malloc(sizeof(trie_root_t));
    if (trie == NULL) {
        errno = ENOMEM;
        return NULL;
    }
    trie->root = trie_malloc_node();
    if (trie->root == NULL) {
        errno = ENOMEM;
        free(trie);
        return NULL;
    }
    return trie;
}

/*
    Funkcja trie_free usuwa drzewo trie
    i zwalnia całą używaną przez niego pamięć.
    Nic nie robi, jeśli zostanie wywołana ze wskaźnikiem NULL.
    Po wykonaniu tej funkcji przekazany jej wskaźnik staje się nieważny.
    Parametr funkcji:
        root – wskaźnik na strukturę reprezentującą trie.
*/
void trie_free(trie_root_t* root) {
    trie_free_nodes(root->root);
    free(root);
}

/*
    Funkcja dodaje do drzewa trie
    wszystkie niepuste prefiksy podanego łańcucha.
    Zwraca:
       -1 w przypadku błędu,
        1 jeśli dodano przynajmniej jeden nowy węzeł
        0 jeśli nie dokonano zmian.
*/
int trie_insert_prefix(trie_root_t* root, const char* name) {
    if (root == NULL || trie_invalid_name(name)) {
        errno = EINVAL;
        return -1;
    }

    size_t n = strlen(name);

    trie_node_t* parent = root->root;
    size_t i = 0; // indeks pierwszej nieistnejącej litery ciągu
    while (i < n && parent->children[name[i] - '0'] != NULL) {
        // szukamy pierwszego niestniejącego prefiksu nazwy
        parent = parent->children[name[i] - '0'];
        i++;
    }

    if (i == n) // jeśli nazwa jest prefiksem istniejącego ciągu
        return 0;

    // z założenia struktury trie, jeśli nie istnieje ciąg name[0..n]
    // to nie istnieje także name[0..n+1]
    trie_node_t* free_parent = parent; // ostatni węzeł którego nie usuwamy.
    for (size_t j = i; j < n; j++) {
        parent->children[name[j] - '0'] = trie_malloc_node();
        if (parent->children[name[j] - '0'] == NULL) {
            trie_free_nodes(free_parent->children[name[i] - '0']);
            free_parent->children[name[i] - '0'] = NULL;
            errno = ENOMEM;
            return -1;
        }
        parent = parent->children[name[j] - '0'];
    }

    return 1;
}

/*
    Funkcja trie_find szuka w drzewie trie węzła o podanej nazwie.
    Parametry funkcji:
        root – wskaźnik na strukturę reprezentującą trie;
        name – wskaźnik na napis reprezentujący nazwę węzła.
    Wynik funkcji:
        wskaźnik na strukturę reprezentującą węzeł o podanej nazwie lub
        NULL – jeśli taki węzeł nie istnieje
*/
trie_node_t* trie_find(trie_root_t* root, const char* name) {
    if (root == NULL || trie_invalid_name(name)) {
        errno = EINVAL;
        return NULL;
    }

    size_t n = strlen(name);

    return trie_find_node(root->root, name, n);
}

/*
    Funkcja trie_remove usuwa z drzewa trie węzeł
    o podanej nazwie oraz wszystkie ciągi których prefiksem jest name.
    Parametry funkcji:
        root – wskaźnik na strukturę reprezentującą trie;
        name – wskaźnik na napis reprezentujący nazwę węzła.
    Wynik funkcji:
        -1 w przypadku błędu,
        1 jeśli usunięto przynajmniej jeden węzeł
        0 jeśli nie dokonano zmian.
*/
int trie_remove_prefix(trie_root_t* root, const char* name) {
    if (root == NULL || trie_invalid_name(name)) {
        errno = EINVAL;
        return -1;
    }
    size_t n = strlen(name);

    // szukamy ojca, aby zmienić mu potem synów
    trie_node_t* parent = trie_find_node(root->root, name, n - 1);
    if (parent == NULL) return errno == EINVAL ? -1 : 0;

    trie_node_t* main = parent->children[name[n - 1] - '0'];
    if (main == NULL) return 0;
    trie_free_nodes(main); // usuwamy żądany węzeł

    // usuwamy wskaźnik na usuwany węzeł
    parent->children[name[n - 1] - '0'] = NULL;
    return 1;
}

/*
    Funkcja iteracyjnie usuwa wszystkie węzły drzewa trie
    i zwalnia całą używaną przez niego pamięć. Próbuje to robić
    w sposób niezawodny, czyli w przypadku braku pamięci
    ponawia usunięcie od początku.
*/
static void trie_free_nodes(trie_node_t* root_node) {
    if (root_node == NULL) return;
    int prev_errno = errno;
    do {
        errno = 0;
        TRIE_FOREACH(root_node, node, {
            if (node->extra != NULL) trie_extra_free(node->extra);
            free(node);
        });
    } while (errno == ENOMEM);
    errno = prev_errno;
}

static trie_node_t* trie_find_node(trie_node_t* root, const char* name,
                                   size_t n) {
    if (root == NULL) return NULL;
    trie_node_t* parent = root;
    for (size_t i = 0; i < n; i++) { // parent.name == name[0..(i-1)]
        if (parent->children[name[i] - '0'] != NULL) { // węzeł istnieje
            parent = parent->children[name[i] - '0'];
        } else { // węzeł nie istnieje
            return NULL;
        }
    }
    return parent;
}

/*
    Funkcja tworzy nowy węzeł drzewa trie i zwraca wskaźnik na niego.
    Wynik funkcji:
        wskaźnik na strukturę reprezentującą nowy węzeł lub
        NULL – jeśli nie udało się zaalokować pamięci.
*/
static inline trie_node_t* trie_malloc_node(void) {
    trie_node_t* node = malloc(sizeof(trie_node_t));
    if (node == NULL) {
        errno = ENOMEM;
        return NULL;
    }

    node->extra = NULL;
    for (int i = 0; i < 3; i++) node->children[i] = NULL;
    return node;
}

/*
    Funkcja zwraca rozmiar stosu potrzebny do przejścia DFS od danego
    wierzchołka. Aby uniknąć przepełnienia stosu, stos jest alokowany
    dynamicznie. Pdejście iteracyjne, zwraca -1 przy błędzie alokacji pamięci
    Parametr funkcji:
        node - wskaźnik na węzeł od którego zaczynamy DFS
    Wynik funkcji:
        rozmiar stosu potrzebny do przejścia DFS od danego wierzchołka
        -1 w przypadku błędu alokacji pamięci
*/
size_t trie_stack_size(trie_node_t* node) {
    if (node == NULL) return 0;

    size_t stack_size = 4;
    size_t stack_end = 1;
    trie_node_t** stack = malloc(stack_size * sizeof(trie_node_t*));
    if (stack == NULL) {
        errno = ENOMEM;
        return -1;
    }
    stack[0] = node;

    size_t max_stack = 0;
    while (stack_end > 0) {
        trie_node_t* current = stack[--stack_end];
        if (current == NULL) continue;

        // dodajemy dzieci do stosu
        for (int i = 0; i < 3; i++) {
            if (current->children[i] != NULL) {
                if (stack_end >= stack_size) {
                    // zwiększamy rozmiar stosu
                    stack_size *= 2;
                    trie_node_t** new_stack =
                        realloc(stack, stack_size * sizeof(trie_node_t*));
                    if (new_stack == NULL) { // błąd alokacji pamięci
                        free(stack);
                        errno = ENOMEM;
                        return -1;
                    }
                    stack = new_stack;
                }
                stack[stack_end++] = current->children[i];
                if (stack_end > max_stack) max_stack = stack_end;
            }
        }
    }

    free(stack);
    return max_stack + 1;
}