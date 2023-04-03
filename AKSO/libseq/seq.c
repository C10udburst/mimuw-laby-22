/*
    "Zbiory równoważnych ciągów"

    Biblioteka obsługująca zbiory ciągów z relacją równoważności.
    Elementami zbiorów są niepuste ciągi, których elementami są liczby 0, 1 i 2.
    W implementacji ciąg reprezentujemy jako napis.
    Na przykład ciąg {0, 1, 2} reprezentujemy jako napis "012".
    Klasom abstrakcji można nadawać nazwy.

    autor: Tomasz Wilkins <tomasz@wilkins.ml>
    wersja: 1.0.0
    data: 02.04.2023
*/

#include "seq.h"
#include "trie.h"
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

/*
    Poprawna reprezentacja ciągu jest niepustym napisem składającym się ze znaków 0, 1 lub 2 i jest zakończona terminalnym zerem.
    Poprawna nazwa klasy abstrakcji jest niepustym napisem zakończonym terminalnym zerem.
    Napisy reprezentujące ciągi mogą przestać istnieć po zakończeniu funkcji.
    Używane poniżej określenie, że zbiór ciągów się nie zmienił, oznacza, że nie zmienił się obserwowalny stan zbioru ciągów.
*/

typedef struct {
    char* name; // nazwa klasy
    unsigned int refs; // ilość odwołań
} equiv_class_t;


static equiv_class_t* seq_new_equiv_class(const char* name);

/* Struktury trie.h */
struct trie_extra {
    equiv_class_t* equiv_class;
};

/* Struktury seq.h */

struct seq {
    trie_root_t* strings;
};

/* funkcje seq.h */

/*
    Funkcja seq_new tworzy nowy pusty zbiór ciągów.
    Wynik funkcji:
        wskaźnik na strukturę reprezentującą zbiór ciągów lub
        NULL – jeśli wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno na ENOMEM.
*/
seq_t * seq_new(void) {
    seq_t* seq = malloc(sizeof(seq_t));
    if (seq == NULL) {
        errno = ENOMEM;
        return NULL;
    }
    seq->strings = trie_init();
    if (seq->strings == NULL) {
        free(seq);
        errno = ENOMEM;
        return NULL;
    }
    return seq;
}


/*
    Funkcja seq_delete usuwa zbiór ciągów i zwalnia całą używaną przez niego pamięć.
    Nic nie robi, jeśli zostanie wywołana ze wskaźnikiem NULL.
    Po wykonaniu tej funkcji przekazany jej wskaźnik staje się nieważny.
    Parametr funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów.

*/
void seq_delete(seq_t *p) {
    if (p == NULL) return;
    trie_free(p->strings);
    free(p);
}

/*
    Funkcja seq_add dodaje do zbioru ciągów podany ciąg i wszystkie niepuste podciągi będące jego prefiksem.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s – wskaźnik na napis reprezentujący niepusty ciąg.
    Wynik funkcji:
        1 – jeśli co najmniej jeden nowy ciąg został dodany do zbioru;
        0 – jeśli zbiór ciągów się nie zmienił;
       -1 – jeśli któryś z parametrów jest niepoprawny lub wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno odpowiednio na EINVAL lub ENOMEM.
*/
int seq_add(seq_t *p, char const *s) {
    if (p == NULL) {
        errno = EINVAL;
        return -1;
    }
    return trie_insert_prefix(p->strings, s);
}

/*
    Funkcja seq_remove usuwa ze zbioru ciągów podany ciąg i wszystkie ciągi, których jest on prefiksem.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s – wskaźnik na napis reprezentujący niepusty ciąg.
    Wynik funkcji:
        1 – jeśli co najmniej jeden ciąg został usunięty ze zbioru;
        0 – jeśli zbiór ciągów się nie zmienił;
       -1 – jeśli któryś z parametrów jest niepoprawny; funkcja ustawia wtedy errno na EINVAL.
*/
int seq_remove(seq_t *p, char const *s) {
    if (p == NULL) {
        errno = EINVAL;
        return -1;
    }
    return trie_remove_prefix(p->strings, s);
}

