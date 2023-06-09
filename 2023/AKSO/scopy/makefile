#TESTER = valgrind --trace-children=yes --track-origins=yes --leak-check=full --show-leak-kinds=all -q --error-exitcode=1
TESTER =
NASMFLAGS = -w+all -w+error
GDB_FLAGS = -x gdbinit
SHELL = /bin/bash
HEX=hexyl #hexdump -C

G_IN = ./testy/example1.in
G_OUT = ./testy/example1.out

SCOPY_VAR = faultyio.elf # scopy.elf

.PHONY: clean dump test tester maletesty faultyio

%.o: %.asm
	nasm -f elf64 $(NASMFLAGS) -o $@ $<

%.elf: %.o
	ld --fatal-warnings -o $@ $^

tester.elf: tester.cpp
	g++ -std=c++17 $^ -o $@

dump: scopy.o
	objdump -M intel -d $^
	size $^

test: $(SCOPY_VAR)
	rm -f ./example1.sout
	$(TESTER) ./$< $(G_IN) ./example1.sout
	diff --color <($(HEX) $(G_OUT)) <($(HEX) ./example1.sout)

maletesty: $(SCOPY_VAR)
	rm -f ./maletesty/output*
	for i in {0..10}; do $(TESTER) ./$< ./maletesty/test$$i ./maletesty/output$$i || echo "ERROR!"; \
		echo "Test $$i:"; \
		diff --color <($(HEX) ./maletesty/output$$i) <($(HEX) ./maletesty/answer$$i) || echo "ERROR!"; \
	done

tester: tester.elf scopy.elf
	./$<

rndtest: rndtest.elf
	for _ in {1..69}; do rm -f ./example1.sout; strace ./$<  $(G_IN) ./example1.sout || echo "ERROR!"; done

faultyio: faultyio.elf
	rm -f ./example1.sout
	strace ./$< $(G_IN) ./example1.sout
	diff --color <($(HEX) $(G_OUT)) <($(HEX) ./example1.sout)

gdb: $(SCOPY_VAR)
	gdb $(GDB_FLAGS) -ex "break _start" $<

strace: scopy.elf
	rm -f ./example1.sout
	strace ./$< $(G_IN) ./example1.sout

clean:
	rm *.o *.elf *.sout
