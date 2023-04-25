; void sum(int64_t *x, size_t n):
; y = 0;
; for (i = 0; i < n; ++i)
;   y += x[i] * (2 ** floor(64 * i * i / n));
; x[0, ..., n-1] = y;

global sum
sum:
  xor r8, r8
  mov r9, rdx

  ; x := rcx
  ; n := r9
  ; i := r8 = 0

  cmp r9, 1
  je .ostatnie_przejscie ; n=1

.petla_for:
  mov rax, r8

  test rax, rax
  jz .reszta_fora ; jeśli i*i*64/n == 0 to nie potęgujemy

  mul r8
  shl rax, 6 ; 2**6 = 64
  ; rax = i*i*64
  
  xor rdx, rdx ; rdx = 0, czy potrzebne?
  div r9 ; rax = i*i*64/n

.liczenie_potegi:
  shl qword [rdi+r8*8], 1 ; x[i] *= 2
  adc qword [rdi+8+r8*8], 0 ; x[i+1] += CF
  dec rax
  jnz .liczenie_potegi

.reszta_fora:
  inc r8
  mov rax, r9
  sub rax, r8
  cmp rax, 2
  jne .petla_for

.ostatnie_przejscie:
  mov rax, r8
  
  test rax, rax
  jz .end

  mul r8
  shl rax, 6 ; 2**6 = 64
  
  xor rdx, rdx ; rdx = 0, czy potrzebne?
  div r9 ; rax = i*i*64/n

.liczenie_potegi_2:
  shl qword [rdi+r8*8], 1 ; x[i] *= 2
  dec rax
  jnz .liczenie_potegi_2
  
.end:
  ret