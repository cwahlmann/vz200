.org 0x8000
.run 0x8000

.def screen: 0x7000
.def intadr: 0x787d
.def latch:  0x6800

// install screen interrupt hook
        DI
        CALL init:
        LD HL, intadr:
        LD (HL), 0xc3
        INC HL
        LD DE, main:
        LD (HL), E
        INC HL
        LD (HL), D
        OR A
        LD (0x78de), A
        EI
    	// and then: do nothing ;-)
wait:   JR wait:

x:      defb 0x10
y:      defb 0x20
dx:     defb 0x01
dy:     defb 0x03

// init
		// set gfx
init:	LD A, 0x08
		LD (latch:), A
		LD C, 0x01
		CALL clear_screen:
		RET

// main loop
main: 	DI
        PUSH HL
        PUSH DE
        PUSH BC
        PUSH AF

        CALL undraw:
        CALL move:
        CALL draw:

exit:   POP AF
        POP BC
        POP DE
        POP HL
        EI
        RETI

// move
move:    LD A, (x:)
         LD B, A
		 LD A, (dx:)
		 ADD A, B
		 LD (x:), A
		 OR A
		 JR Z, move_n1:
		 CP 0x1f
		 JR C, move_n2:
move_n1: LD A, (dx:)
		 XOR 0xff
		 INC A
		 LD (dx:), A

move_n2: LD A, (y:)
         LD B, A
		 LD A, (dy:)
		 ADD A, B
		 LD (y:), A
		 OR A
		 JR Z, move_n3:
		 CP 0x3f
		 JR C, move_n4:
move_n3: LD A, (dy:)
		 XOR 0xff
		 INC A
		 LD (dy:), A

move_n4: RET

// undraw
undraw:  CALL pos:
		 LD A, 0x55
		 LD (HL), A
		 RET

// draw
draw:    CALL pos:
		 LD A, 0xaa
		 LD (HL), A
		 RET

// pos
pos:     LD A, (y:)
		 LD L, A
		 LD H, 0x00
		 ADD HL, HL
		 ADD HL, HL
		 ADD HL, HL
		 ADD HL, HL
		 ADD HL, HL
		 LD DE, screen:
		 LD A, (x:)
		 LD E, A
		 ADD HL, DE
		 RET

// fill screen with color C
clear_screen: LD A, C
			  SLA C
			  SLA C
			  OR C
			  SLA C
			  SLA C
			  OR C
			  SLA C
  			  SLA C
  			  OR C
  			  LD HL, screen:
  			  LD BC, 0x0800
  			  LD D, A
clr_loop1:	  LD (HL), D
			  INC HL
			  DEC BC
			  LD A, B
			  OR C
			  JR NZ, clr_loop1:
			  RET

