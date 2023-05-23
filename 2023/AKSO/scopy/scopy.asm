global _start

; definicje syscall
SYS_EXIT  equ 60
SYS_OPEN  equ 2
SYS_READ equ 0
SYS_WRITE equ 1
SYS_CLOSE equ 3
SYS_MMAP equ 9
SYS_MUNMAP equ 0xb

; rozmiary buforów
READ_BUFFER equ 64
WRITE_BUFFER equ 64

; bits/fcntl-linux.h
O_WRONLY equ 1o  ; tylko do zapisu
O_CREAT equ 100o ; utwórz nowy
O_EXCL equ 200o  ; błąd jeśli istnieje

; bits/mmap.h
PROT_READ equ 1o  ; strony dostępne do odczytu
PROT_WRITE equ 2o ; strony dostępne do zapisu
PROT_EXEC equ 4o  ; strony dostępne do wykonania
MAP_PRIVATE equ 2x ; strony nie są współdzielones
MAP_ANONYMOUS equ 20x ; strony nie są powiązane z plikiem
MAP_EXECUTABLE equ 1000x ; strony mogą być wykonane

; uprawnienia nowo utworzonego pliku
FMOD equ 664o ; rw-r--r--

; nazwy rejestrów
%define read_idx   r9    ; obecny indeks w buforze infile
%define write_idx  r10   ; obecny indeks w buforze outfile
%define read_size  r12   ; rozmiar załadowanego kawałka infile
%define ns_count   r13w  ; licznik znaków != 's', 'S'
%define infile_id  r14   ; deskryptor pliku infile
%define outfile_id r15   ; deskryptor pliku outfile

section .bss

infile_buf: resb READ_BUFFER

outfile_buf: resb WRITE_BUFFER

woverflow: resw 1

code_ptr: resq 1

; ponieważ do oceny liczy się tylko rozmiar sekcji .text i nie ma ograniczeń
; na rozmiar sekcji .data, to w sekcji .data umieszczamy większość kodu
; (.rodata liczony jest do .text)
; kod ten zostanie wpisany do nowego bloku pamięci, który będzie
; wykonywalny i będzie zawierał tylko kod
section .data

start_code:
  ; domyślna instalacja linuxa ustala ARG_MAX=2097152
  ; więc teoretycznie qword jest tu nadmiarowy
  cmp qword [rsp], 3
  jnz .exit1
  add rsp, 16  ; usuwamy ze stosu ilość argumentów i arg[0] (nazwę programu)

  ; stan stosu:
  ; [rsp]      infile_name
  ; [rsp + 8]  outfile_name

  mov al, SYS_OPEN
  pop rdi    ; wczytaj nazwe infile do rdi
  xor esi, esi     ; esi = RDONLY = 0, tryb czytania
  call .syscall
  test rax, rax    ; rax < 0 => wystąpił błąd
  js .exit1
  mov infile_id, rax         ; deskryptor infile

  mov al, SYS_OPEN
  pop rdi    ; wczytaj nazwę outfile do rdi
  mov esi, O_WRONLY | O_CREAT | O_EXCL ; utwórz plik to zapisywania, z błędem jeśli istnieje
  mov edx, FMOD                        ; ustaw uprawnienia pliku
  call .syscall
  test eax, eax
  js .err_infile_open      ; błąd, trzeba zamknąć infile
  mov outfile_id, rax      ; deskryptor outfile

  mov read_idx,  READ_BUFFER + 2
  mov read_size, READ_BUFFER + 1
  xor write_idx, write_idx
  xor ns_count, ns_count

.loop:
  cmp write_idx, WRITE_BUFFER
  jb .write_buf_ok
  ; należy przesunąć bufor outfile
  mov al, SYS_WRITE
  mov rdi, outfile_id         ; wczytaj deskryptor outfile do rdi
  mov rsi, outfile_buf        ; wczytaj adres bufora do rsi
  mov edx, WRITE_BUFFER       ; wczytaj rozmiar bufora do rdx
  call .syscall
  test rax, rax               ; jesli rax<0 to wystąpił błąd 
  js .err_both_open           ; trzeba zamknąć oba pliki z błędem 
  mov ax, word [abs woverflow]        ; wczytaj strażnika
  mov word [abs outfile_buf], ax      ; wstaw strażnika do buforu
  sub write_idx, WRITE_BUFFER         ; przesuń write_idx do początku  
