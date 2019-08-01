.org 0x8000
.run 0x8000

.def screen: 0x7000
.def intadr: 0x787d

		DI
start:  LD HL, screen:
        LD BC, 0x0200
loop:   CALL zufall:
     	LD A, (zahl:)
        LD (HL), A
        INC HL
        DEC BC
        LD A, B
        OR C
        JR NZ, loop:
        
        LD A, (zahl_l:)        
        LD B, A
        
loop2:  CALL zufall:
        DEC B
        LD A, B
        OR A
        JR NZ, loop2:
        
		JR start:

zahl_h:   defb 0x00
zahl_l:   defb 0x00

zufall: PUSH HL
		PUSH DE
		
		LD HL, (zahl_h:)
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
		LD (zahl_h:), HL
		
		POP DE
		POP HL
		RET        