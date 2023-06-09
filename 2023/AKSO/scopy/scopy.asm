global _start

; definicje syscall
SYS_EXIT  equ 60
SYS_OPEN  equ 2
SYS_READ equ 0
SYS_WRITE equ 1
SYS_CLOSE equ 3

; ustawiamy rozmiar buforów tak aby wypełnić .bss całą stronę pamięci
; https://github.com/torvalds/linux/blob/master/arch/x86/include/asm/page_types.h#L11
; łącznie dostępne mamy 4096 bajtów, więc 4096 - 2 (na strażnika) = 4094
; prawdobodobieństwo wystąpienia 's' lub 'S' to 2/256 = 1/128 a dodanie 's' to maksymalnie 3 bajty
; więc na bufor do zapisu ustalam (1/128*3) ~ 2.3% miejsca, czyli 96 bajtów
READ_BUFFER equ 3998
WRITE_BUFFER equ 96

; bits/fcntl-linux.h
; https://github.com/bminor/glibc/blob/master/bits/fcntl.h
O_WRONLY equ 1o          ; tylko do zapisu
O_CREAT equ 100o         ; utwórz nowy
O_EXCL equ 200o          ; błąd jeśli istnieje

; uprawnienia nowo utworzonego pliku
; https://www.nettools.club/chmod_calc
FMOD equ 664o            ; rw-r--r--

; nazwy rejestrów, ich znaczenie praktycznie nie zmienia się
%define read_idx       r9    ; obecny indeks w buforze infile
%define write_idx      r10   ; obecny indeks w buforze outfile
%define read_size      r12   ; rozmiar załadowanego kawałka infile
%define ns_count       r13   ; licznik znaków != 's', 'S'
%define ns_count_mod   r13w  ; ns_count mod 2**16
%define infile_id      r14   ; deskryptor pliku infile
%define outfile_id     r15   ; deskryptor pliku outfile


section .bss

infile_buf: resb READ_BUFFER
outfile_buf: resb WRITE_BUFFER

; jeśli skończy sie miejsce w outfile_buf, to pozostałe, maksymalnie 2 bajty (druga część ns_count i 's' czy 'S') wpiszą się tu
woverflow: resw 1

section .text

_start:
  cmp qword [rsp], 3
  jnz .exit1
  add rsp, 16         ; usuwamy ze stosu ilość argumentów i arg[0] (nazwę programu)

  ; stan stosu:
  ; [rsp]      infile_name
  ; [rsp + 8]  outfile_name

  xor eax, eax
  mov al, SYS_OPEN                ; open(infile_name, read)
  pop rdi                         ; wczytaj nazwe infile do rdi
  xor esi, esi                    ; esi = RDONLY = 0, tryb czytania
  syscall
  test rax, rax                   ; rax < 0 => wystąpił błąd
  js .exit1
  mov infile_id, rax              ; deskryptor infile

  xor eax, eax
  mov al, SYS_OPEN                         ; open(outfile_name, write|create|err_if_exists)
  pop rdi                                  ; wczytaj nazwę outfile do rdi
  mov esi, O_WRONLY | O_CREAT | O_EXCL     ; utwórz plik to zapisywania, z błędem jeśli istnieje
  mov edx, FMOD                            ; ustaw uprawnienia pliku
  syscall
  test rax, rax                            ; rax < 0 => wystąpił błąd
  js .err_infile_open
  mov outfile_id, rax                      ; deskryptor outfile

  mov read_idx,  READ_BUFFER + 2
  mov read_size, READ_BUFFER + 1
  xor write_idx, write_idx
  xor ns_count, ns_count

.loop:
  cmp write_idx, WRITE_BUFFER
  jb .write_buf_ok
  ; należy przesunąć bufor outfile
  xor r8, r8                         ; będziemy używać r8 jako licznika zapisanych bajtów
.write_loop:
  xor eax, eax
  mov al, SYS_WRITE
  mov rdi, outfile_id                ; wczytaj deskryptor outfile do rdi
  lea rsi, [abs outfile_buf + r8]    ; wczytaj adres bufora do rsi z przesunięciem o zapisane już bajty
  mov rdx, WRITE_BUFFER
  sub rdx, r8                        ; wczytaj rozmiar bufora z przesunięciem o zapisane już bajty
  syscall
  test rax, rax
  js .err_both_open                  ; jeśli rax < 0 to wystąpił błąd
  add r8, rax                        ; dodajemy do licznika zapisanych bajtów ilość bajtów zapisanych w ostatnim wywołaniu
  cmp r8, WRITE_BUFFER               ; jeśli zapisano mniej niż WRITE_BUFFER to trzeba próbować zapisać jeszcze raz
  jb .write_loop
  mov ax, word [rel woverflow]       ; wczytaj strażnika
  mov word [rel outfile_buf], ax     ; wstaw strażnika do buforu
  sub write_idx, WRITE_BUFFER        ; przesuń write_idx do początku  
