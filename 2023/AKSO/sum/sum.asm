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
  mov first_fff, -1       ; first_fff = INFINITY
  xor i, i                ; i = 0
  xor carry, carry        ; carry = 0

.loop_start:              ; for(int i=0; i<n; i++)
  mov current, qword [x + 8*i]    ; Wczytuje current = x[i]
  mov qword [x + 8*i], 0          ; Ustawiam x[i] = 0

  ; jeśli carry jest puste, to nie trzeba go dodawać
  cmp carry, 0
  je .carry_empty

  ; fill_with_fff zapewnia, że w x[0...rax] rzeczywiście reprezentuje obecny y
  lea rdx, [rel .finish_fill_1] ; ustawienie adresu powrotu z .fill_with_fff
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

    ; Wyliczanie carry = (current < 0) ? -1 : 0
  test current, current       ; SF = current < 0
  setl cl                     ; cl = SF ? 1 : 0
  movzx carry, cl             ; carry = cl
  neg carry                   ; carry *= -1

  ; Wyliczanie który blok z x[] wybrać i o ile pomnożyć current
  mov rcx, rax
  and rcx, 64 - 1    ; rcx = rax % 64, przesunięcie, które jest mniejsze niż indeks
  shr rax, 6         ; rax = rax / 64, wybór indeksu

  ; mnożenie current przez 2^((64*i*i/n) % 64) i zapisywanie wyniku do [carry:current]
  ; ponieważ cl < 64 (bo rcx = rax % 64), to wiemy na pewno że znak zostanie zachowanym,
  ; bo zostanie przynajmniej 1 bit z pierwotnego carry
  shld carry, current, cl       ; przesuwamy cl bitów z current do carry
  shl current, cl               ; mnożymy current razy 2^cl

  lea rdx, [rel .finish_fill_2] ; ustawienie adresu powrotu z .fill_with_fff
  jmp .fill_with_fff            ; wypełnienie -1
.finish_fill_2:
  
  add qword [x + 8*rax], current ; y += current mod 64
  adc carry, 0                   ; carry += CF
  inc rax  ; rax trzyma adres poprzedniego dodawania, carry znajduje się o jeden indeks wyżej

  inc i

  ; jeśli i<n to kontynuuj pętle
  cmp i, n
  jb .loop_start

; dodawanie ostatniego carry
  lea rdx, [rel .finish_fill_3]
  jmp .fill_with_fff
.finish_fill_3:   ; zapewniamy, że y jest wypełniony -1 jeśli trzeba
  add qword [x + 8*rax], carry

  ret

; while(first_fff <= rax)
;     x[first_fff++] = -1
; jmp rdx
.fill_with_fff:
  cmp rax, first_fff
  jb .end_fill_fff            ; jeśli rax < first_fff, to nie trzeba nic wypełniać

.loop_fill_fff: ; while(first_fff <= rax)
  mov qword [x + 8*first_fff], -1  ; x[first_fff] = -1 = 0xfffffff...
  inc first_fff                    ; first_fff++
  cmp first_fff, rax
  jbe .loop_fill_fff         ; first_fff <= rax
.end_fill_fff:
  jmp rdx