.org 0x8800
.run 0x8800

.def screen: 0x7000
.def buffer: 0x8000
.def latch:  0x6800

		JP start_program:
		
.include utility.asm
 
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

		LD A, (screen_needs_refresh:)
		OR A
		JR NZ, outer_loop_1:
		
		LD A, (interrupt_counter:)
		OR A
		JR NZ, outer_loop_1:
		
		LD A, 0x01
		LD (interrupt_counter:), A
		
 		CALL clear:

		LD HL, 0x0101
		CALL draw_level:
						 
		LD HL, buffer:
		LD A, (tom_dir:)
		LD B, A
		LD A, (tom_pos:)
		ADD A, B
		LD (tom_pos:), A
		
		CP 0x7b
		JR NZ, outer_next_1: 
		LD A, 0xff
		LD (tom_dir:), A
		JR outer_next_2:
outer_next_1:
		CP 0x00
		JR NZ, outer_next_2:
		LD A, 0x01 
		LD (tom_dir:), A
outer_next_2:
		LD B, 0x00
		LD A, (tom_dir:)
		CP 0x01
		JR NZ, outer_next_3: 
		LD B, 0x04
outer_next_3:
		LD A, (tom_pos:)		
		
		LD E, A
		SRL E
		SRL E
		LD D, 0x00
		ADD HL, DE
		
		AND 0x03
		ADD A, B
		CALL draw_sprite:

		LD A, 0x01
		LD (screen_needs_refresh:), A
				
		JR outer_loop_1: 

tom_pos:
		defb 0x00
tom_dir:
		defb 0x01 // left: 0xff

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

; draw leveldata
; input: L xoffset (1-84)
;        H yoffset (1-36)
draw_level:
		DEC L
		DEC H

		LD C, L
		LD B, 0x00
		
		LD E, H
		LD D, 0x00
		
		LD L, H
		LD H, 0x00
		
		ADD HL, HL ; x2
		ADD HL, HL ; x4
		ADD HL, DE ; x5
		ADD HL, HL ; x10
		ADD HL, HL ; x20
		ADD HL, DE ; x21
		ADD HL, HL ; x42
		ADD HL, DE ; x43
		ADD HL, HL ; x86
		
		ADD HL, BC ; +x
		
		LD DE, level:
		ADD HL, DE
		
		LD E, L
		LD D, H
		
		// loop rows
		LD B, 0x08
		LD H, 0x00
draw_level_loop_1:
		// loop cols
		LD C, 0x10
		LD L, 0x00
		
draw_level_loop_2:
		LD A, (DE)
		PUSH HL
		PUSH DE
		PUSH BC
		CALL draw_block:
		POP BC
		POP DE
		POP HL
		INC DE
		INC L
		DEC C
		JR NZ, draw_level_loop_2:
		
		PUSH HL
		LD HL, 0x0046 // 86-16 = 70
		ADD HL, DE
		LD E, L
		LD D, H
		POP HL
		INC H
		DEC B
		JR NZ, draw_level_loop_1:
		RET
  		
; draw a block on a fix pos
; input: L xpos (00-0f)
;		 H ypos (00-07)
;        A  sprite nr
		 
draw_block:
		SLA L 			; multiply xpos with 2
						; ypos in msb means y already multiplied by 256, that is 8 lines
		LD DE, buffer:
		ADD HL, DE		
		LD DE, sprites_bg:
		JR draw:
				 
; draw a sprite
; input: HL screenpos
;       A  sprite nr

draw_sprite:
		LD DE, sprites_char:
draw:
		PUSH HL

		LD L, A
		LD H, 0x00
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
		ADD HL, HL
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
				
.org 0x9000
.include sprites_bg.asm

.org 0xa000
.include sprites_char.asm

.org 0xb000
.include level.asm
