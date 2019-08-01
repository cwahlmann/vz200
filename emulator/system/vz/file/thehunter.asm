.org 0x8000
.run 0x8000

.def screen: 0x7000
.def intadr: 0x787d
.def latch:  0x6800

// install screen interrupt hook
        DI
        CALL intro:
        CALL init0:
        
        LD HL, intadr:
        LD (HL), 0xc3
        INC HL
        LD DE, main:
        LD (HL), E
        INC HL
        LD (HL), D

        LD A, 0x01
        LD (0x78dd), A
        
        EI
    	// and then: do nothing ;-)
wait:   JR wait:

score:     defw 0x000f
lives:     defb 0x05

x:         defb 0x0e
r:         defb 0x01
arr_x:     defb 0x00
arr_y:     defb 0xff

// *** duck data
// 0: x, y
// 2: sprite (0-3; 0xff = dead)
// 3: type (0 = normal, 1 = evil)
// 4: delay
// 5: delay-count

max_ducks:  defb 0x0a

duck_data: 	defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
		   	defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
			defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
		   	defb 0x00, 0x00, 0xff, 0x00, 0x00, 0x00
		   		   
spr_hunter_left: defb 0xc0, 0xcf, 0xc0
                 defb 0xac, 0xaf, 0xa9
                 defb 0xb6, 0xb5, 0xb0
                 
spr_hunter_right: defb 0xc0, 0xcf, 0xc0
                 defb 0xa6, 0xaf, 0xac
                 defb 0xb0, 0xba, 0xb9

spr_hunter_stop: defb 0xc0, 0xcf, 0xc0
                 defb 0xa6, 0xaf, 0xa9
                 defb 0xb0, 0xbf, 0xb0

spr_arr:    defb 0x1e

spr_duck_0: defb 0xd6, 0xd9
spr_duck_1: defb 0xd3, 0xd3
spr_duck_2: defb 0xd9, 0xd6
spr_duck_3: defb 0xdc, 0xdc

spr_duck_4: defb 0xb6, 0xb9
spr_duck_5: defb 0xb3, 0xb3
spr_duck_6: defb 0xb9, 0xb6
spr_duck_7: defb 0xbc, 0xbc

init0:	LD A, 0x05
		LD (lives:), A
		LD HL, 0x0000
		LD (score:), HL
init:   LD IX, duck_data:
		LD A, (max_ducks:)
		LD B, A
init_lp1: LD (IX+2), 0xFF
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		DEC B
		JR NZ, init_lp1:
		
		LD A, 0x10
		LD (x:), A
		LD A, 0x00
		LD (r:), A
		LD A, 0xff
		LD (arr_y:), A
		RET

// main loop
main: 	DI
        PUSH HL
        PUSH DE
        PUSH BC
        PUSH AF

		CALL clear_screen:
        CALL draw:
        CALL draw_arr:
        CALL draw_ducks:
		CALL draw_score:
		CALL draw_lives:
		CALL control:
        CALL move:
        CALL move_arr:
        CALL move_ducks:

		LD A, (lives:)
		OR A
		JR NZ, exit:
		
		CALL game_over:
		CALL init0:
				
exit:   POP AF
        POP BC
        POP DE
        POP HL
        EI
        RETI

fire_pressed: defb 0x00

// A
control: LD A, (0x68fd)
		AND 0x10
		JR NZ, control_nx1:
		LD A, 0xff
		LD (r:), A
		JR control_nx3: 
		
// D
control_nx1: LD A, (0x68fd)
		AND 0x08
		JR NZ, control_nx2:
		LD A, 0x01
		LD (r:), A
		JR control_nx3: 
		
control_nx2: LD A, 0x00
        LD (r:), A
        
// SPACE
control_nx3: LD A, (0x68ef)
		AND 0x10
		JR NZ, control_nx4:
		
		LD A, (fire_pressed:)
		OR A
		RET NZ
		
		LD A, (arr_y:)
		CP 0xff
		RET NZ
		
		LD A, 0x01
		LD (fire_pressed:), A
		
		LD A, (x:)
		INC A
		LD (arr_x:), A
		LD A, 0x0c
		LD (arr_y:), A
		RET 

control_nx4: XOR A
		LD (fire_pressed:), A 
		RET 

// move
move_delay: defb 0x04

move:   LD A, (move_delay:)
		DEC A
		LD (move_delay:), A
		OR A
		RET NZ
		
		LD A, 0x04
		LD (move_delay:), A

		LD A, (r:)
		LD B, A
		LD A, (x:)
		ADD A, B
		LD (x:), A
		
		CP 0xff
		JR NZ, move_nx1:
		
		LD A, 0x00
		LD (r:), A
		LD A, 0x00
		LD (x:), A
		RET
		
