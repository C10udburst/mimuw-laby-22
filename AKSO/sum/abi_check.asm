global abi_check
extern sum

RBX_VAL equ 0xcafebabedeadbeef
R12_VAL equ 0xaa57018b2e209b9c
R13_VAL equ 0xef0f26d2c89e4980
R14_VAL equ 0xb25d51f219407076
R15_VAL equ 0x650d0a1b172f5003

abi_check:
    push rbx
    push r12
    push r13
    push r14
    push r15
    
    mov rbx, RBX_VAL
    mov r12, R12_VAL
    mov r13, R13_VAL
    mov r14, R14_VAL
    mov r15, R15_VAL
    
    call sum

    mov rax, RBX_VAL
    cmp rbx, rax
    jne .fail

    mov rax, R12_VAL
    cmp r12, rax
    jne .fail

    mov rax, R13_VAL
    cmp r13, rax
    jne .fail

    mov rax, R14_VAL
    cmp r14, rax
    jne .fail

    mov rax, R15_VAL
    cmp r15, rax
    jne .fail

    mov rax, 1

    jmp .end

.fail:

    mov rax, 0

.end:

    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx

    ret

