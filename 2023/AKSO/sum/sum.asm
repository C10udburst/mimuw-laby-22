global sum


section .text

sum:
  xor r11, r11
  xor rax, rax
  mov r8, 1

  cmp rsi, 1
  je .loop_end
  
  add qword [rdi], 0 

; for (int r8 = 1; r8 < rsi; r8++)
.loop_start:
  mov r10, qword [rdi + 8*r8]
  js .if_neg_prev
  mov qword [rdi + 8*r8], 0

; [rdi + r8] = SF ? 0xFFF... : 0
  jmp .endif_neg_ref
.if_neg_prev:
  mov qword [rdi + 8*r8], 0xFFFFFFFFFFFFFFFF
.endif_neg_ref:

  add [rdi+r8+1], r11

  mov rax, r8
  mul r8
  shl rax, 6
  div rsi

  ; rcx = rax%8
  mov rcx, rax
  and rcx, 0x7

  ; rax = rax/8
  shr rax, 3

  ; TODO
  xor r11, r11
  shl r10, cl
  adc r11, 0

  
  add qword [rdi+rax], r10

  inc r8

  ; r8 < rsi
  cmp r8, rsi
  jl .loop_start

.loop_end:
  ret