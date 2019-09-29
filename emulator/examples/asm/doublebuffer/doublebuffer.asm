.org 0x8800
.run 0x8800

.def screen: 0x7000
.def buffer: 0x8000
.def latch:  0x6800

		JP start_program:
		
.include utility.asm

sprites:  defw 0q22222000
		  defw 0q11110000
		  defw 0q03300000
		  defw 0q31130000
		  defw 0q31130000
		  defw 0q22220000
		  defw 0q02200000
		  defw 0q02220000

		  defw 0q02222200
		  defw 0q01111000
		  defw 0q00330000
		  defw 0q03113000
		  defw 0q03113000
		  defw 0q02222000
		  defw 0q00220000
		  defw 0q00222000

		  defw 0q00222220
		  defw 0q00111100
		  defw 0q00033000
		  defw 0q00311300
		  defw 0q00311300
		  defw 0q00222200
		  defw 0q02202202
		  defw 0q02220220

		  defw 0q00022222
		  defw 0q00011110
		  defw 0q00003300
		  defw 0q00031130
		  defw 0q00031130
		  defw 0q00022220
		  defw 0q00002200
		  defw 0q00002220

start_program:
		LD A, 0x02
		LD (interrupt_counter:), A

		LD A, 0x08
		LD (latch:), A

// install screen interrupt hook
		LD DE, main:
		CALL install_interrupt_main_loop:

// outer game loop

outer_loop_1:

		LD A, (interrupt_counter:)
		OR A
		JR NZ, outer_loop_1:
		LD A, 0x02
		LD (interrupt_counter:), A

 		CALL clear:
				 
		LD HL, buffer:
		XOR A
		CALL draw_sprite:

		INC HL
		INC HL
		LD A, 0x02
		CALL draw_sprite:
		
		LD A, 0x01
		LD (screen_needs_refresh:), A
		
		JR outer_loop_1: 

clear:
		LD HL, buffer:
		LD BC, 0x0800
		LD D, 0x00
clear_loop_1:
		LD (HL), D
		INC HL
		DEC BC
		LD A, B
		OR C
		JR NZ, clear_loop_1:
		RET

; draw a sprite
; input: HL screenpos
;        A  sprite nr

draw_sprite:
		PUSH HL

		LD L, A
		LD H, 0x00
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		LD DE, sprites:
		ADD HL, DE
		
		LD E, L
		LD D, H

		POP HL
		PUSH HL

		LD C, 0x08
		
draw_sprite_loop_1:
		LD A, (DE)
		INC HL
		LD (HL), A
		INC DE
		DEC HL

		LD A, (DE)
		LD (HL), A
		INC DE
		
		PUSH DE
		LD DE, 0x0020
		ADD HL, DE
		POP DE

		DEC C
		LD A, C
		OR A
		JR NZ, draw_sprite_loop_1:
		
		POP HL
		RET
				
screen_needs_refresh:
		defb 0x00

interrupt_counter:
		defb 0x00

main:
		LD A, (interrupt_counter:)
		OR A
		JR Z, main_next1:
		
		DEC A
		LD (interrupt_counter:), A
		
main_next1:
		LD A, (screen_needs_refresh:)
		OR A
		CALL NZ, refresh_screen:
		XOR A
		LD (screen_needs_refresh:), A
		RET

refresh_screen:
		LD HL, screen:
		LD DE, buffer:
		LD BC, 0x0800
refresh_screen_loop_1:
		LD A, (DE)
		LD (HL), A
		INC HL
		INC DE
		DEC BC
		LD A, B
		OR C
		JR NZ, refresh_screen_loop_1:
		RET
		