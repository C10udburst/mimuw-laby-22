#include "trie_extra.h"
#include "trie.h"

/*
    Biblioteka zawiera dodatkowe funkcje trie, które nie są używane w ostatecznym programie, ale są przydatne przy testowaniu.
    Zakładamy, że użytkownik zaimplementował trie_value_write_dot()
*/

/*
    Funkcja pomocnicza do trie_write_dot()
    Wypisuje wierzchołek drzewa do formatu dot
*/
void trie_write_dot_node(FILE* fp, trie_node_t* node, trie_node_t* parent, int index) {
    if (node == NULL) return;
    if (parent != NULL)
        fprintf(fp, "\t\"%p\" -> \"%p\" [label=\"%d\"]\n", parent, node, index);
    fprintf(fp, "\t\"%p\" [label=\"\n", node);
    if (node->extra == NULL) fprintf(fp, "NULL");
    else trie_extra_write_dot(node->extra, fp);
    fprintf(fp, "\"]\n");
    for (int i = 0; i < 3; i++) {
        trie_write_dot_node(fp, node->children[i], node, i);
    }
}

/*
    Funkcja wypisuje drzewo do formatu dot
*/
void trie_write_dot(trie_root_t* root, FILE* fp) {
    fprintf(fp, "digraph trie {\n");
    fprintf(fp, "graph [ordering=\"out\"];\n");
    fprintf(fp, "node [shape=record];\n");
    trie_write_dot_node(fp, root->root, NULL, -1);
    fprintf(fp, "}");
}