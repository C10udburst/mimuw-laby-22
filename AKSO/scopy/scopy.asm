global _start

; Definicje syscall'i:
SYS_EXIT  equ 60
SYS_OPEN  equ 2
SYS_READ equ 0
SYS_WRITE equ 1
SYS_CLOSE equ 3

; Ustawiamy rozmiar buforów tak aby wypełnić .bss całą stronę pamięci.
; Łącznie dostępne mamy 4096 bajtów, więc 4096 - 2 (na strażnika) = 4094.
; Źródło: https://github.com/torvalds/linux/blob/master/arch/x86/include/asm/page_types.h#L11.
; Prawdobodobieństwo wystąpienia 's' lub 'S' to 2/256 = 1/128, a dodanie 's' to maksymalnie 3 bajty.
; Więc na bufor do zapisu ustalam (1/128*3) ~ 2.3% miejsca, czyli 96 bajtów.
READ_BUFFER equ 3998
WRITE_BUFFER equ 96

; Definicje flag dla syscalla open:
; Źródło: bits/fcntl-linux.h.
O_WRONLY equ 1o          ; tylko do zapisu
O_CREAT equ 100o         ; utwórz nowy
O_EXCL equ 200o          ; błąd jeśli istnieje

; Uprawnienia nowo utworzonego pliku:
; Wygenerowane przez: https://www.nettools.club/chmod_calc.
FMOD equ 644o            ; rw-r--r--

; Nazwy rejestrów, ich znaczenie praktycznie nie zmienia się:
%define read_idx       r9    ; Obecny indeks w buforze infile
%define write_idx      r10   ; Obecny indeks w buforze outfile
%define read_size      r12   ; Rozmiar załadowanego kawałka infile
%define ns_count       r13   ; Licznik znaków != 's', 'S'
%define ns_count_mod   r13w  ; ns_count mod 2**16
%define infile_id      r14   ; Deskryptor pliku infile
%define outfile_id     r15   ; Deskryptor pliku outfile


section .bss

infile_buf: resb READ_BUFFER
outfile_buf: resb WRITE_BUFFER

; Jeśli skończy sie miejsce w outfile_buf, to pozostałe, maksymalnie 2 bajty (druga część ns_count i 's' czy 'S') wpiszą się tu.
woverflow: resw 1

; Makro do ustawiania kodu syscalla w rejestrze eax:
; %1 - kod syscalla
; Używam tego makra zamiast mov eax, SYS_XXX, ponieważ jest krótsze.
%macro set_syscall 1
  xor eax, eax
  mov al, %1
%endmacro

section .text

_start:
  cmp qword [rsp], 3    ; Jeśli argc < 3,
  jnz .exit1            ; to zakończ program z kodem 1.

  pop rax        ; Usuwamy ze stosu ilość argumentów
  pop rax        ; oraz arg[0] (nazwę programu).

  ; Stan stosu:
  ; [rsp]      infile_name
  ; [rsp + 8]  outfile_name

  set_syscall SYS_OPEN            ; open(infile_name, read)
  pop rdi                         ; Wczytaj nazwe infile do rdi.
  xor esi, esi                    ; esi = RDONLY = 0, tryb czytania
  syscall
  test rax, rax                   ; rax < 0 => wystąpił błąd
  js .exit1
  mov infile_id, rax              ; Ustaw deskryptor infile.

  set_syscall SYS_OPEN                     ; open(outfile_name, write|create|err_if_exists)
  pop rdi                                  ; Wczytaj nazwę outfile do rdi.
  mov esi, O_WRONLY | O_CREAT | O_EXCL     ; Utwórz plik to zapisywania, z błędem jeśli istnieje.
  mov edx, FMOD                            ; Ustaw uprawnienia pliku.
  syscall
  test rax, rax                            ; rax < 0 => wystąpił błąd
  js .err_infile_open
  mov outfile_id, rax                      ; Ustaw deskryptor outfile.

  mov read_idx,  READ_BUFFER + 2
  mov read_size, READ_BUFFER + 1
  xor write_idx, write_idx
  xor ns_count, ns_count

.loop:
  cmp write_idx, WRITE_BUFFER
  jb .write_buf_ok
  mov r8, WRITE_BUFFER               ; Ustal rozmiar bufora do zapisu.
  call .write_file                   ; Zapisz bufor do pliku.
  mov ax, word [rel woverflow]       ; Wczytaj strażnika.
  mov word [rel outfile_buf], ax     ; Wstaw strażnika do buforu.
  sub write_idx, WRITE_BUFFER        ; Przesuń write_idx do początku.
