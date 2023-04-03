#ifndef TRIE_H
#define TRIE_H

#include <stdlib.h>

/*
    Biblioteka obsługująca drzewo trie składające się z ciągów znaków '0','1' lub '2'.
*/

/*
    Definicje wartości trzymanej w drzewie (poza nazwą) oraz funkcje je obsługujące.
    Powinny zostać utworzone przez użytkownika.
*/
typedef struct trie_extra trie_extra_t;
void           trie_extra_free(trie_extra_t* v);

/* Typy używane przez bibliotekę */
typedef struct trie_node {
    trie_extra_t* extra;
    
    struct trie_node* children[3];
} trie_node_t;

typedef struct {
    trie_node_t* root;
} trie_root_t;

/* Definicje funkcji */
trie_root_t*    trie_init();
void            trie_free(trie_root_t* root);
int             trie_insert_prefix(trie_root_t* root, const char* name);
trie_node_t*    trie_find(trie_root_t* root, const char* name);
int             trie_remove_prefix(trie_root_t* root, const char* name);
size_t          trie_height(trie_root_t* root);
int             trie_invalid_name(const char* str);

/* Definicje makr */

/*
    Makro wywołuje kod w każdym węźle drzewa, w obiegu DFS preorder.
    Parametry:
        trie_root - struktura drzewa
        iterated_symbol - nazwa węzła
        code - kod wykonywany w każdym węźle
*/
#define TRIE_FOREACH(trie_root, iterated_symbol, code)                          \
do {                                                                            \
    trie_node_t* iterated_symbol;                                               \
    trie_node_t** stack = calloc(trie_height(trie_root), sizeof(trie_node_t*)); \
    size_t stack_size = 1;                                                      \
    stack[0] = trie_root->root;                                                 \
    while (stack_size > 0) {                                                    \
        iterated_symbol = stack[--stack_size];                                  \
        if (iterated_symbol == NULL)                                            \
            continue;                                                           \
        code;                                                                   \
        for (int i = 0; i < 3; i++)                                             \
            if (iterated_symbol->children[i] != NULL)                           \
                stack[stack_size++] = iterated_symbol->children[i];             \
    }                                                                           \
} while(0)

#endif // TRIE_H