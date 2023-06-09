%define call _fakecall

%define _start start_scopy
%define .text .code exec

%include "./scopy.asm"

%undef _start
%undef .text
%undef syscall

global _start

section .testdata write
    counter: dq 0
    had_error: dq 0

section .text

_start:
    jmp start_scopy

_fakecall:
    cmp rax, 60 ; sys_exit
    je .exit
    cmp rax, 3  ; sys_close
    je .fclose
.back:
    rdrand r11
    test r11, r11
    jns .syscall
    mov qword [had_error], 1
    mov rax, -1
    jmp .done
.syscall:
    cmp rax, 2  ; sys_open
    je .fopen
.syscall_back:
    syscall
    jmp .done
.exit:
    sub qword [had_error], rdi
    jmp _done
.fopen:
    inc qword [counter]
    jmp .syscall_back
.fclose:
    dec qword [counter]
    jmp .back
.done:
    ret

_done:
    mov rax, 60
    mov rdi, [counter]
    or rdi, [had_error]
    syscall
