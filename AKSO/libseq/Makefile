CC       = gcc
CPPFLAGS =
CFLAGS   = -Wall -Wextra -Wno-implicit-fallthrough -std=gnu17 -fPIC -O0 -g3

SOLIBFLAGS = -shared -Wl,--wrap=malloc -Wl,--wrap=calloc -Wl,--wrap=realloc -Wl,--wrap=reallocarray -Wl,--wrap=free -Wl,--wrap=strdup -Wl,--wrap=strndup

export LD_LIBRARY_PATH := $(LD_LIBRARY_PATH):.

.PHONY: all clean test list

all: libseq.so seqtest.elf trietest.elf

%.o: %.c %.h
	$(CC) $(CFLAGS) -c -o $@ $<

libseq.so: seq.o trie.o memory_tests.o
	$(CC) -shared -o $@ $^ $(SOLIBFLAGS)

clean: 
	rm -f *.o *.so *.a *.elf *.dot

seqtest.elf: seq_example_debug.o libseq.so
	$(CC) -L. -o $@ $< -lseq

trietest.elf: trie_test.o trie_extra.o trie.o
	$(CC) -L. -o $@ $^

trietest.dot: trietest.elf
	./$<

trieshow: trietest.dot
	python -c 'import webbrowser;from urllib.parse import quote;path="$<";webbrowser.open(f"https://dreampuf.github.io/GraphvizOnline/#{quote(open(path).read())}")'

test: seqtest.elf
# TODO: install valgrind
#	valgrind --leak-check=full -q --error-exitcode=1
	@./$< params && echo -e "params: \e[32mOK\e[0m" || echo -e "params: \e[31mFailed\e[0m"
	@./$< simple && echo -e "simple: \e[32mOK\e[0m" || echo -e "simple: \e[31mFailed\e[0m"
	@./$< equivalence && echo -e "equivalence: \e[32mOK\e[0m" || echo -e "equivalence: \e[31mFailed\e[0m"
	@./$< memory && echo -e "memory: \e[32mOK\e[0m" || echo -e "memory: \e[31mFailed\e[0m"

list: Makefile
	@grep -E "^[a-z0-9.]+: " Makefile | cut -d':' -f1 | sort
	@ls *.c | sed s/.c$$/.o/g | sort