move_nx1: CP 0x1e
		RET C
		LD A, 0x00
		LD (r:), A
		LD A, 0x1d
		LD (x:), A
		RET
		
// draw
draw:    LD A, (x:)
		 LD L, A
		 LD H, 0x00
		 
		 LD DE, 0x0180
		 ADD HL, DE
		 		 
		 LD DE, screen:
		 ADD HL, DE
		 
		 LD C, 0x03
		 LD A, (r:)
		 CP 0x01
		 JR NZ, draw_nx1:
		 LD DE, spr_hunter_right:
		 JR draw_lo1:

draw_nx1: CP 0xff
		 JR NZ, draw_nx2:
		 LD DE, spr_hunter_left:
		 JR draw_lo1:
		 
draw_nx2: LD DE, spr_hunter_stop:

draw_lo1: LD B, 0x03 
draw_lo2: LD A, (DE)
		 LD (HL), A
		 INC HL
		 INC DE
		 DEC B
		 JR NZ, draw_lo2:
		 PUSH DE
		 LD DE, 0x001d
		 ADD HL, DE
		 POP DE
		 DEC C
		 JR NZ, draw_lo1:
		 RET

duck_delay: defb 0x04

move_ducks: LD IX, duck_data:
		LD A, (max_ducks:)
		LD B, A
move_ducks_lp1: PUSH BC
		LD A, (IX+2)
		CP 0xff
		JR NZ, move_ducks_nx1:

		CALL zufall:
		LD A, (zahl_l:)
		OR A
		CALL Z, new_duck:
		JR move_ducks_nx2:  
		
move_ducks_nx1: CALL move_duck:

move_ducks_nx2: POP BC
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		DEC B		
		JR NZ, move_ducks_lp1:
		RET 

// *** new duck
new_duck: CALL zufall:
		LD A, (zahl_l:)
		AND 0x1f
		LD (IX+0), A
		
		CALL zufall:
		LD A, (zahl_l:)
		AND 0x07
		LD (IX+1), A
		XOR A
		LD (IX+2), A
		LD (IX+3), A
		CALL zufall:
		LD A, (zahl_l:)
		AND 0x07
		ADD A, 0x02
		LD (IX+4), A
		LD (IX+5), A
		RET		

// *** move duck
duck_y_moves: 		defb 0xff, 0xff, 0x00, 0x01
duck_evil_y_moves:	defb 0xff, 0xff, 0x01, 0x02

// duck delay 
move_duck: LD A, (IX+5)
		DEC A
		LD (IX+5), A
		OR A
		RET NZ
		LD A, (IX+4)
		LD (IX+5), A

// select sprite
		CALL zufall:
		LD A, (zahl_l:)
		AND 0x03
		LD (IX+2), A
		
		CALL zufall:
		LD A, (zahl_l:)
		CP 0xfd
		JR C, move_duck_n0:

		LD A, (IX+3)		
		XOR 0x01
		LD (IX+3), A
		
// move x
move_duck_n0: CALL zufall:
		LD D, (IX+0)
		LD A, (zahl_l:)
		AND 0x01
		ADD A, A
		SUB 0x01
		ADD A, D
		CP 0xff
		JR NZ, move_duck_n1:
		LD A, 0x00
move_duck_n1: CP 0x1f
		JR NZ, move_duck_n2:
		LD A, 0x1e
move_duck_n2: LD (IX+0), A
		
// move y
        LD HL, duck_y_moves:
        LD A, (IX+3)
        OR A
        JR Z, move_duck_n2a:
        INC HL
        INC HL
        INC HL
        INC HL
move_duck_n2a: CALL zufall:
		LD A, (zahl_l:)
		AND 0x03
		LD E, A
		LD D, 0x00
		ADD HL, DE
		LD A, (HL)
		LD D, (IX+1)
		ADD A, D
		CP 0xfe
		JR C, move_duck_n3:
		LD A, 0x00
move_duck_n3: CP 0x0d
		JR C, move_duck_n4:
		LD A, 0x0c
move_duck_n4: LD (IX+1), A
		RET

draw_ducks: LD IX, duck_data:
		LD A, (max_ducks:)
		LD B, A
draw_ducks_lp1: PUSH BC
		LD A, (IX+2)
		CP 0xff
		JR Z, draw_ducks_nx1:
					
		CALL draw_duck:

draw_ducks_nx1:	POP BC
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		INC IX
		DEC B		
		JR NZ, draw_ducks_lp1:
		RET 
				
get_ready_text: defs "OUCH! SHE BIT YOU!"
				defb 0x00
				defs "PRESS <S> WHEN READY"				
				defb 0x00
				
