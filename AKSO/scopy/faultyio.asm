%define syscall call _fakecall

%define _start start_scopy
%define .text .code exec

%include "./scopy.asm"

%undef _start
%undef .text
%undef syscall

global _start

section .text

_start:
    jmp start_scopy

_fakecall:
    cmp rax, 1 ; sys_write
    je .write
    cmp rax, 0 ; sys_read
    je .read
.done:
    syscall
    ret
.write:
    ; simulate faulty io, write 1 or 0 bytes
    rdrand r11
    and r11, 1
    mov rdx, r11 ; rdx = r11
    jmp .done
.read:
    ; simulate faulty io, read 1 byte
    mov rdx, 1
    jmp .done


