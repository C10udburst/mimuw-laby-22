CC = gcc
CFLAGS = -Wall -Wextra -std=c17 -O0 -g3
#TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
TESTER =
NASMFLAGS = -w+all -w+error
GDB_FLAGS = --tui -x gdbinit

.PHONY: clean test gdb dump

sum.o: sum.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

sum_example.o: sum_example.c

sum.elf: sum_example.o sum.o
	gcc -z noexecstack -o $@ $^

libsum.so: sum.o
	$(CC) -shared -o $@ $^

gdb: sum.elf
	gdb ./$< $(GDB_FLAGS) -ex "run"

pgdb: libsum.so
	gdb python3 $(GDB_FLAGS) -ex "run sum_test.py"

p: libsum.so
	python3 sum_test.py

dump: sum.o
	objdump -M intel -d $^

test: sum.elf
	$(TESTER) ./$<

clean:
	rm *.o *.elf