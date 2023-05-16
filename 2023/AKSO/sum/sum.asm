global sum

%define n rsi
%define x rdi
%define i r8

%define current r9
%define carry r10
%define next r11

section .text

sum:
  movq xmm0, r12
  xor i, i             ; i = 0
  or rax, -1           ; rax = INFTY
  xor next, next       ; next = 0
  xor carry, carry     ; carry = 0
  xor current, current ; current = 0 
.loop_start: ; for(int i=0; i<n; i++)

  ; wczytuję next = x[i] oraz ustawiam x[i] = 0
  xchg next, qword [x + 8*i] ; x[i] = 0, next = x[i]

  xor rdx, rdx
  jmp .add_start
.ret_carry1:
  xchg next, current

  mov rax, i    ; rax = i < 2^29
  mul i         ; rax = i*i < 2^58
  shl rax, 6    ; rax = 64*i*i < 2^64
  div n         ; rax = 64*i*i / n

  ; wyliczanie carry = (current < 0) ? -1 : 0
  xor carry, carry            ; carry = 0
  test current, current       ; SF = current < 0
  jns .current_positive       ; SF > 0
  or carry, -1                ; if (SF > 0) carry = -1
.current_positive:

  ; Wyliczanie który blok z x[] wybrać i o ile pomnożyć current
  mov rcx, rax
  and rcx, 64 - 1    ; rcx = rax % 64, przesunięcie, które jest mniejsze niż indeks
  shr rax, 6         ; rax = rax / 64, wybór indeksu

  ; mnożenie current przez 2^((64*i*i/n) % 64) i zapisywanie wyniku do [carry:current]
  ; ponieważ cl < 64 (bo rcx = rax % 64), to wiemy na pewno że znak zostanie zachowanym,
  ; bo zostanie przynajmniej 1 bit z pierwotnego carry
  shld carry, current, cl       ; przesuwamy cl bitów z current do carry
  shl current, cl               ; mnożymy current razy 2^cl

  inc i

  ; jeśli i<n to kontynuuj pętle
  cmp i, n
  jb .loop_start
  or rdx, -1
  dec i     ; i = n-1, ponieważ rax <= i => rax < n

.add_start:
  xor rcx, rcx
  test carry, carry
  jns .carry_positive
  or rcx, -1
.carry_positive:
  xor r12, r12
.add_loop: ; y[rax...i] += [current:carry]
  ; jeśli rax jest większy niż obecny rozmiar y[0..i], to go nie dodajemy
  cmp rax, i
  ja .add_end

  ; dodawanie i liczenie carry
  add qword [x + 8*rax], current
  adc carry, r12
  jnc .ncc
;.cc:
  mov r12, 1
  jmp .cc
.ncc:
  xor r12, r12
.cc:
  ; przesuwanie bufora liczby o jeden w prawo
  mov current, carry     ; current = carry
  mov carry, rcx

  inc rax
  jmp .add_loop

.add_end:
  test rdx, rdx
  jz .ret_carry1

  movq r12, xmm0
  ret