.write_buf_ok:

  cmp read_idx, read_size            ; Jeśli read_idx >= read_size to należy wczytać kolejny kawałek pliku.
  jb .read_buf_ok                    ; Wpw. nie trzeba.

  set_syscall SYS_READ               ; read(infile_id, infile_buf, READ_BUFFER)
  mov rdi, infile_id                 ; Wczytaj deskryptor infile do rdi.
  lea rsi, [rel infile_buf]          ; Wczytaj adres bufora do rsi.
  mov edx, READ_BUFFER               ; Wczytaj rozmiar bufora do rdx.
  syscall
  test rax, rax
  js .err_both_open                  ; Jeśli rozmiar < 0, to wystąpił błąd.
  jz .read_done                      ; Jeśli rozmiar == 0, to plik się skończył.
  mov read_size, rax                 ; Ustaw rozmiar wczytanej części pliku.
  xor read_idx, read_idx             ; Przesuń read_idx na początek buforu.
.read_buf_ok:

  ; W tym miejscu na pewno w infile został co najmniej jeden bajt,
  ; a w buforze outfile (ze strażnikiem) są 3 wolne bajty.

  mov dl, [abs infile_buf + read_idx]
  cmp dl, 's'                              ; Jeśli dl='s',
  je .is_s                                 ; to wpisz do bufora.
  cmp dl, 'S'                              ; Jeśli dl='S',
  je .is_s                                 ; to wpisz do bufora.
  inc ns_count                             ; Jeśli dl != 's' i dl != 'S' to zwiększ licznik.
  jmp .not_s
.is_s:
  call .write_ns_count                               ; Zapisz licznik do bufora.
  mov byte [abs outfile_buf + write_idx], dl         ; Wpisz 's' albo 'S' do bufora.
  inc write_idx                                      ; Ustaw następny bajt do zapisu.
.not_s:
  inc read_idx                                       ; Ustaw następny bajt do odczytu.
  jmp .loop

.read_done:

  call .write_ns_count               ; Zapisz licznik do bufora.

  test write_idx, write_idx
  jz .no_write                       ; Bufor zapisu jest pusty.

  mov r8, write_idx                  ; Ustaw rozmiar bufora do zapisu.
  call .write_file                   ; Zapisz bufor do pliku.

.no_write:

  xor dl, dl                   ; Od tego miejsca edx := $? (exit code).

.exit_close:                   ; Zamknij oba pliki i zakończ program.
  set_syscall SYS_CLOSE        ; close(outfile_id)
  mov rdi, outfile_id          ; Ustaw deskryptor outfile.
  syscall
  test rax, rax
  jns .outfile_closed          ; Jeśli udało się zamknąć outfile, to kod błędu pozostaje.
  mov dl, 1                    ; Nie udało się zamknąć outfile, ustaw kod błędu na 1.

.outfile_closed:               ; infile już zamknięty, więc wystarczy zamknąć outfile.
  set_syscall SYS_CLOSE        ; close(infile_id)
  mov rdi, infile_id           ; Ustaw deskryptor infile.
  syscall
  test rax, rax
  jns .infile_closed           ; Jeśli udało się zamknąć infile, kod błędu pozostaje.
.exit1:
  mov dl, 1
.infile_closed:
  set_syscall SYS_EXIT         ; exit(r12)
  movzx edi, dl                ; Rozszerz kod błędu do rdi.
  syscall

.err_infile_open:
  mov dl, 1
  jmp .outfile_closed

.err_both_open:
  mov dl, 1
  jmp .exit_close


; Metoda wpisuje ns_count do bufora outfile_buf, jeśli ns_count != 0.
; Po zapisaniu ns_count do bufora, zeruje ns_count i zwiększa write_idx o 2.
.write_ns_count:
  test ns_count, ns_count
  jz .no_ns_count                                               ; Jeśli ns_count == 0, to nie trzeba wpisywać.
  mov word [abs outfile_buf + write_idx], ns_count_mod          ; Wpisz ns_count do bufora.
  xor ns_count, ns_count                                        ; Wyzeruj ns_count.
  add write_idx, 2                                              ; Zwiększ write_idx o 2, bo wpisaliśmy 2 bajty.
.no_ns_count:
  ret

; Metoda zapisuje r8 bajtów z bufora outfile_buf do pliku outfile.
.write_file:
  mov rdi, outfile_id                               ; Wczytaj deskryptor outfile do rdi.
  lea rsi, [rel outfile_buf]                        ; Wczytaj adres bufora do rsi.
.write_loop:
  set_syscall SYS_WRITE                             ; write(outfile_id, outfile_buf, r8)
  mov rdx, r8                                       ; Wczytaj rozmiar bufora do zapisu do rdx.
  syscall
  test rax, rax
  js .err_both_open                                 ; Jeśli rax < 0, to wystąpił błąd.
  add rsi, rax                                      ; Przesuwamy wskaźnik bufora o tyle bajtów, ile zapisaliśmy.
  sub r8, rax                                       ; Dodajemy do licznika zapisanych bajtów ilość bajtów zapisanych w ostatnim wywołaniu.
  jnz .write_loop
  ret