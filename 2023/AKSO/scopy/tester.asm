%ifndef TESTER_ASM
%define TESTER_ASM

%define syscall fakecall

%macro fakecall 0
%%fakecall:
    cmp rax, 60 ; sys_exit
    je %%syscall
    rdrand r11
    test r11, r11
    jns %%syscall
    mov rax, r11
    jmp %%done
%%syscall:
    db 0x0f, 0x05 ; syscall
%%done:
%endmacro
%endif