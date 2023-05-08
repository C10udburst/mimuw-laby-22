global sum

%define n rsi
%define x rdi
%define i r8

; [carry:current] = x[i] * 2^(i*i*64/n)
%define current r9
%define carry r10

; r11 będzie trzymać od którego miejsca należy uzupełnić 0xffff... aby liczba x[] rzeczywiście trzymała wynik poprzednej operacji
%define first_fff r11

section .text

sum:
  ; jeśli tablica jest jednoelementowa, to nie trzeba nic robić
  cmp n, 1
  je .done

  mov first_fff, -1       ; first_fff = INFINITY
  xor i, i                ; i = 0
  xor carry, carry        ; carry = 0
.loop_start:              ; for(int i=0; i<n; i++)

  mov current, qword [x + 8*i]    ; Wczytuje current = x[i]
  mov qword [x + 8*i], 0          ; Ustawiam x[i] = 0

  ; jeśli carry jest puste, to nie trzeba go dodawać
  cmp carry, 0
  je .carry_empty

  inc rax

  lea rdx, [rel .finish_fill_1] ; ustawienie adresu powrotu z .fill_with_fff
  jmp .fill_with_fff
.finish_fill_1:

  add qword [x + 8*rax], carry

  js .y_negative
  mov first_fff, -1         ; first_fff = INFINITY, bo liczba jest dodatnia
  jmp .y_positive
.y_negative:
  lea first_fff, [rax + 1] ; first_fff = rax+1
.y_positive:

.carry_empty:

  mov rax, i    ; rax = i
  mul i         ; rax = i*i
  shl rax, 6    ; rax = 64*i*i
  div n         ; rax = 64*i*i / n

  ; Wyliczanie który blok z x[] wybrać i o ile pomnożyć current
  mov rcx, rax
  and rcx, 64 - 1    ; rcx = rax % 64
  shr rax, 6         ; rax = rax / 64

  ; Wyliczanie carry = (current < 0) ? -1 : 0
  test current, current       ; SF = current < 0
  js .current_negative        ; if current < 0
  xor carry, carry            ; carry = 0 if current >= 0 
  jmp .current_positive       ; if current >= 0 
.current_negative:
  mov carry, -1               ; carry = -1 if current < 0
.current_positive:

  ; mnożenie current przez 2^((64*i*i/n) % 64) i zapisywanie wyniku do [carry:current]
  ; ponieważ cl < 64, to wiemy na pewno że znak zostanie zachowanym,
  ; bo zostanie przynajmniej 1 bit z pierwotnego carry
  shld carry, current, cl       ; przesuwamy cl bitów z current do carry
  shl current, cl               ; mnożymy current razy 2^cl

  lea rdx, [rel .finish_fill_2] ; ustawienie adresu powrotu z .fill_with_fff
  jmp .fill_with_fff            ; wypełnienie -1
.finish_fill_2:
  
  add qword [x + 8*rax], current ; y += current mod 64
  adc carry, 0                   ; carry += CF

  inc i       ; i++

  ; jeśli i<n to kontynuuj pętle
  cmp i, n
  jb .loop_start

; dodawanie ostatniego carry
  inc rax
  lea rdx, [rel .finish_fill_3]
  jmp .fill_with_fff
.finish_fill_3:
  add qword [x + 8*rax], carry

.done:
  ret

; for(rcx = first_fff; rcx <= rax; rcx++)
;     x[rcx] = -1
; jmp rdx
.fill_with_fff:
; rax = max(rax, n-1)
  cmp rax, n
  jb .rax_smaller_t_n
  lea rax, [n - 1]
.rax_smaller_t_n:

  cmp rax, first_fff
  jb .end_fill_fff            ; jeśli rax < first_fff, to nie trzeba nic wypełniać

  mov rcx, first_fff
.loop_fill_fff: ; for(rcx = first_fff; rcx <= rax; rcx++)
  mov qword [x + 8*rcx], -1  ; x[rcx] = -1 = 0xfffffff...
  inc rcx                    ; rcx++
  cmp rcx, rax
  jbe .loop_fill_fff         ; rcx <= rax

  ; ponieważ wypełniliśmy wszystko od first_fff do rax włącznie
  ; to następny niezmieniony blok to rax + 1
  lea first_fff, [rax + 1]   ; first_fff = rax + 1
.end_fill_fff:
  jmp rdx