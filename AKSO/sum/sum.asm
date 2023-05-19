global sum

%define n rsi
%define n_32 esi
%define x rdi
%define i r8

; [x2:x1] = x[i] * 2^(i*i*64/n)
%define x1 r9
%define x2 r10
%define y_len r11

section .text

sum:
  xor i, i
  xor x1, x1
  xor x2, x2
  xor y_len, y_len
  xor eax, eax

.loop_start:    ; for(int i=0; i<n; i++)
  xor ecx, ecx
  xchg rcx, qword [x + 8*i]

  jmp .add_x
.added_prev:

  ; ustawia [x2:x1] = x[i]
  mov rax, rcx  ; wpisuje x[i] do rax
  cqo           ; rozszerza znak x[i] do rdx
  mov x1, rax   ; ustala x1 na pierwszą cześć x[i]
  mov x2, rdx   ; ustala x2 na pierwszą cześć x[i]

  mov rax, i    ; rax = i
  mul i         ; rax = i*i, rdx = 0
  shl rax, 6    ; rax = 64*i*i
  div n         ; rax = 64*i*i / n

  ; Wyliczanie który blok z x[] wybrać i o ile pomnożyć current
  mov ecx, eax
  and ecx, 64 - 1    ; rcx = rax % 64, przesunięcie, które jest mniejsze niż indeks
  shr rax, 6         ; rax = rax / 64, wybór indeksu

  ; liczenie mnnożenia przez 2^cl
  shld x2, x1, cl
  shl x1, cl  

  inc i
  cmp i, n
  jb .loop_start
  xor n_32, n_32
  dec i

.add_x:
  add eax, 2

  ; rdx = (y < 0) ? -1 : 0
  xchg qword [x + 8*y_len], rax
  cqo
  xchg qword [x + 8*y_len], rax

.fill_y:
  cmp rax, y_len
  jbe .fill_end
  cmp y_len, i
  je .fill_end
  inc y_len
  mov qword [x + 8*y_len], rdx
  jmp .fill_y
.fill_end:
  
  ; rdx = (x2 < 0) ? -1 : 0
  xchg rax, x2
  cqo
  xchg rax, x2

  ; dodawanie x[i]
  add qword [x + 8*rax - 16], x1
  adc qword [x + 8*rax - 8], x2
  adc rdx, 0
  
  cmp y_len, rax
  jb .ignore_carry
  add qword [x + 8*rax], rdx
.ignore_carry:

.add_end:
  test n_32, n_32
  jnz .added_prev

  ret