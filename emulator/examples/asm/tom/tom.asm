.org 0x8800
.run 0x8800

.def screen: 0x7000
.def buffer: 0x8000
.def latch:  0x6800

		JP start_program:
		
.include utility.asm
.include sprite.asm
 
start_program:
		LD A, 0x02
		LD (interrupt_counter:), A

		LD A, 0x08
		LD (latch:), A

		LD HL, buffer:
		LD (screen_data:), HL
		
		LD HL, sprite_data_bg:
		LD (block_data:), HL
		
		LD HL, sprite_data_char:
		LD (sprite_data:), HL

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
						 
		LD A, (tom_dir:)
		OR A
		JR NZ, go_left:
		
; go right
		LD A, (tom_pos:)
		CP 0x7b
		JR NC, turn_left:
		
		INC A
		LD (tom_pos:), A
		JR draw_tom:
		
turn_left:
		LD A, 0x01
		LD (tom_dir:), A
		LD A, 0x7a
		LD (tom_pos:), A
		JR draw_tom:		
		
go_left:
		LD A, (tom_pos:)
		OR A
		JR Z, turn_right:
		
		DEC A
		LD (tom_pos:), A
		JR draw_tom:
		
turn_right:
		LD A, 0x00
		LD (tom_dir:), A
		LD (tom_pos:), A

draw_tom:
		LD A, (tom_dir:)
		LD E, A
		LD D, 0x00
		LD HL, tom_sprite_offset:
		ADD HL, DE
		LD A, (HL)
		LD HL, (tom_pos:)		
		CALL draw_sprite:

		LD A, 0x01
		LD (screen_needs_refresh:), A
				
		JR outer_loop_1: 

tom_pos:
		defw 0x0000
tom_dir:
		defb 0x01 // left: 0x00
tom_sprite_offset:
		defb 0x04, 0x00
		
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

animation:
		defb    0x00

; draw level
; input: L xoffset (1-84)
;        H yoffset (1-36)
draw_level:
		LD A, (animation:)
		INC A
		AND 0x03
		LD (animation:), A

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
		
		LD DE, level_data:
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
		PUSH HL
		PUSH DE
		PUSH BC

		LD A, (DE)
		CP 0x80 		; is it to be animated?
		JR C, draw_level_next_1:
						
		AND 0xfc
		LD C, A
		LD A, (animation:)
		ADD A, C
		
draw_level_next_1:
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
				
.org 0x9000
.include sprite_data_bg.asm

.org 0x9800
.include sprite_data_bg_anim.asm

.org 0xa000
.include sprite_data_char.asm

.org 0xb000
.include level_data.asm
