#ifndef trie_EXTRA_H
#define trie_EXTRA_H

#include "trie.h"
#include <stdio.h>

/* Implementowane przez użytkownika */
void trie_extra_write_dot(const trie_extra_t* v, FILE* fp);

void trie_write_dot(trie_root_t* root, FILE* fp);

#endif // trie_EXTRA_H