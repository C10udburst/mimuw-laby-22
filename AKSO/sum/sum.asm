; void sum(int64_t *x, size_t n):
; y = 0;
; for (i = 0; i < n; ++i)
;   y += x[i] * (2 ** floor(64 * i * i / n));
; x[0, ..., n-1] = y;

global sum
sum:
  mov r8, 1

  ; x := rdi
  ; n := rsi
  ; i := r8 = 1

.for_start:
  ; for (r8 = 0; r8 < rsi)
  cmp r8, rsi
  je .for_end

  mov rax, r8
  mul r8
  shl rax, 6
  div rsi

.potegowanie_start:
  cmp rax, 0
  je .potegowanie_end

  shl dword [rdi + 8*r8], 1

  mov r9, r8
  inc r9

.przesuwanie_bitow_start:
  jnc .przesuwanie_bitow_end
  cmp r9, rsi
  je .przesuwanie_bitow_end

  add dword [rdi + 8*r9], 1

  inc r9
  jmp .przesuwanie_bitow_start
.przesuwanie_bitow_end:

  dec rax
  jmp .potegowanie_start
.potegowanie_end:

  inc r8
  jmp .for_start
.for_end:
  ret