/*
    Funkcja seq_valid sprawdza, czy podany ciąg należy do zbioru ciągów.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s – wskaźnik na napis reprezentujący niepusty ciąg.
    Wynik funkcji:
        1 – jeśli ciąg należy do zbioru ciągów;
        0 – jeśli ciąg nie należy do zbioru ciągów;
       -1 – jeśli któryś z parametrów jest niepoprawny; funkcja ustawia wtedy errno na EINVAL.
*/
int seq_valid(seq_t *p, char const *s) {
    if (p == NULL) {
        errno = EINVAL;
        return -1;
    }
    trie_node_t* found = trie_find(p->strings, s);
    if (found) return 1;
    else return errno != 0 ? -1 : 0;
}

/*
    Funkcja seq_set_name ustawia lub zmienia nazwę klasy abstrakcji, do której należy podany ciąg.
    Podaną nazwę należy skopiować, gdyż napis wskazywany przez wskaźnik n może przestać istnieć po zakończeniu tej funkcji.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s – wskaźnik na napis reprezentujący niepusty ciąg;
        n – wskaźnik na napis z nową niepustą nazwą.
    Wynik funkcji:
        1 – jeśli nazwa klasy abstrakcji została przypisana lub zmieniona;
        0 – jeśli ciąg nie należy do zbioru ciągów lub nazwa klasy abstrakcji nie została zmieniona;
       -1 – jeśli któryś z parametrów jest niepoprawny lub wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno odpowiednio na EINVAL lub ENOMEM.
*/
int seq_set_name(seq_t *p, char const *s, char const *n) {
    if (p == NULL) {
        errno = EINVAL;
        return -1;
    }
    trie_node_t* node = trie_find(p->strings, s);
    if (node == NULL)
        return errno != 0 ? -1 : 0;
    if (node->extra != NULL) {
        if (node->extra->equiv_class == NULL) {
            node->extra->equiv_class = seq_new_equiv_class(n);
            if (node->extra->equiv_class == NULL)
                return -1;
            return 1;
        } else {
            if (node->extra->equiv_class->name == NULL) {
                node->extra->equiv_class->name = strdup(n);
                if (node->extra->equiv_class->name == NULL) {
                    errno = ENOMEM;
                    return -1;
                }
                return 1;
            } else {
                if (strcmp(node->extra->equiv_class->name, n) == 0)
                    return 0;
                else {
                    char* new_name = strdup(n);
                    if (new_name == NULL) {
                        errno = ENOMEM;
                        return -1;
                    }
                    free(node->extra->equiv_class->name);
                    node->extra->equiv_class->name = new_name;
                    return 1;
                }
            }
        }
    } else {
        node->extra = malloc(sizeof(trie_extra_t));
        if (node->extra == NULL) {
            errno = ENOMEM;
            return -1;
        }
        node->extra->equiv_class = seq_new_equiv_class(n);
        if (node->extra->equiv_class == NULL) {
            free(node->extra);
            node->extra = NULL;
            return -1;
        }
        return 1;
    }
}

/*
    Funkcja seq_get_name daje wskaźnik na napis zawierający nazwę klasy abstrakcji, do której należy podany ciąg.
    Nie wolno modyfikować pamięci wskazywanej przez ten wskaźnik.
    Wskaźnik ten może zostać unieważniony po jakiejkolwiek zmianie w strukturze zbioru ciągów.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s – wskaźnik na napis reprezentujący niepusty ciąg.
    Wynik funkcji:
        wskaźnik na napis zawierający nazwę lub
        NULL – jeśli ciąg nie należy do zbioru ciągów lub klasa abstrakcji zawierająca ten ciąg nie ma przypisanej nazwy; funkcja ustawia wtedy errno na 0.
        NULL – jeśli któryś z parametrów jest niepoprawny; funkcja ustawia wtedy errno na EINVAL.

*/
char const * seq_get_name(seq_t *p, char const *s) {
    if (p == NULL) {
        errno = EINVAL;
        return NULL;
    }
    trie_node_t* found = trie_find(p->strings, s);
    if (found == NULL)
        return NULL;
    if (found->extra == NULL)
        return NULL;
    if (found->extra->equiv_class == NULL)
        return NULL;
    return found->extra->equiv_class->name;
}

