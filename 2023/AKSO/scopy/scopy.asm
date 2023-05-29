global _start

; definicje syscall
SYS_EXIT  equ 60
SYS_OPEN  equ 2
SYS_READ equ 0
SYS_WRITE equ 1
SYS_CLOSE equ 3

; rozmiary buforów
READ_BUFFER equ 1084
WRITE_BUFFER equ 573

; bits/fcntl-linux.h
O_WRONLY equ 1o        ; tylko do zapisu
O_CREAT equ 100o       ; utwórz nowy
O_EXCL equ 200o        ; błąd jeśli istnieje

; uprawnienia nowo utworzonego pliku
; https://www.nettools.club/chmod_calc
FMOD equ 664o ; rw-r--r--

; nazwy rejestrów, ich znaczenie praktycznie nie zmienia się
%define read_idx   r9    ; obecny indeks w buforze infile
%define write_idx  r10   ; obecny indeks w buforze outfile
%define read_size  r12   ; rozmiar załadowanego kawałka infile
%define ns_count   r13w  ; licznik znaków != 's', 'S'
%define infile_id  r14   ; deskryptor pliku infile
%define outfile_id r15   ; deskryptor pliku outfile

; makro do sprawdzania czy syscall zwrócił błąd, jeśli tak to skocz do etykiety
%macro jmp_syscall_err 1
  cmp rax, -4095
  jae %1
%endmacro

section .bss

infile_buf: resb READ_BUFFER

outfile_buf: resb WRITE_BUFFER

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
  jmp_syscall_err .err_infile_open         ; błąd, trzeba zamknąć infile
  mov outfile_id, rax                      ; deskryptor outfile

  mov read_idx,  READ_BUFFER + 2
  mov read_size, READ_BUFFER + 1
  xor write_idx, write_idx
  xor ns_count, ns_count

.loop:
  cmp write_idx, WRITE_BUFFER
  jb .write_buf_ok
  ; należy przesunąć bufor outfile
  xor eax, eax
  mov al, SYS_WRITE
  mov rdi, outfile_id                ; wczytaj deskryptor outfile do rdi
  mov rsi, outfile_buf               ; wczytaj adres bufora do rsi
  mov edx, WRITE_BUFFER              ; wczytaj rozmiar bufora do rdx
  syscall
  jmp_syscall_err .err_both_open     ; trzeba zamknąć oba pliki z błędem 
  mov ax, word [abs woverflow]       ; wczytaj strażnika
  mov word [abs outfile_buf], ax     ; wstaw strażnika do buforu
  sub write_idx, WRITE_BUFFER        ; przesuń write_idx do początku  
.write_buf_ok:

  cmp read_idx, read_size            ; jeśli read_idx >= read_size to należy wczytać kolejny kawałek pliku
  jb .read_buf_ok                    ; wpw. nie trzeba
  cmp read_size, READ_BUFFER         ; jeśli read_size < READ_BUFFER to doszliśmy do końca pliku
  jb .read_done                      ; wpw. należy przesunąć bufor infile
  
  xor eax, eax
  mov al, SYS_READ                   ; read(infile_id, infile_buf, READ_BUFFER)
  mov rdi, infile_id                 ; wczytaj deskryptor infile do rdi
  mov rsi, infile_buf                ; wczytaj adres bufora do rsi
  mov edx, READ_BUFFER               ; wczytaj rozmiar bufora do rdx
  syscall
  jmp_syscall_err .err_both_open     ; trzeba zamknąć oba pliki z błędem
  jz .read_done                      ; jeśli rozmiar == 0 to plik się skończył 
  mov read_size, rax                 ; ustaw rozmiar wczytanej części pliku
  xor read_idx, read_idx             ; przesuń read_idx na początek buforu
.read_buf_ok:

  ; w tym miejscu na pewno w infile został co najmniej jeden bajt
  ; i na pewno w buforze outfile (ze strażnikiem) są 3 wolne bajty
  mov dl, [abs infile_buf + read_idx]
  xor dl, 's'                              ; jeśli dl jest s lub S to wszystkie bity = 0 poza 2^15
  test dl, 1011111b                        ; s i S różnią się jedynie przedostatnim bitem
  jz .is_s                                 ; jeśli dl == 's' lub 'S' to skocz do .is_s 
  inc ns_count                             ; jeśli dl != 's' i dl != 'S' to zwiększ licznik
  jmp .not_s
.is_s:
  test ns_count, ns_count
  jz .no_counter                                     ; jeśli ns_count == 0 to nie ma co wpisywać
  mov word [abs outfile_buf + write_idx], ns_count   ; wpisz ns_count do bufora
  xor ns_count, ns_count                             ; ns_count = 0
  add write_idx, 2                                   ; ustaw następny bajt do zapisu (+2 bo word = 2)
.no_counter:
  xor dl, 's'                                        ; przywróć dl do 's' albo 'S'
  mov byte [abs outfile_buf + write_idx], dl         ; wpisz 's' albo 'S' do bufora
  inc write_idx                                      ; ustaw następny bajt do zapisu
.not_s:
  inc read_idx
  jmp .loop

.read_done:

  test ns_count, ns_count
  jz .no_last_counter                                 ; jeśli ns_count == 0 to nie ma co wpisywać
  mov word [abs outfile_buf + write_idx], ns_count    ; wpisz ns_count do bufora
  xor ns_count, ns_count                              ; ns_count = 0
  add write_idx, 2                                    ; ustaw następny bajt do zapisu

.no_last_counter:

  test write_idx, write_idx
  jz .no_write                       ; bufor zapisu jest pusty

  xor eax, eax
  mov al, SYS_WRITE                  ; write(outfile_id, outfile_buf, write_idx)
  mov rdi, outfile_id                ; wczytaj deskryptor outfile do rdi
  mov rsi, outfile_buf               ; wczytaj adres bufora do rsi
  mov rdx, write_idx                 ; wczytaj rozmiar bufora do rdx
  syscall
  jmp_syscall_err .err_both_open     ; trzeba zamknąć oba pliki z błędem

.no_write:

  xor r12, r12                 ; od tego miejsca r12 := $? (exit code)

.exit_close:                   ; zamknij oba pliki i zakończ program
  xor eax, eax
  mov al, SYS_CLOSE            ; close(outfile_id)
  mov rdi, outfile_id          ; ustaw deskryptor outfile
  syscall
  test rax, rax
  jns .outfile_closed          ; jeśli udało się zamknąć outfile, to kod błędu pozostaje
  mov r12, 1                   ; nie udało się zamknąć outfile, ustaw kod błędu na 1

.outfile_closed:               ; infile już zamknięty, więc wystarczy zamknąć outfile
  xor eax, eax
  mov al, SYS_CLOSE            ; close(infile_id)
  mov rdi, infile_id           ; ustaw deskryptor infile
  syscall
  test rax, rax
  jns .infile_closed           ; jeśli udało się zamknąć infile, kod błędu pozostaje
.exit1:
  mov r12, 1
.infile_closed:
  mov edi, r12d                ; wczytaj kod błędu do edi
  xor eax, eax
  mov al, SYS_EXIT             ; exit(r12)
  syscall

.err_infile_open:
  mov r12, 1
  jmp .outfile_closed

.err_both_open:
  mov r12, 1
  jmp .exit_close

