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
wait:
		JR wait:

x:      defb 64
y:      defb 61
dx:     defb 2
dy:     defb -10

ball0:  defb 0q1333, 0q1111
		defb 0q3330, 0q3111
		defb 0q1333, 0q1111
		defb 0,0

ball1:  defb 0q1133, 0q3111
		defb 0q1333, 0q0311
		defb 0q1133, 0q3111
		defb 0,0

ball2:  defb 0q1113, 0q3311
		defb 0q1133, 0q3031
		defb 0q1113, 0q3311
		defb 0,0

ball3:  defb 0q1111, 0q3331
		defb 0q1113, 0q3303
		defb 0q1111, 0q3331
		defb 0,0

// init
		// set gfx
init:	LD A, 0x08
		LD (latch:), A
		LD C, 0x01
		CALL clear_screen:
		RET

// main loop
main: 	DI

        CALL undraw:
        CALL move:
        CALL draw:

exit:   POP HL
        POP HL
        POP DE
        POP BC
        POP AF
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
		 CP 0x7c
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
		 CP 0x3d
		 JR C, move_n3:

		 LD (y:), A
		 LD A, (dy:)
         XOR 0xff
		 INC A
		 JR move_n4:

move_n3: LD A, (dy:)
		 INC A

move_n4: LD (dy:), A
		 RET

// undraw
undraw:  CALL pos:
		 LD A, 0x55
		 LD DE, 0x001f
		 LD (HL), A
		 INC HL
		 LD (HL), A
		 ADD HL, DE
		 LD (HL), A
		 INC HL
		 LD (HL), A
		 ADD HL, DE
		 LD (HL), A
		 INC HL
		 LD (HL), A
		 RET

// draw
draw:    CALL pos:
		 LD DE, 0x001f

		 LD A, (BC)
		 LD (HL), A
		 INC HL
		 INC BC

		 LD A, (BC)
		 LD (HL), A
		 ADD HL, DE
		 INC BC

		 LD A, (BC)
		 LD (HL), A
		 INC HL
		 INC BC

		 LD A, (BC)
		 LD (HL), A
		 ADD HL, DE
		 INC BC

		 LD A, (BC)
		 LD (HL), A
		 INC HL
		 INC BC

		 LD A, (BC)
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
		 SRA E
		 SRA E
		 ADD HL, DE
		 PUSH HL

		 LD A, (x:)
		 AND 0x03
		 LD L, A
		 LD H, 0x00
		 ADD HL, HL
		 ADD HL, HL
		 ADD HL, HL
		 LD DE, ball0:
		 ADD HL, DE
		 LD C, L
		 LD B, H
		 POP HL
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

