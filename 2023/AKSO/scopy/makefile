#TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
TESTER =
NASMFLAGS = -w+all -w+error
GDB_FLAGS = -x gdbinit
SHELL = /bin/bash
HEX=hexdump #hexyl

.PHONY: clean dump test

scopy.o: scopy.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

rndtest.o: rndtest.asm scopy.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

scopy.elf: scopy.o
	ld --fatal-warnings -o $@ $<

rndtest.elf: rndtest.o
	ld --fatal-warnings -o $@ $<

dump: scopy.o
	objdump -M intel -d $^
	size $^

test: scopy.elf
	rm -f ./example1.sout
	$(TESTER) ./$< ./testy/example1.in ./example1.sout
	diff --color <($(HEX) ./testy/example1.out) <($(HEX) ./example1.sout)

rndtest: rndtest.elf
	for _ in {1..69}; do rm -f ./example1.sout; strace ./$<  ./testy/example1.in ./example1.sout || echo "ERROR!"; done

gdb: scopy.elf
	rm -f ./example1.sout
	gdb $(GDB_FLAGS) -ex "break _start" -ex "run" --args $< ./testy/example1.in ./example1.sout

strace: scopy.elf
	rm -f ./example1.sout
	strace ./$< ./testy/example1.in ./example1.sout

clean:
	rm *.o *.elf *.sout