draw_duck: LD L, (IX+1)
		LD H, 0x00
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		LD E, (IX+0)
		LD D, 0x00
		ADD HL, DE
		LD DE, screen:
		ADD HL, DE
		
		PUSH HL
		LD A, (IX+3)
		ADD A, A
		ADD A, A
		ADD A, (IX+2)
		ADD A, A
		LD E, A
		LD D, 0x00
		LD HL, spr_duck_0:
		ADD HL, DE
		LD E, L
		LD D, H
		POP HL
		
// check collision

// Arr
		LD A, (spr_arr:)
		LD B, (HL)
		CP B
		JR Z, draw_duck_nx1:
		INC HL
		LD B, (HL)
		DEC HL
		CP B
		JR Z, draw_duck_nx1:

// hunter
		LD A, (IX+3)
		OR A
		JR Z, draw_duck_nx2:
		LD A, 0xcf
		LD B, (HL)
		CP B
		JR Z, draw_duck_nxb1:
		INC HL
		LD B, (HL)
		DEC HL
		CP B
		JR NZ, draw_duck_nx2:
		
// hit hunter
draw_duck_nxb1: LD (IX+2), 0xff
		LD A, (lives:)
		OR A
		JR Z, draw_duck_nx2:
		DEC A
		LD (lives:), A
		OR A
		RET Z
		
		LD BC, 0x0107
		LD DE, get_ready_text:
		CALL print_at: 
		LD BC, 0x0126
		CALL print_at:
		
// wait for "s"-Key
draw_duck_lp1b: LD A, (0x68fd)
		AND 0x02
		JR NZ, draw_duck_lp1b:
		
		CALL init:
		RET
		        
// hit arr
draw_duck_nx1: LD (IX+2), 0xff
		LD A, 0xff
		LD (arr_y:), A
		
		LD HL, (score:)
		
		LD A, (IX+3)
		OR A
		JR NZ, got_evil_duck:

		INC HL
		LD (score:), HL
		RET
		
got_evil_duck: LD DE, 0x0005
		ADD HL, DE
		LD (score:), HL
		RET
		
draw_duck_nx2: LD A, (DE)
		LD (HL), A
		INC HL
		INC DE
		LD A, (DE)
		LD (HL), A
		RET		

move_arr: LD A, (arr_y:)
		CP 0xff
		RET Z
		DEC A
		LD (arr_y:), A
		RET

draw_arr: LD A, (arr_y:)
		CP 0xff
		RET Z
		LD L, A
		LD H, 0x00
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		LD A, (arr_x:)
		LD E, A
		LD D, 0x00
		ADD HL, DE
		LD DE, screen:
		ADD HL, DE
		LD A, (spr_arr:)
		LD (HL), A
		RET
		
// fill screen
clear_screen: LD HL, screen:
  			  LD BC, 0x01e0
clr_loop1:	  LD (HL), 0x80
			  INC HL
			  DEC BC
			  LD A, B
			  OR C
			  JR NZ, clr_loop1:
  			  LD B, 0x20
clr_loop2:	  LD (HL), 0x20
			  INC HL
			  DEC B
			  JR NZ, clr_loop2:
			  RET

// Zufallsgenerator

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
		
// inputs: DE - screen pos
draw_score: LD DE, 0x01e8
draw_score_at: LD HL, screen:
		ADD HL, DE
		LD D, H
		LD E, L 
		LD HL, (score:)
		LD B, 0x05
		INC DE
		INC DE
		INC DE
		INC DE
		INC DE
		LD A, 0x30
		LD (DE), A
		DEC DE
draw_score_lp1: PUSH DE
		PUSH BC
		CALL div_10:
		POP BC
		POP DE
 	    ADD A, 0x30
        LD (DE), A
        DEC DE
        DJNZ draw_score_lp1:
        RET
        
// lives
draw_lives: LD HL, screen:
        LD DE, 0x01f6
        ADD HL, DE
        LD A, (lives:)
draw_lives_lp2: LD (HL), 0x2a
		INC HL
		DEC A
		JR NZ, draw_lives_lp2: 
        RET

;Inputs:
;     HL is the numerator
;     C is the denominator
;Outputs:
;     A is the remainder
;     B is 0
;     C is not changed
;     DE is not changed
;     HL is the quotient
;
div_10: LD C, 0x0a
div:  	LD B, 0x10
     	XOR A
  
div_lp1: ADD HL, HL
		RLA
        CP C
        JR C, div_nx1:
           
        INC HL
        SUB C
        
div_nx1: DJNZ div_lp1:
       	RET

