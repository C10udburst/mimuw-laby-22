global sum

%define n rsi
%define x rdi
%define i r8

; [x1:x2:x3] = x[i] * 2^(i*i*64/n)
%define x1 r9
%define x2 r10
%define y_len r11

section .text

sum:
  xor i, i
  xor x1, x1
  xor x2, x2
  xor y_len, y_len
  xor rax, rax

  cmp n, 2
  jb .skip

.loop_start:    ; for(int i=0; i<n; i++)
  xor rdx, rdx
  xchg rdx, qword [x + 8*i]

  jmp .add_x
.added_prev:

  mov x1, rdx
  xor x2, x2
  test x1, x1
  jns .pos_x1
  or x2, -1
.pos_x1:

  mov rax, i    ; rax = i
  mul i         ; rax = i*i
  shl rax, 6    ; rax = 64*i*i
  div n         ; rax = 64*i*i / n

  ; Wyliczanie który blok z x[] wybrać i o ile pomnożyć current
  mov rcx, rax
  and rcx, 64 - 1    ; rcx = rax % 64, przesunięcie, które jest mniejsze niż indeks
  shr rax, 6         ; rax = rax / 64, wybór indeksu

  shld x2, x1, cl
  shl x1, cl  

  inc i
  cmp i, n
  jb .loop_start
  xor n, n

.add_x:
  inc rax

.extend_y:
  test qword [x + 8*y_len], -1
  jns .y_positive
.fill_y: ; ujemne y
  cmp rax, y_len
  jbe .extend_done
  inc y_len
  or qword[x + 8*y_len], -1
  jmp .fill_y
.y_positive:
  cmp rax, y_len
  cmova y_len, rax
.extend_done:
  add qword [x + 8*rax - 8], x1
  adc qword [x + 8*rax], x2
  ; carry missing?

.add_end:
  test n, n
  jnz .added_prev

.skip:
  ret

