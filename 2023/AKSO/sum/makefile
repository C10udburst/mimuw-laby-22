CC = gcc
CFLAGS = -Wall -Wextra -std=c17 -O0 -g3
#TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
TESTER =
NASMFLAGS = -w+all -w+error
GDB_FLAGS = --tui -x gdbinit

.PHONY: clean test gdb dump 69 p pgdb testy

sum.o: sum.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

sum_example.o: sum_example.c
tester.o: tester.c

abi_check.o: abi_check.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

sum.elf: sum_example.o sum.o
	gcc -z noexecstack -o $@ $^

libsum.so: sum.o
	$(CC) -shared -o $@ $^

gdb: sum.elf
	gdb ./$< $(GDB_FLAGS) -ex "run"

pgdb: libsum.so
	gdb python3 $(GDB_FLAGS)

69: libsum.so
	for _ in {1..69}; do python3 sum_test.py || break; done

p: libsum.so
	python3 sum_test.py

dump: sum.o
	objdump -M intel -d $^
	size sum.o

test: sum.elf
	$(TESTER) ./$<

tester.elf: sum.o abi_check.o tester.o
	gcc -z noexecstack -o $@ $^

testy: tester.elf
	./$< < ./testy/t1 > /dev/null || echo "FAIL"
	./$< < ./testy/t2 > /dev/null || echo "FAIL"
	./$< < ./testy/t3 > /dev/null || echo "FAIL"
	./$< < ./testy/t4 > /dev/null || echo "FAIL"
	./$< < ./testy/t5 > /dev/null || echo "FAIL"
	./$< < ./testy/t6 > /dev/null || echo "FAIL"
	./$< < ./testy/t7 > /dev/null || echo "FAIL"
	./$< < ./testy/t8 > /dev/null || echo "FAIL"
	./$< < ./testy/t9 > /dev/null || echo "FAIL"
	./$< < ./testy/t10 > /dev/null || echo "FAIL"

clean:
	rm *.o *.elf