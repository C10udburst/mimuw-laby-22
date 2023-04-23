CC       = gcc
CPPFLAGS =
#CFLAGS   = -Wall -Wextra -Wno-implicit-fallthrough -std=gnu17 -fPIC -O0 -g3 -fanalyzer -Wanalyzer-double-fclose -Wanalyzer-double-free -Wanalyzer-exposure-through-output-file -Wanalyzer-file-leak -Wanalyzer-free-of-non-heap -Wanalyzer-malloc-leak -Wanalyzer-possible-null-argument -Wanalyzer-possible-null-dereference -Wanalyzer-null-argument -Wanalyzer-null-dereference -Wanalyzer-stale-setjmp-buffer -Wanalyzer-tainted-array-index -Wanalyzer-unsafe-call-within-signal-handler -Wanalyzer-use-after-free -Wanalyzer-use-of-pointer-in-stale-stack-frame
CFLAGS   = -Wall -Wextra -Wno-implicit-fallthrough -std=gnu17 -fPIC -O2
TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
#TESTER = 
LINT_TARGETS = seq.c trie.c seq_test.c trie_test.c trie_extra.c trie.h trie_extra.h

SOLIBFLAGS = -shared -Wl,--wrap=malloc -Wl,--wrap=calloc -Wl,--wrap=realloc -Wl,--wrap=reallocarray -Wl,--wrap=free -Wl,--wrap=strdup -Wl,--wrap=strndup

export LD_LIBRARY_PATH := $(LD_LIBRARY_PATH):.

.PHONY: all clean trieshow test lint list

all: libseq.so seqtest.elf trietest.elf

%.o: %.c %.h
	$(CC) $(CFLAGS) -c -o $@ $<

libseq.so: seq.o trie.o memory_tests.o
	$(CC) -shared -o $@ $^ $(SOLIBFLAGS)

clean: 
	rm -f *.o *.so *.a *.elf *.dot

seqtest.elf: seq_test.o libseq.so
	$(CC) -L. -o $@ $< -lseq

trietest.elf: trie_test.o trie_extra.o trie.o
	$(CC) -L. -o $@ $^

trietest.dot: trietest.elf
	$(TESTER) ./$<

trieshow: trietest.dot
	python -c 'import webbrowser;from urllib.parse import quote;path="$<";webbrowser.open(f"https://dreampuf.github.io/GraphvizOnline/#{quote(open(path).read())}")'

test: seqtest.elf
	@$(TESTER) ./$< params && echo -e "params: \e[32mOK\e[0m" || echo -e "params: \e[31mFailed\e[0m"
	@$(TESTER) ./$< simple && echo -e "simple: \e[32mOK\e[0m" || echo -e "simple: \e[31mFailed\e[0m"
	@$(TESTER) ./$< equivalence && echo -e "equivalence: \e[32mOK\e[0m" || echo -e "equivalence: \e[31mFailed\e[0m"
	@$(TESTER) ./$< equivalence_extra && echo -e "equivalence_extra: \e[32mOK\e[0m" || echo -e "equivalence_extra: \e[31mFailed\e[0m"
	@$(TESTER) ./$< memory && echo -e "memory: \e[32mOK\e[0m" || echo -e "memory: \e[31mFailed\e[0m"
	@$(TESTER) ./$< memory_extra && echo -e "memory_extra: \e[32mOK\e[0m" || echo -e "memory_extra: \e[31mFailed\e[0m"

lint: .clang-format $(LINT_TARGETS)
	clang-format --verbose $(LINT_TARGETS) -i

list: Makefile
	@grep -E "^[a-z0-9.]+: " Makefile | cut -d':' -f1 | sort
	@ls *.c | sed s/.c$$/.o/g | sort