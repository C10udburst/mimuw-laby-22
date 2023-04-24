#include "trie.h"
#include "trie_extra.h"
#include <errno.h>
#include <stdio.h>
#include <string.h>

struct trie_extra {
    char* name;
};

void trie_extra_free(trie_extra_t* v) {
    if (v->name != NULL) free(v->name);
    free(v);
}

void trie_extra_write_dot(const trie_extra_t* v, FILE* fp) {
    if (v->name != NULL) {
        printf("%s", v->name);
        fprintf(fp, "%s", v->name);
    } else
        fprintf(fp, "null");
}

#define TESTFIND(tree, query)                                                  \
    do {                                                                       \
        trie_node_t* result = trie_find(tree, query);                          \
        if (result == NULL)                                                    \
            printf("\e[31mError:\e[0m result should be %s, but is NULL\n",     \
                   query);                                                     \
    } while (0)

#define RENAME(tree, query)                                                    \
    do {                                                                       \
        trie_node_t* result = trie_find(tree, query);                          \
        if (result == NULL)                                                    \
            printf("\e[31mError:\e[0m result should be %s, but is NULL\n",     \
                   query);                                                     \
        else {                                                                 \
            result->extra = malloc(sizeof(trie_extra_t));                      \
            result->extra->name = strdup(query);                               \
        }                                                                      \
    } while (0)

#define TESTNULL(tree, query)                                                  \
    do {                                                                       \
        trie_node_t* result = trie_find(tree, query);                          \
        if (result != NULL)                                                    \
            printf(                                                            \
                "\e[31mError:\e[0m %s should be NULL, but is: %p: [%p, %p, "   \
                "%p]\n",                                                       \
                query, result, result->children[0], result->children[1],       \
                result->children[2]);                                          \
    } while (0)

#define TESTV(predicate, expected)                                             \
    do {                                                                       \
        if (predicate != expected)                                             \
            printf("\e[31mError:\e[0m %s should be %s\n", #predicate,          \
                   #expected);                                                 \
    } while (0)

int main(void) {
    trie_root_t* tree = trie_init();

    TESTNULL(tree, "0");

    trie_insert_prefix(tree, "01122");
    TESTFIND(tree, "01122");
    TESTFIND(tree, "011");
    TESTNULL(tree, "1");

    trie_insert_prefix(tree, "invalid");
    TESTV(errno, EINVAL);

    trie_remove_prefix(tree, "0112");
    TESTNULL(tree, "0112");
    TESTNULL(tree, "01122");
    TESTFIND(tree, "011");

    trie_insert_prefix(tree, "000000");
    trie_insert_prefix(tree, "011111");
    trie_insert_prefix(tree, "012111");
    trie_insert_prefix(tree, "011211");
    trie_insert_prefix(tree, "011121");
    trie_insert_prefix(tree, "012121");

    RENAME(tree, "000000");
    RENAME(tree, "011");

    // Write to file
    FILE* f = fopen("trietest.dot", "w");
    trie_write_dot(tree, f);
    fclose(f);

    printf("foreach: ");
    TRIE_FOREACH(tree, node, { printf("%p ", node); });
    printf("\n");

    printf("tree stack size: %lu\n", trie_stack_size(tree));

    trie_free(tree);

    return 0;
}