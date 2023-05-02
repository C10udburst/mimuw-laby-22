CC = gcc
CFLAGS = -Wall -Wextra -std=c17 -O0 -g3
#TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
TESTER =

.PHONY: clean test gdb

sum.o: sum.asm
	nasm -f elf64 -w+all -w+error -o $@ $<

sum_example.o: sum_example.c

sum.elf: sum_example.o sum.o
	gcc -z noexecstack -o $@ $^

gdb: sum.elf
	gdb ./$< --tui -ex "break sum" -ex "set debuginfod enabled on" -ex "set disassembly-flavor intel" -ex "r" -ex "layout asm"

test: sum.elf
	$(TESTER) ./$<

clean:
	rm *.o *.elf