intro_logo:	defb 0x3c, 0x3e, 0x38, 0x3a, 0x30, 0x3a, 0x3e, 0x3c, 0x38, 0x00, 0x2a, 0x20, 0x2a, 0x2a, 0x20, 0x2a, 0x2b, 0x20, 0x2a, 0x2c, 0x2e, 0x28, 0x2e, 0x2c, 0x28, 0x2e, 0x2d, 0x20
			defb 0x30, 0x3a, 0x30, 0x3e, 0x3c, 0x3a, 0x3e, 0x38, 0x30, 0x00, 0x2e, 0x2c, 0x2a, 0x2a, 0x20, 0x2a, 0x2a, 0x29, 0x2a, 0x20, 0x2a, 0x20, 0x2e, 0x28, 0x20, 0x2e, 0x2c, 0x2a
			defb 0x30, 0x38, 0x30, 0x38, 0x30, 0x38, 0x3c, 0x3c, 0x38, 0x00, 0x28, 0x20, 0x28, 0x2c, 0x2c, 0x28, 0x28, 0x20, 0x28, 0x20, 0x28, 0x20, 0x2c, 0x2c, 0x28, 0x28, 0x20, 0x28

intro_text: defs "A BAT HUNTING GAME BY ** FR3D **"
			defb 0x00
			defs "PRESS <S> TO START"
			defb 0x00

intro: 	LD HL, screen:
		LD BC, 0x0200
intro_lp1: LD (HL), 0x80
		INC HL
		DEC BC
		LD A, B
		OR C
		JR NZ, intro_lp1:
		
		LD HL, screen:
		LD DE, 0x0082
		ADD HL, DE
		LD DE, intro_logo:
		LD B, 0x03
intro_lp2: LD C, 0x1c
intro_lp3: LD A, (DE)
		ADD A, 0x80
		LD (HL), A
		INC HL
		INC DE
		DEC C
		JR NZ, intro_lp3:
		PUSH DE
		LD DE, 0x0004
		ADD HL, DE
		POP DE
		DEC B
		JR NZ, intro_lp2:
		
		LD BC, 0x00e0
		LD DE, intro_text:
		CALL print_at:

		LD BC, 0x01c7
		CALL print_at:
		 
// wait for "s"-Key
intro_nx1: LD A, (0x68fd)
		AND 0x02
		JR NZ, intro_nx1:
		RET
		
game_over_logo:	defb 0x4e, 0x4c, 0x48, 0x4e, 0x4c, 0x4a, 0x4e, 0x4e, 0x4a, 0x4e, 0x4c, 0x48, 0x40, 0x4e, 0x4c, 0x4a, 0x4a, 0x40, 0x4a, 0x4e, 0x4c, 0x48, 0x4e, 0x4d, 0x40
				defb 0x4a, 0x44, 0x4a, 0x4b, 0x43, 0x4a, 0x4a, 0x48, 0x4a, 0x4e, 0x48, 0x40, 0x40, 0x4a, 0x40, 0x4a, 0x4a, 0x45, 0x48, 0x4e, 0x48, 0x40, 0x4e, 0x4c, 0x4a
				defb 0x4c, 0x4c, 0x48, 0x48, 0x40, 0x48, 0x48, 0x40, 0x48, 0x4c, 0x4c, 0x48, 0x40, 0x4c, 0x4c, 0x48, 0x4c, 0x4c, 0x40, 0x4c, 0x4c, 0x48, 0x48, 0x40, 0x48
				
game_over_text: defs "PRESS <S> FOR START"
				defb 0x00
				defs "OR <I> FOR INTRO"
				defb 0x00

game_over: LD HL, screen:
		LD BC, 0x0200
game_over_lp1: LD (HL), 0x80
		INC HL
		DEC BC
		LD A, B
		OR C
		JR NZ, game_over_lp1:
		
		LD HL, screen:
		LD DE, 0x0084
		ADD HL, DE
		LD DE, game_over_logo:
		LD B, 0x03
game_over_lp2: LD C, 0x19
game_over_lp3: LD A, (DE)
		ADD A, 0x80
		LD (HL), A
		INC HL
		INC DE
		DEC C
		JR NZ, game_over_lp3:
		PUSH DE
		LD DE, 0x0007
		ADD HL, DE
		POP DE
		DEC B
		JR NZ, game_over_lp2:

		LD DE, 0x012d
		CALL draw_score_at:
				
		LD BC, 0x01a6
		LD DE, game_over_text:
		CALL print_at: 
		LD BC, 0x01c8
		CALL print_at:
		 
		// check "s"-Key
game_over_lp4: LD A, (0x68fd)
		AND 0x02
		RET Z
		// check "i"-Key
		LD A, (0x68bf)
		AND 0x08
		JR NZ, game_over_lp4:
		JP intro:

// print text at DE on screen at BC
print_at: LD HL, screen:
		ADD HL, BC
print_at_lp4: LD A, (DE)
		INC DE
		OR A
		RET Z
		LD (HL), A
		INC HL
		JR print_at_lp4:
		