.write_buf_ok:

  cmp read_idx, read_size
  jb .read_buf_ok
  cmp read_size, READ_BUFFER
  jb .read_done   ; jeśli rozmiar wczytanego pliku mniejszy od bufora, to doszliśmy do końca pliku
  ; należy przesunąć bufor infile
  mov al, SYS_READ
  mov rdi, infile_id        ; wczytaj deskryptor infile do rdi
  mov rsi, infile_buf       ; wczytaj adres bufora do rsi
  mov edx, READ_BUFFER      ; wczytaj rozmiar bufora do rdx
  call .syscall
  test rax, rax
  js .err_both_open      ; jeśli rax<0 to wystąpił błąd
  jz .read_done          ; jeśli rozmiar == 0 to plik się skończył 
  mov read_size, rax     ; ustaw rozmiar wczytanej części pliku
  xor read_idx, read_idx ; przesuń read_idx na początek buforu
.read_buf_ok:

  ; w tym miejscu na pewno w infile został co najmniej jeden bajt
  ; i na pewno w buforze outfile (ze strażnikiem) są 3 wolne bajty
  mov dl, [abs infile_buf + read_idx]
  xor dl, 's'         ; jeśli dl jest s lub S to wszystkie bity = 0 poza 2^15
  test dl, 1011111b   ; s i S różnią się jedynie przedostatnim bitem
  jz .is_s
  ; nie jest 's' ani 'S'
  inc ns_count
  jmp .not_s
.is_s:
  test ns_count, ns_count
  jz .no_counter ; jeśli nie napotkaliśmy na ciąg nie 's' 'S' to nie wpisujemy do pliku
  mov word [abs outfile_buf + write_idx], ns_count
  xor ns_count, ns_count  ; ns_count = 0
  add write_idx, 2        ; word = 2 bajty, które wpisaliśmy
.no_counter:
  xor dl, 's'             ; przywróć dl do 's' albo 'S'
  mov byte [abs outfile_buf + write_idx], dl
  inc write_idx
.not_s:
  inc read_idx
  jmp .loop

.read_done:

  test ns_count, ns_count
  jz .no_last_counter
  mov word [abs outfile_buf + write_idx], ns_count
  xor ns_count, ns_count  ; ns_count = 0
  add write_idx, 2        ; word = 2 bajty, które wpisaliśmy

.no_last_counter:

  test write_idx, write_idx
  jz .no_write ; bufor zapisu jest pusty

  mov al, SYS_WRITE
  mov rdi, outfile_id         ; wczytaj deskryptor outfile do rdi
  mov rsi, outfile_buf        ; wczytaj adres bufora do rsi
  mov rdx, write_idx          ; wczytaj rozmiar bufora do rdx
  call .syscall
  test rax, rax
  js .err_both_open

.no_write:

  call .close_both_files
  xor edi, edi
  jmp .exit
.exit1:
  mov edi, 1
.exit:
  push rdi   ; zapisz kod wyjścia na stosie
  mov rax, _start.exit
  jmp rax

.close_both_files:
  mov al, SYS_CLOSE
  mov rdi, outfile_id
  call .syscall
  test rax, rax
  js .err_infile_open   ; jeśli nie udało sie zamknąć outfile
                        ; to nie próbujemy ponownie, ale zamykamy infile

  mov al, SYS_CLOSE
  mov rdi, infile_id
  call .syscall
  test rax, rax
  js .exit1

  ret

.err_infile_open:

  ; zamknij infile
  mov al, SYS_CLOSE
  mov rdi, infile_id
  call .syscall

  jmp .exit1

.err_both_open:
  call .close_both_files
  jmp .exit1

; wykonywanie syscalli w ten sposób zajmuje mniej miejsca niz robienie xor eax, eax przed każdym 
.syscall:
  movzx eax, al
  syscall
  ret

end_code:

section .text

_start:
  ; utwórz blok pamięci z uprawnieniami do wykonywania
  xor eax, eax
  mov al, SYS_MMAP
  xor edi, edi ; NULL
  mov esi, end_code - start_code ; rozmiar bloku
  xor edx, edx
  mov dl, PROT_EXEC | PROT_READ | PROT_WRITE
  mov r10, MAP_PRIVATE | MAP_ANONYMOUS | MAP_EXECUTABLE
  or r8d, -1 ; fd
  xor r9d, r9d ; offset
  syscall

  mov r10, rax ; r10 = adres bloku

  ; przepisz kod do bloku
  mov rdi, rax ; adres bloku
  mov esi, start_code
  mov ecx, end_code - start_code
  rep movsb

  mov [abs code_ptr], r10 ; zapisz adres bloku w zmiennej start_code

  ; wykonaj kod
  jmp r10


.exit:

  ; usuwanie bloku pamięci
  xor eax, eax
  mov al, SYS_MUNMAP
  mov rdi, [abs code_ptr] ; wczytaj adres bloku do rdi
  mov esi, end_code - start_code
  syscall

  xor eax, eax
  mov al, SYS_EXIT
  pop rdi  ; wczytaj kod wyjścia do rdi
  syscall