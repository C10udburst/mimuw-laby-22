global _start

; definicje syscall
SYS_EXIT  equ 60
SYS_OPEN  equ 2
SYS_READ equ 0
SYS_WRITE equ 1
SYS_CLOSE equ 3

; rozmiary buforów
READ_BUFFER equ 64
WRITE_BUFFER equ 64

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

  ; stan stosu:
  ; [rsp]:     nazwa infile
  ; [rsp + 8]: nazwa outfile

  ; otwieramy plik infile do czytania
  xor eax, eax
  mov al, SYS_OPEN
  mov rdi, [rsp]
  xor esi, esi         ; esi = RDONLY = 0, tryb czytania
  syscall
  test eax, eax
  js .exit1

  push rax

  ; stan stosu:
  ; [rsp]:      deskyptor infile
  ; [rsp + 8]:  nazwa infile
  ; [rsp + 16]: nazwa outfile

  ; otwieramy plik outfile do pisania
  xor eax, eax
  mov al, SYS_OPEN
  mov rdi, [rsp + 16]
  mov esi, O_WRONLY | O_CREAT | O_EXCL ; utwórz plik to zapisywania
  mov edx, FMOD ; ustaw uprawnienia pliku
  syscall
  test eax, eax
  js .error_on_open_outfile

  push rax
  add rsp, READ_BUFFER+WRITE_BUFFER ; zarezerwuj pamięć na read buffer i write buffer

  ; stan stosu:
  ; [rsp]:                                   bufor infile
  ; [rsp - READ_BUFFER]:                     bufor outfile
  ; [rsp + 8 - READ_BUFFER - WRITE_BUFFER]:  deskyptor infile
  ; [rsp - READ_BUFFER - WRITE_BUFFER]:      deskyptor outfile
  ; [rsp + 16 - READ_BUFFER - WRITE_BUFFER]: nazwa infile
  ; [rsp + 24 - READ_BUFFER - WRITE_BUFFER]: nazwa outfile

  mov r9, READ_BUFFER
  mov r10, WRITE_BUFFER
  xor r13, r13
.rw_loop:
  cmp r9, READ_BUFFER
  jae .infile_next
.infile_next_done:

  cmp r10, WRITE_BUFFER
  jae .outfile_next
.outfile_next_done:

.calc_loop: ; pętla która wypełnia bufor outfile
  mov dl, [rsp + r9]
  xor dl, 's'         ; jeśli dl jest s lub S to wszystkie bity = 0 poza 2^15
  test dl, 1011111b   ; s i S różnią się jedynie przedostatnim bitem
  jz .is_s
  inc r13
  jmp .not_s 
.is_s:
  test r13, r13
  jz .no_counter
  mov word [rsp - READ_BUFFER + r10], r13w
  xor r13, r13
  add r10, 2
.no_counter:
  xor dl, 's'
  mov byte [rsp - READ_BUFFER + r10], dl
  inc r10
.not_s:
  inc r9

  cmp r10, WRITE_BUFFER
  jae .rw_loop

  cmp r9, r12
  ja .rw_done

  cmp r9, READ_BUFFER
  jb .calc_loop
  ;jmp .rw_loop

.rw_done:


  ; wychodzenie z programu
  call .close_files
  xor edi, edi  ; $? := rdi
  xor eax, eax       ; eax = 0
  mov al, SYS_EXIT  ; eax = SYS_EXIT
  syscall

.error_on_open_outfile:
  mov rdi, [rsp]     ;  na górze stosu obecnie znajduje się deskryptor infile

  xor eax, eax
  mov al, SYS_CLOSE
  syscall
  
  js .exit1          ; zamknięcie programu z błędem

.error_both_files:
  call .close_files
  js .exit1

.exit1:
  xor edi, edi
  inc edi
  xor eax, eax       ; eax = 0
  mov al, SYS_EXIT  ; eax = SYS_EXIT
  syscall

.close_files:
  xor r12, r12

  mov rdi, [rsp - READ_BUFFER - WRITE_BUFFER]
  xor eax, eax
  mov al, SYS_CLOSE
  syscall

  test eax, eax
  jns .no_error
  inc r12
.no_error:

  mov rdi, [rsp + 8 - READ_BUFFER - WRITE_BUFFER]
  xor eax, eax
  mov al, SYS_CLOSE
  syscall

  test eax, eax
  js .exit1
  test r12, r12
  jnz .exit1

  ret

.infile_next:
  xor eax, eax                       ; eax = 0 = SYS_READ
  mov rdi, [rsp + 8 - READ_BUFFER - WRITE_BUFFER]   ; wczytaj deskryptor infile do rdi
  mov rsi, rsp                       ; wczytaj adres bufora do rsi
  mov rdx, READ_BUFFER               ; wczytaj rozmiar bufora do rdx
  syscall
  test rax, rax
  js .error_both_files
  jz .rw_done
  mov r12, rax
  xor r9, r9
  jmp .infile_next_done


.outfile_next:
  xor eax, eax
  inc eax                                     ; eax = 1 = SYS_WRITE
  mov rdi, [rsp - READ_BUFFER - WRITE_BUFFER] ; wczytaj deskryptor outfile do rdi
  lea rsi, [rsp - READ_BUFFER]                ; wczytaj adres bufora do rsi
  mov rdx, WRITE_BUFFER                       ; wczytaj rozmiar bufora do rdx
  syscall
  test rax, rax
  js .error_both_files
  xor r10, r10
  jmp .outfile_next_done