.org 0x8000
.run 0x8000
    
    JP start:
        
.include utility

start:
    // initialize
    LD HL, ypos:
    LD C, 0x20
init_loop:
    CALL random:
    LD A, (randomnumber_l:)
    AND 0x1f
    OR 0xe0
    LD (HL), A
    INC HL
    DEC C
    LD A, C
    OR A
    JR NZ, init_loop:
    
    LD DE, main_loop:
    CALL install_interrupt_main_loop:
do_nothing_loop:
    JP do_nothing_loop:
    
ypos:
    defs "01234567890123456789012345678901"

main_loop:
    LD HL, ypos:
    LD C, 0x20
    
loop1:
    LD A, (HL)
    INC A
    LD (HL), A
    CP 0x18
    JR NZ, next3:

    CALL random:
    LD A, (randomnumber_l:)
    AND 0x0f
    OR 0xf0
    LD (HL), A

next3:
    CP 0x18
    JR NC, next1:
    
    PUSH HL
    PUSH BC
    
    LD L, A
    LD H, 0x00
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL

    LD B, 0x00
    ADD HL, BC
    DEC HL
    LD DE, 0x6f00
    ADD HL, DE

    CP 0x08
    JR C, next2:
    LD (HL), 0x80

next2:
    CP 0x10
    JR NC, next4:
    
    LD DE, 0x0100
    ADD HL, DE
    CALL random:
    LD A, (randomnumber_l:)
    AND 0x1f
    OR 0x20
    LD (HL), A    
    
next4:
    POP BC
    POP HL
    
next1:
    INC HL
    DEC C
    LD A, C
    OR A
    JR NZ, loop1:
    
    RET