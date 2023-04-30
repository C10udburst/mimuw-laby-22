global sum


section .text

sum:
  ; jeśli n = 1 to nic nie trzeba robić
  cmp rsi, 1
  je .loop_end
  
  xor rax, rax
  mov r8, 1

  ; ustawiamy r9 na wartość jaką należy wypełnić miejsce w x[1]
  add qword [rdi], 0
  js .if_neg_first ; r9 = SF ? 0xFFFFF : 0
  xor r9, r9
  jmp .loop_start
.if_neg_first:
  mov r9, 0xFFFFFFFFFFFFFFFF

; for (int r8 = 1; r8 < rsi; r8++)
.loop_start:
  mov r10, qword [rdi + 8*r8]
  mov qword [rdi + 8*r8], r9

  mov rax, r8
  mul r8
  shl rax, 6
  div rsi

  ; rcx = rax%8
  mov rcx, rax
  and rcx, 0x7

  ; rax = rax/8
  shr rax, 3
  
  add qword [rdi+rax], r10

  ; r9 = y > 0 ? 0x00.. : 0xFF..
  js .if_neg
  xor r9, r9
  jmp .end_neg
.if_neg:
  mov r9, 0xFFFFFFFFFFFFFFFF
.end_neg:

  inc r8

  ; r8 < rsi
  cmp r8, rsi
  jl .loop_start

.loop_end:
  ret