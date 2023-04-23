#ifndef TRIE_H
#define TRIE_H

#include <errno.h>
#include <stdlib.h>

/*
    Biblioteka obsługująca drzewo trie składające się z ciągów znaków '0','1'
   lub '2'.
*/

/*
    Definicje wartości trzymanej w drzewie (poza nazwą) oraz funkcje je
   obsługujące. Powinny zostać utworzone przez użytkownika.
*/
typedef struct trie_extra trie_extra_t;
void trie_extra_free(trie_extra_t* v);

/* Typy używane przez bibliotekę */
typedef struct trie_node {
    trie_extra_t* extra;

    struct trie_node* children[3];
} trie_node_t;

typedef struct {
    trie_node_t* root;
} trie_root_t;

/* Definicje funkcji */
trie_root_t* trie_init();
void trie_free(trie_root_t* root);
int trie_insert_prefix(trie_root_t* root, const char* name);
trie_node_t* trie_find(trie_root_t* root, const char* name);
int trie_remove_prefix(trie_root_t* root, const char* name);
size_t trie_stack_size(trie_node_t* node);
int trie_invalid_name(const char* str);

/* Definicje makr */

/*
    Makro wywołuje kod w każdym węźle drzewa, w obiegu DFS postorder.
    Ustawia errno na ENOMEM jeśli nie udało się zaalkować stosu.
    Kod będzie wykonywany również dla start_node.
    Jeśli wystąpi błąd alokacji, to kod nie wykona się ani razu.
    Parametry:
        start_node - węzeł, od którego zaczynamy
        iterated_symbol - nazwa węzła
        code - kod wykonywany w każdym węźle
*/
#define TRIE_FOREACH(start_node, iterated_symbol, code)                        \
    do {                                                                       \
        trie_node_t* iterated_symbol;                                          \
        size_t height = trie_stack_size(start_node);                           \
        if (height > 0) {                                                      \
            trie_node_t** stack = calloc(height, sizeof(trie_node_t*));        \
            if (stack == NULL)                                                 \
                errno = ENOMEM;                                                \
            else {                                                             \
                size_t stack_size = 1;                                         \
                stack[0] = start_node;                                         \
                while (stack_size > 0) {                                       \
                    iterated_symbol = stack[--stack_size];                     \
                    if (iterated_symbol == NULL) continue;                     \
                    for (int i = 0; i < 3; i++) {                              \
                        if (iterated_symbol->children[i] != NULL)              \
                            stack[stack_size++] =                              \
                                iterated_symbol->children[i];                  \
                    }                                                          \
                    code;                                                      \
                }                                                              \
                free(stack);                                                   \
            }                                                                  \
        }                                                                      \
    } while (0)

#endif // TRIE_H