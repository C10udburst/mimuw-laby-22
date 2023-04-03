#include "trie.h"
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <stdbool.h>
#include <stdio.h>

/*
    "Drzewo trie 012"

    Biblioteka obsługująca drzewo trie mające składające się z ciągów znaków '0','1' lub '2'.
    Zakładamy, że użytkownik zaimplementuje struct trie_extra oraz trie_extra_free()

    autor: Tomasz Wilkins <tomasz@wilkins.ml>
    wersja: 1.0.0
    data: 02.04.2023
*/


static void trie_free_nodes(trie_node_t* node);
static trie_node_t* trie_find_node(trie_node_t* root, const char* name, size_t n);
static inline trie_node_t* trie_malloc_node();
static size_t trie_node_height(trie_node_t* node);

/*
    Funkcja trie_invalid_name sprawdza, czy podany napis jest poprawną reprezentacją ciągu.
    Parametr funkcji:
        str – wskaźnik na napis reprezentujący nazwę ciągu.
    Wynik funkcji:
        1 – jeśli napis nie jest poprawną reprezentacją ciągu;
        0 – jeśli napis jest poprawną reprezentacją ciągu.
*/
int trie_invalid_name(const char* str) {
    if (str == NULL) return true;
    size_t len = 0;
    while(*str != '\0') {
        if  (*str < '0' || *str > '2') // *str rożne od '0', '1' i '2'
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
        NULL – jeśli wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno na ENOMEM.
*/
trie_root_t* trie_init(void) {
    trie_root_t* trie = malloc(sizeof(trie_root_t));
    if (trie == NULL) {
        errno = ENOMEM;
        return NULL;
    }
    trie->root = trie_malloc_node();
    return trie;
}

/*
    Funkcja trie_free usuwa drzewo trie i zwalnia całą używaną przez niego pamięć.
    Nic nie robi, jeśli zostanie wywołana ze wskaźnikiem NULL.
    Po wykonaniu tej funkcji przekazany jej wskaźnik staje się nieważny.
    Parametr funkcji:
        root – wskaźnik na strukturę reprezentującą trie.
*/
void trie_free(trie_root_t* root) {
    trie_free_nodes(root->root);
}


/*
    Funkcja dodaje do drzewa trie wszystkie niepuste prefiksy podanego łańcucha.
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

    bool added_node = false;
    trie_node_t* parent = root->root;
    for (size_t i = 0; i < n; i++) { // parent.name == name[0..(i-1)]
        if (parent->children[name[i]-'0'] != NULL) { // węzeł istnieje
            parent = parent->children[name[i]-'0'];
        } else { // węzeł nie istnieje
            trie_node_t* new_node = trie_malloc_node();
            if (new_node == NULL) {
                errno = ENOMEM;
                return -1;
            }
            parent->children[name[i]-'0'] = new_node;
            parent = new_node;
            added_node = true;
        }
    }
    return added_node ? 1 : 0;
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
    Funkcja trie_remove usuwa z drzewa trie węzeł o podanej nazwie oraz wszystkie ciągi których prefiksem jest name.
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
    trie_node_t* parent = trie_find_node(root->root, name, n - 1); // szukamy ojca
    if (parent == NULL)
        return errno == EINVAL ? -1 : 0;
    trie_node_t* main = parent->children[name[n-1]-'0'];
    if (main == NULL) 
        return 0;
    trie_free_nodes(main); // usuwamy żądany węzeł
    parent->children[name[n-1]-'0'] = NULL; // usuwamy wskaźnik na usuwany węzeł
    return 1;
}

/*
    Funkcja zwraca liczbę węzłów w drzewie trie.
    Parametr funkcji:
        root – wskaźnik na strukturę reprezentującą trie.
    Wynik funkcji:
        liczba węzłów w drzewie trie.
*/
size_t trie_height(trie_root_t* root) {
    return trie_node_height(root->root);
}

/*
    Funkcja rekurencyjnie usuwa węzeł i jego synów
*/
static void trie_free_nodes(trie_node_t* node) {
    if (node == NULL) return;
    for (int i = 0; i < 3; i++) {
        trie_free_nodes(node->children[i]);
    }
    if (node->extra != NULL)
        trie_extra_free(node->extra);
    free(node);
}

static trie_node_t* trie_find_node(trie_node_t* root, const char* name, size_t n) {
    if (root == NULL) return NULL;
    trie_node_t* parent = root;
    for (size_t i = 0; i < n; i++) { // parent.name == name[0..(i-1)]
        if (parent->children[name[i]-'0'] != NULL) { // węzeł istnieje
            parent = parent->children[name[i]-'0'];
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
    for (int i = 0; i < 3; i++)
        node->children[i] = NULL;
    return node;
}

/*
    Funkcja zwraca wysokość drzewa trie.
    Parametr funkcji:
        root – wskaźnik na strukturę reprezentującą trie.
    Wynik funkcji:
        wysokość drzewa trie.
*/
static size_t trie_node_height(trie_node_t* node) {
    if (node == NULL) return 0;

    size_t max_height = 0;
    for (int i = 0; i < 3; i++) {
        size_t height = trie_node_height(node->children[i]);
        if (height > max_height)
            max_height = height;
    }

    return max_height + 1;
}