.write_buf_ok:

  cmp read_idx, read_size            ; jeśli read_idx >= read_size to należy wczytać kolejny kawałek pliku
  jb .read_buf_ok                    ; wpw. nie trzeba

  xor eax, eax
  mov al, SYS_READ                   ; read(infile_id, infile_buf, READ_BUFFER)
  mov rdi, infile_id                 ; wczytaj deskryptor infile do rdi
  lea rsi, [rel infile_buf]          ; wczytaj adres bufora do rsi
  mov edx, READ_BUFFER               ; wczytaj rozmiar bufora do rdx
  syscall
  test rax, rax
  js .err_both_open                  ; jeśli rozmiar < 0 to wystąpił błąd
  jz .read_done                      ; jeśli rozmiar == 0 to plik się skończył 
  mov read_size, rax                 ; ustaw rozmiar wczytanej części pliku
  xor read_idx, read_idx             ; przesuń read_idx na początek buforu
.read_buf_ok:

  ; w tym miejscu na pewno w infile został co najmniej jeden bajt
  ; a w buforze outfile (ze strażnikiem) są 3 wolne bajty
  mov dl, [abs infile_buf + read_idx]
  cmp dl, 's'
  je .is_s                                 ; jeśli 's' to wpisz do bufora
  cmp dl, 'S'
  je .is_s                                 ; jeśli 'S' to wpisz do bufora
  inc ns_count                             ; jeśli dl != 's' i dl != 'S' to zwiększ licznik
  jmp .not_s
.is_s:
  test ns_count, ns_count
  jz .no_counter                                         ; jeśli ns_count == 0 to nie ma co wpisywać
  mov word [abs outfile_buf + write_idx], ns_count_mod   ; wpisz ns_count do bufora
  xor ns_count, ns_count                                 ; ns_count = 0
  add write_idx, 2                                       ; ustaw następny bajt do zapisu (+2 bo word = 2)
.no_counter:
  mov byte [abs outfile_buf + write_idx], dl         ; wpisz 's' albo 'S' do bufora
  inc write_idx                                      ; ustaw następny bajt do zapisu
.not_s:
  inc read_idx                                       ; ustaw następny bajt do odczytu
  jmp .loop

.read_done:

  test ns_count, ns_count
  jz .no_last_counter                                     ; jeśli ns_count == 0 to nie ma co wpisywać
  mov word [abs outfile_buf + write_idx], ns_count_mod    ; wpisz ns_count do bufora
  xor ns_count, ns_count                                  ; ns_count = 0
  add write_idx, 2                                        ; ustaw następny bajt do zapisu

.no_last_counter:

  test write_idx, write_idx
  jz .no_write                       ; bufor zapisu jest pusty

  xor r8, r8                         ; będziemy używać r8 jako licznika zapisanych bajtów
.write_loop_last:
  xor eax, eax
  mov al, SYS_WRITE                  ; write(outfile_id, outfile_buf, write_idx)
  mov rdi, outfile_id                ; wczytaj deskryptor outfile do rdi
  lea rsi, [abs outfile_buf + r8]    ; wczytaj adres bufora do rsi z przesunięciem o zapisane już bajty
  mov rdx, write_idx
  sub rdx, r8                        ; wczytaj rozmiar bufora z przesunięciem o zapisane już bajty
  syscall
  test rax, rax                      ; jeśli rax < 0 to wystąpił błąd
  js .err_both_open                  ; trzeba zamknąć oba pliki z błędem
  add r8, rax                        ; dodajemy do licznika zapisanych bajtów ilość bajtów zapisanych w ostatnim wywołaniu
  cmp r8, write_idx                  ; jeśli zapisano mniej niż write_idx to trzeba próbować zapisać jeszcze raz
  jb .write_loop_last

.no_write:

  xor dl, dl                   ; od tego miejsca edx := $? (exit code)

.exit_close:                   ; zamknij oba pliki i zakończ program
  xor eax, eax
  mov al, SYS_CLOSE            ; close(outfile_id)
  mov rdi, outfile_id          ; ustaw deskryptor outfile
  syscall
  test rax, rax
  jns .outfile_closed          ; jeśli udało się zamknąć outfile, to kod błędu pozostaje
  mov dl, 1                    ; nie udało się zamknąć outfile, ustaw kod błędu na 1

.outfile_closed:               ; infile już zamknięty, więc wystarczy zamknąć outfile
  xor eax, eax
  mov al, SYS_CLOSE            ; close(infile_id)
  mov rdi, infile_id           ; ustaw deskryptor infile
  syscall
  test rax, rax
  jns .infile_closed           ; jeśli udało się zamknąć infile, kod błędu pozostaje
.exit1:
  mov dl, 1
.infile_closed:
  xor eax, eax
  mov al, SYS_EXIT             ; exit(r12)
  movzx edi, dl                ; wczytaj kod błędu do edi
  syscall

.err_infile_open:
  mov dl, 1
  jmp .outfile_closed

.err_both_open:
  mov dl, 1
  jmp .exit_close

