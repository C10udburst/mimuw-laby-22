global sum

; dla wygody rejestry, których semantyka nie zmienia się znacząco
; w trakcie wykonywania programu zostały poniżej ponazywane

; w niektórych miejscach używamy 32bitowej wersji bo zajmuje mniej bajtów, a n jest < 2^29
%define n rsi
%define n_32 esi

; x to wskaźnik na tablicę x[]
%define x rdi
%define i r8

; [x2:x1] = x[i] * 2^(i*i*64/n)
%define x1 r9
%define x2 r10

; ostatni indeks, w którym trzymany jest y
%define y_len r11

section .text

sum:

  ; czyścimy wszystkie rejestry, które będą potrzebne
  xor i, i
  xor x1, x1
  xor x2, x2
  xor y_len, y_len
  xor eax, eax

.loop_start:    ; for(int i=0; i<n; i++)

  ; zapisujemy rcx = x[i] oraz x[i] = 0
  xor ecx, ecx
  xchg rcx, qword [x + 8*i]

  ; dodajemy liczbę wyliczoną w poprzednim obrocie pętli
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

  ; liczenie mnnożenia przez 2^rcx, ponieważ rcx < 64 to rcx = cl
  shld x2, x1, cl
  shl x1, cl  

  inc i
  cmp i, n
  jb .loop_start
  xor n_32, n_32  ; ustalamy n na 0, aby kod poniżej wiedział, że nie należy wracać do .added_prev
  dec i ; odejmujemy 1 aby w i trzymać ostatni indeks w tablicy

.add_x:
  add eax, 2 ; rax trzyma indeks trzeciej częsci x, a przedtem trzymał indeks pierwszej

  ; rdx = (y < 0) ? -1 : 0
  xchg qword [x + 8*y_len], rax ; zamieniamy najbardziej znaczące 64 bity y z rax
  cqo  ; wyliczamy znak y (obecnie rax) i wstawiamy do rdx 
  xchg qword [x + 8*y_len], rax ; przywracamy y i rax do pierwotnych wartości

; uzupełnianie y tak aby można było dodać x
.fill_y:
  cmp rax, y_len  ; jeśli ostatni fragment x się mieści,
  jbe .fill_end   ; to nie trzeba rozszerzać
  cmp y_len, i    ; jeśli y już wypełnił cały dostępny obszar
  je .fill_end    ; to nie da się już rozszerzyć
  ; trzeba jeszcze poszerzyć
  inc y_len       ; poszerzamy y
  mov qword [x + 8*y_len], rdx ; 0xffff lub 0 w zależności od znaku y
  jmp .fill_y    ; kontynuujemy pętle
.fill_end:
  
  ; rdx = (x2 < 0) ? -1 : 0
  xchg rax, x2  ; zamieniam rejestry, aby wykonać cqo
  cqo           ; ustalam wartość znaku w rdx
  xchg rax, x2  ; przywracam pierwotny stan

  ; dodawanie x[i]
  add qword [x + 8*rax - 16], x1   ; dodajemy najmniej znaczące 64 bity
  adc qword [x + 8*rax - 8], x2    ; dodajemy środkowe 64 bity
  adc rdx, 0   ; zapisuje wynik najbardziej znaczących bitów w rdx
  
  cmp y_len, rax   ; jeśli ostatnia część x nie mieści się w y
  jb .ignore_carry ; to jej nie dodajemy
  add qword [x + 8*rax], rdx ; dodajemy ostatnią część x
.ignore_carry:

.add_end:
  test n_32, n_32 ; jeśli n nie został wyzerowany, to pętla sie nie zakończyła
  jnz .added_prev ; wracamy do momentu wywołania 

  ; jeśli n został wyzerowany, to główna pętla programu zakończyła się
  ; wiemy, że cały y został wypełniony,
  ; bo ostatni rax = (floor(64*(n-1)*(n-1))/n)/64 = (floor((64 * n^2 - 2n + 1)/n)/64 + 2 > n
  ret  ; więc kończymy działanie funkcji