global _start

; definicje syscall
SYS_EXIT  equ 60
SYS_OPEN  equ 2

; rozmiary buforów
READ_BUFFER equ 16
WRITE_BUFFER equ 16

; bits/fcntl-linux.h
O_WRONLY equ 1o  ; tylko do zapisu
O_CREAT equ 100o ; utwórz nowy
O_EXCL equ 200o  ; błąd jeśli istnieje

; uprawnienia nowo utworzonego pliku
FMOD equ 664o ; rw-r--r--


_start:
  ; domyślna instalacja linuxa ustala ARG_MAX=2097152
  ; więc teoretycznie qword jest tu niepotrzebny
  cmp qword [rsp], 3
  jnz .exit1
  add rsp, 16  ; usuwamy ze stosu ilość argumentów i arg[0] (nazwę programu)
  ; infile = [rsp], outfile = [rsp + 8]

  ; otwieramy plik infile do czytania
  xor eax, eax
  mov al, SYS_OPEN
  mov rdi, [rsp]
  xor esi, esi         ; esi = RDONLY = 0, tryb czytania
  syscall
  test eax, eax
  js .exit1

  ; otwieramy plik outfile do pisania
  xor eax, eax
  mov al, SYS_OPEN
  mov rdi, [rsp + 8]
  mov esi, O_WRONLY | O_CREAT | O_EXCL ; utwórz plik to zapisywania
  mov edx, FMOD ; ustaw uprawnienia pliku
  syscall
  test eax, eax
  js .exit1


  ; wychodzenie z programu
  xor edi, edi  ; rdi = 0, $? = 0
  jmp .exit0 ; nie było skoku do exit1 więc rdi = 0
.exit1:
  inc edi ; był skok, więc rdi = 1
.exit0:
  or eax, eax       ; eax = 0
  mov al, SYS_EXIT  ; eax = SYS_EXIT
  syscall