/*
    Funkcja seq_equiv łączy w jedną klasę abstrakcji klasy abstrakcji reprezentowane przez podane ciągi.
    Jeśli obie klasy abstrakcji nie mają przypisanej nazwy, to nowa klasa abstrakcji też nie ma przypisanej nazwy.
    Jeśli dokładnie jedna z klas abstrakcji ma przypisaną nazwę, to nowa klasa abstrakcji dostaje tę nazwę.
    Jeśli obie klasy abstrakcji mają przypisane różne nazwy, to nazwa nowej klasy abstrakcji powstaje przez połączenie tych nazw.
    Jeśli obie klasy abstrakcji mają przypisane taką same nazwę, to ta nazwa pozostaje nazwą nowej klasy abstrakcji.
    Parametry funkcji:
        p – wskaźnik na strukturę reprezentującą zbiór ciągów;
        s1 – wskaźnik na napis reprezentujący niepusty ciąg;
        s2 – wskaźnik na napis reprezentujący niepusty ciąg.
    Wynik funkcji:
        1 – jeśli powstała nowa klasa abstrakcji;
        0 – jeśli nie powstała nowa klasa abstrakcji, bo podane ciągi należą już do tej samej klasy abstrakcji lub któryś z nich nie należy do zbioru ciągów;
       -1 – jeśli któryś z parametrów jest niepoprawny lub wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno odpowiednio na EINVAL lub ENOMEM.
*/
int seq_equiv(seq_t *p, char const *s1, char const *s2) {
    if (p == NULL) {
        errno = EINVAL;
        return -1;
    }
    
    trie_node_t* found1 = trie_find(p->strings, s1);
    if (found1 == NULL)
        return errno != 0 ? -1 : 0;

    if (strcmp(s1, s2) == 0)
        return 0;

    trie_node_t* found2 = trie_find(p->strings, s2);
    if (found2 == NULL)
        return errno != 0 ? -1 : 0;
    
    equiv_class_t* class1 = (found1->extra != NULL) ? found1->extra->equiv_class : NULL; // class1 = found1.extra?.equiv_class
    equiv_class_t* class2 = (found2->extra != NULL) ? found2->extra->equiv_class : NULL; // class2 = found2.extra?.equiv_class
    equiv_class_t* joined_class = NULL;
    bool is_new_class = false;

    if (class1 == NULL && class2 == NULL) {
        joined_class = seq_new_equiv_class(NULL);
        if (joined_class == NULL) {
            errno = ENOMEM;
            return -1;
        }
        is_new_class = true;
    } else if (class1 == NULL && class2 != NULL)
        joined_class = class2;
    else if (class1 != NULL && class2 == NULL)
        joined_class = class1;
    else if (class1 == class2) // class1 != NULL && class2 != NULL
        return 0; // już są w tej samej klasie abstrakcji

    if (found1->extra == NULL) {
        found1->extra = malloc(sizeof(trie_extra_t));
        found1->extra->equiv_class = NULL;
        if (found1->extra == NULL) {
            if (is_new_class) free(joined_class); // is_new_class => joined_class->name == NULL
            errno = ENOMEM;
            return -1;
        }
    }
    if (found2->extra == NULL) {
        found2->extra = malloc(sizeof(trie_extra_t));
        if (found2->extra == NULL) {
            if (is_new_class) free(joined_class); // is_new_class => joined_class->name == NULL
            errno = ENOMEM;
            return -1;
        }
    }

    if (joined_class != NULL) { // class1 == NULL || class2 == NULL
        if (is_new_class) joined_class->refs = 2;
        else joined_class->refs += 1;

        found1->extra->equiv_class = joined_class;
        found2->extra->equiv_class = joined_class;
        return 1;
    }

    // class1 != NULL && class2 != NULL && class1 != class2
    
    equiv_class_t* bigger = (class1->refs >= class2->refs) ? class1 : class2;
    equiv_class_t* smaller = (class1->refs >= class2->refs) ? class2 : class1;
    
    if (class1->name == NULL || class2->name == NULL || strcmp(class1->name, class2->name) != 0) { // nazwy klas są rózne
        size_t str_len = 0;
        str_len += (class1->name != NULL) ? strlen(class1->name) : 0; // class1.name?.length ?: 0
        str_len += (class2->name != NULL) ? strlen(class2->name) : 0; // class2.name?.length ?: 0

        char* new_name = malloc(str_len + 1);
        if (new_name == NULL) {
            errno = ENOMEM;
            return -1;
        }
        new_name[0] = '\0';
        if (class1->name != NULL) strcat(new_name, class1->name);
        if (class2->name != NULL) strcat(new_name, class2->name);
        free(bigger->name);
        bigger->name = new_name;
    }

    // łączymy mniejszą klasę z większą
    bigger->refs += smaller->refs;
    if (smaller->refs <= 1) { // tylko found1 lub found2 odwołuje się do mniejszej klasy
        if (found1->extra->equiv_class == smaller) 
            found1->extra->equiv_class = bigger;
        else // found2->extra->equiv_class == smaller
            found2->extra->equiv_class = bigger;
    } else { // trzeba zmienić każde odwołanie w strukturze do mniejszej klasy
        TRIE_FOREACH(p->strings, node, { // dla każdego węzła w strukturze trie
            if (node->extra != NULL && node->extra->equiv_class == smaller)
                node->extra->equiv_class = bigger;
        });
    }
    free(smaller->name);
    free(smaller);

    return 1;
}

