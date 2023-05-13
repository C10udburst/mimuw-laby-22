global sum

%define n rsi
%define x rdi
%define i r8

; [carry:current] = x[i] * 2^(i*i*64/n)
%define current r9
%define carry r10

; r11 będzie trzymać od którego miejsca należy uzupełnić 0xffff... aby liczba x[] rzeczywiście trzymała wynik poprzednej operacji
%define first_fff r11

; rdx będzie trzymać do którego miejsca ma wrócić wywołanie .fill_with_fff
; 0 - powrót do .finish_fill_1
; -1 - powrót do .finish_fill_2
; wpw - kontynuuj, czyli dodaj ostanią resztę
%define return_mark rdx

section .text

sum:
  or first_fff, -1        ; first_fff = INFINITY
  xor i, i                ; i = 0
  xor carry, carry        ; carry = 0
  xor rax, rax

.loop_start:              ; for(int i=0; i<n; i++)

  ; wczytuję current = x[i] oraz ustawiam x[i] = 0
  xor current, current          ; current = 0
  xchg current, qword [x + 8*i] ; x[i] = 0, current = x[i]

  ; fill_with_fff zapewnia, że w x[0...rax] rzeczywiście reprezentuje obecny yf
  xor return_mark, return_mark  ; return_mark = 0, czyli .finish_fill_1
  jmp .fill_with_fff            ; wywołanie funkcji fill_with_fff
.finish_fill_1:

  add qword [x + 8*rax], carry

  ; first_fff = (SF) ? (rax + 1) : -1
  mov first_fff, -2         ; -2, gdyż potem będziemy dodawać 1 aby dostać -1 lub rax + 1
  cmovs first_fff, rax      ; jeśli SF to first_fff = rax
  inc first_fff             ; rax => rax + 1 lub -2 => -1

.carry_empty:

  mov rax, i    ; rax = i
  mul i         ; rax = i*i
  shl rax, 6    ; rax = 64*i*i
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

  or return_mark, -1   ; return_mark = -1, czyli .finish_fill_2
  jmp .fill_with_fff   ; wypełnienie -1
.finish_fill_2:
  
  add qword [x + 8*rax], current ; y += current mod 64
  adc carry, 0                   ; carry += CF
  inc rax  ; rax trzyma adres poprzedniego dodawania, carry znajduje się o jeden indeks wyżej

  inc i

  ; jeśli i<n to kontynuuj pętle
  cmp i, n
  jb .loop_start

  ; ustaw return_mark na wartość inną niż 0 lub -1 
  ; aby .fill_with_fff kontynuuowało
  mov return_mark, n

; while(first_fff <= rax)
;     x[first_fff++] = -1
; jmp rdx
.fill_with_fff:
  cmp rax, first_fff
  jb .end_fill_fff
  or qword [x + 8*first_fff], -1  ; x[first_fff] = -1 = 0xfffffff...
  inc first_fff                    ; first_fff++
  jmp .fill_with_fff
.end_fill_fff:
  test return_mark, return_mark
  jz .finish_fill_1
  js .finish_fill_2
  ; jeśli tu jesteśmy do znaczy że .fill_with_fff wywołane na samym końcu

; dodawanie ostaniego carry
  add qword [x + 8*rax], carry
  ret