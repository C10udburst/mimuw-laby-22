global sum

section .text

sum:
  ; rsi = n
  ; rdi = x
  ; r8 = i
  ; r10 = abs(x[i])
  ; r9 = sgn(y)
  ; rax = 64*r8*r8/rsi lub 64*r8*r8/(8*rsi)
  ; rdx = y[rax + 8]
  ; r11 = sgn(x[i]) lub sgn(x[i]) != sgn(y) 

  ; jeśli n = 1 to nic nie trzeba robić
  cmp rsi, 1
  je .skip_everything

  ; rdx = 0, rax = 0
  xor rdx, rdx
  xor rax, rax
  xor r8, r8

  ; r9 = sgn(x[0])
  cmp qword [rdi], 0
  jl .if_neg_firstx
  mov r9, 0
  jmp .loop_start
.if_neg_firstx:
  mov r9, 1

.loop_start: ; for (int i := r8 = 1; i < n; i++)
  mov r10, [rdi + 8*r8] ; r10 = x[i]
  mov qword [rdi + 8*r8], 0 ; x[i] = 0

  ; r11 = (r10 > 0) ? 0 : 1; r10 = abs(r10)
  cmp r10, 0
  jl .if_neg_xi
  mov r11d, 0 ; r11 = 0
  jmp .endif_neg_xi
.if_neg_xi: ; else
  neg r10 ; r10 = -r10
  mov r11d, 1 ; r11 = 1
.endif_neg_xi:

  ; if rdx > 0 then [rdi + rax + 64] += rdx
  ; dodajemy to co zostało z poprzedniego działania do następnego 64 bloku
.adding_previous_carry:
  cmp rdx, 0
  je .no_prev_carry
  add rax, 8
  mov rcx, rax
  shr rcx, 6 ; rcx /= 64
  cmp rcx, rsi
  jge .no_prev_carry
  ; jeśli sie mieści w tablicy to powiększamy
  add [rdi + rax], rdx
.no_prev_carry:

  ; rax = 64*i*i/n
.calculating_power:
  mov rax, r8
  mul r8
  shl rax, 6 ; rax *= 64
  div rsi

  ; rcx = rax%8
.calculating_table_offset_from_power:
  mov rcx, rax
  and rcx, 0x7 ; trzy ostatnie bity
  shr rax, 3

  ; [rdx:r10] = r10<<rcx
  jrcxz .skip_shift_by_rcx
  xor rdx, rdx
.shift_by_rcx:
  shl rdx, 1
  shl r10, 1
  adc rdx, 0
  loop .shift_by_rcx
.skip_shift_by_rcx:

  ; r11d = sgn(y) !=  sgn(x[i])
.begin_actual_addition:
  xor r11d, r9d
  jnz .if_diff_sgn
.if_same_sgn:
  ; sgn(y) == sgn(s[i])
  add qword [rdi + rax], r10
  adc rdx, 0
  jmp .endif_same_sgn
.if_diff_sgn:
  ; sgn(y) != sgn(s[i])
  cmp rdx, 0
  je .no_carry
.has_carry:
  xor r9, 1   ; y = -y
  cmp r10, qword [rdi + rax]
  jle .has_carry_r10_less_equal
.has_carry_r10_bigger:
  sub r10, qword [rdi + rax]
  dec r10 
  mov qword [rdi + rax], r10
  jmp .sub_old_bits
.has_carry_r10_less_equal:
  dec rdx
  xor qword [rdi + rax], 0xffffffffffffffff
  add [rdi + rax], r10
  jmp .sub_old_bits

.no_carry:
  cmp r10, qword [rdi + rax]
  jle .no_carry_r10_less_equal
.no_carry_r10_bigger:
  xor r9, 1 ; y = -y
  sub r10, qword [rdi + rax]
  dec r10
  mov qword [rdi + rax], r10
  jmp .sub_old_bits
.no_carry_r10_less_equal:
  sub qword [rdi + rax], r10
.endif_has_carry:
.endif_same_sgn:

  jmp .loop_continue
.sub_old_bits:
  mov rcx, rax
  jrcxz .sub_old_bits_skip
  dec rcx
  jrcxz .sub_old_bits_end
  xor r11, r11 ; ostatni niezerowy bajt
.sub_old_bits_loop:
  cmp byte [rdi + rcx], 0
  je .sub_old_bits_zero
  mov r11, rcx
.sub_old_bits_zero:
  xor byte [rdi+rcx], 0xFF
  sub rcx, 1
  jns .sub_old_bits_loop
.sub_old_bits_end:
  inc rcx
.sub_old_bits_skip:
  inc byte [rdi+r11]

.loop_continue:
  inc r8
  cmp r8, rsi
  jne .loop_start
.loop_end:

.adding_last_carry:
  cmp rdx, 0
  je .no_last_carry
  add rax, 8
  mov rcx, rax
  shr rcx, 6 ; rcx /= 64
  cmp rcx, rsi
  jge .no_last_carry
  ; jeśli sie mieści w tablicy to powiększamy
  add [rdi + rax], rdx
.no_last_carry:

  and r9, 0x1
  jz .positive_y
.negative_y:
  xor r8, r8
  mov rcx, 1
.negative_y_loop:
  xor qword [rdi+8*r8], 0xffffffffffffffff
  jrcxz .negative_y_no_carry
  add qword [rdi+8*r8], 1
  jc .next_has_carry
.next_has_no_carry:
  xor rcx, rcx
.next_has_carry:
.negative_y_no_carry:
  inc r8
  cmp r8, rsi
  jl .negative_y_loop

.positive_y:
.skip_everything:
  ret