/*
    Funkcja seq_new_equiv_class tworzy nową klasę abstrakcji i ustala refs na 1
    Parametry funkcji:
        name – wskaźnik na napis reprezentujący niepusty ciąg lub NULL.
    Wynik funkcji:
        wskaźnik na nową klasę abstrakcji lub
        NULL – jeśli wystąpił błąd alokowania pamięci; funkcja ustawia wtedy errno na ENOMEM.
*/
static equiv_class_t* seq_new_equiv_class(const char* name) {
    equiv_class_t* new_class = malloc(sizeof(equiv_class_t));
    if (new_class == NULL) {
        errno = ENOMEM;
        return NULL;
    }
    if (name == NULL) {
        new_class->name = NULL;
    } else {
        new_class->name = strdup(name);
        if (new_class->name == NULL) {
            free(new_class);
            errno = ENOMEM;
            return NULL;
        }
    }
    new_class->refs = 1;
    return new_class;
}

/* funkcje trie.h */

/*
    Funkcja trie_extra_free zwalnia pamięć zajmowaną przez strukturę trie_extra_t, ewentualnie usuwa klase abstrakcji, jeśli nic się do niej nie odnosi.
    Parametry funkcji:
        v – wskaźnik na strukturę trie_extra_t lub NULL.
    Funkcja nie zwraca wartości.
*/
void trie_extra_free(trie_extra_t* v) {
    if (v == NULL) return;
    if (v->equiv_class != NULL) {
        v->equiv_class->refs -= 1;
        if (v->equiv_class->refs < 1) {
            if (v->equiv_class->name != NULL)
                free(v->equiv_class->name);
            free(v->equiv_class);
        }
    }
    free(v);
}