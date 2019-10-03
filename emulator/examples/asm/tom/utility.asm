.def interrupt_pointer: 0x787d

; install interrupt main loop
; input: DE pointer to main loop
 
install_interrupt_main_loop:
        DI
        
        LD HL, user_main_loop_call:
        INC HL
        LD (HL), E
        INC HL
        LD (HL), D
         
        LD HL, interrupt_pointer:
        LD (HL), 0xc3
        INC HL
        LD DE, interrupt_loop:
        LD (HL), E
        INC HL
        LD (HL), D

        EI
        RET

interrupt_loop:
		DI
user_main_loop_call:
        CALL user_dummy_main_loop:
						
		POP HL  // do not return do basic routines!!
		POP HL
        POP DE
        POP BC
        POP AF
        EI
        RETI

user_dummy_main_loop:
		RET


; random numnber generator
; input: -
; output: a random number in zahl_h / zahl_l 

randomnumber_h:   defb 0x00
randomnumber_l:   defb 0x00

random: PUSH HL
		PUSH DE
		
		LD HL, (randomnumber_h:)
        LD D, H
        LD E, L

        ADD HL, HL
        ADD HL, HL
		ADD HL, DE

        ADD HL, HL
        ADD HL, HL
		ADD HL, DE
        ADD HL, HL

        ADD HL, HL
		ADD HL, DE
        ADD HL, HL
		ADD HL, DE

        ADD HL, HL
        ADD HL, HL
        ADD HL, HL
		ADD HL, DE

		LD DE, 0x2517
		ADD HL, DE
		LD (randomnumber_h:), HL
		
		POP DE
		POP HL
		RET

; 16 by 8 division
; Inputs:
;     HL is the numerator
;     C is the denominator
; Outputs:
;     A is the remainder
;     B is 0
;     C is not changed
;     DE is not changed
;     HL is the quotient
;
div_by_ten: LD C, 0x0a
divide:
	  	LD B, 0x10
     	XOR A
  
divide_loop_1: 
		ADD HL, HL
		RLA
        CP C
        JR C, divide_next_1:
           
        INC HL
        SUB C
        
divide_next_1: 
		DJNZ divide_loop_1:
       	RET
