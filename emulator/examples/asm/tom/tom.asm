.org 0x8800
.run 0x8800

.def screen: 0x7000
.def buffer: 0x8000
.def latch:  0x6800

		JP start_program:

.include keyboard.asm		
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

		CALL player_move:
		CALL player_check_boundary:
		CALL player_check_obstacle:

		LD HL, (tom_pos:)
		LD A, (tom_dir_x:)
		ADD A, L
		LD L, A
		LD A, (tom_dir_y:)
		ADD A, H
		LD H, A
		LD (tom_pos:), HL
										 
		CALL draw_tom:

		LD A, 0x01
		LD (screen_needs_refresh:), A
				
		JR outer_loop_1: 
		
draw_tom:
		LD A, (tom_pos_x:)
		AND 0x03
		LD L, A
		
		LD A, (tom_dir:)
		CP 0xff // left?
		JR NZ, draw_tom_next1:		
		LD A, 0x00
		JR draw_tom_next3:

draw_tom_next1:
		CP 0x01 // right?
		JR NZ, draw_tom_next2:		
		LD A, 0x04
		JR draw_tom_next3:
				
draw_tom_next2: // stand still
		LD A, 0x08

draw_tom_next3:
		// ADD A, L		
		LD HL, (tom_pos:)		
		JP draw_sprite:

player_move:
		LD A, key_K:
		CALL check_key:
		JR NZ, player_move_next1:

		LD A, 0xff
		JR player_move_next3:		

player_move_next1:
		LD A, key_L:
		CALL check_key:
		JR NZ, player_move_next2:

		LD A, 0x01
		JR player_move_next3:

player_move_next2:
		XOR A
player_move_next3:
		LD (tom_dir:), A
		
		LD A, (tom_jump:)
		CP 0xff
		JR Z, player_move_fall:
		
		OR A
		JR NZ, player_move_jump:

		LD A, (tom_dir_y:)
		OR A
		JR NZ, player_move_fall:
		
		LD A, key_SHIFT:
		CALL check_key:
		JR NZ, player_move_fall:

		LD A, 0x09		
player_move_jump:
		DEC A
		LD (tom_jump:), A
		LD A, 0xff
		LD (tom_dir_y:), A
		RET
		
player_move_fall:
		LD A, 0x00
		LD (tom_jump:), A
		LD A, 0x01
		LD (tom_dir_y:), A
		RET
		
player_check_boundary:
		LD HL, (tom_pos:)
		LD A, (tom_dir_x:)
		CP 0xff
		JR NZ, player_check_boundary_next1:
		LD A, L
		CP 0x09
		JR NC, player_check_boundary_next2:
		XOR A
		LD (tom_dir_x:), A
		JR player_check_boundary_next2:
		
player_check_boundary_next1:
		CP 0x01
		JR NZ, player_check_boundary_next2:
		LD A, L
		CP 0x78
		JR C, player_check_boundary_next2:
		XOR A
		LD (tom_dir_x:), A

player_check_boundary_next2:
		LD A, (tom_dir_y:)
		CP 0xff
		JR NZ, player_check_boundary_next3:
		LD A, H
		CP 0x09
		RET NC
		XOR A
		LD (tom_dir_y:), A
		RET

player_check_boundary_next3:
		CP 0x01
		RET NZ
		LD A, H
		CP 0x38
		RET C
		LD A, 0x00
		LD (tom_dir_y:), A
		RET
	
player_check_obstacle:
		LD A, (tom_dir_x:)
		CP 0xff
		CALL Z, player_check_left:
		LD A, (tom_dir_x:)
		CP 0x01		
		CALL Z, player_check_right:

		LD A, (tom_dir_y:)
		CP 0xff
		CALL Z, player_check_top:
		LD A, (tom_dir_y:)
		CP 0x01		
		CALL Z, player_check_bottom:
		RET

player_check_left:
		LD HL, (tom_pos:)
		DEC L
		CALL calc_screen_pos:
		JR check_vertical:
		
player_check_right:
		LD HL, (tom_pos:)
		LD A, L
		ADD A, 0x08
		LD L, A
		CALL calc_screen_pos:

check_vertical:
		LD B, 0x08
		LD DE, 0x0020
check_vertical_loop1:			
		LD A, (HL)
		OR A
		JR NZ, stop_vertical:
		ADD HL, DE
		DEC B
		JR NZ, check_vertical_loop1:			
		RET
stop_vertical:
		XOR A
		LD (tom_dir_x:), A
		RET
		
player_check_top:
		LD HL, (tom_pos:)
		DEC H
		CALL calc_screen_pos:
		LD A, (HL)
		INC HL
		OR (HL)
		RET Z
		XOR A
		LD (tom_dir_y:), A
		LD A, 0xff
		LD (tom_jump:), A
		RET

player_check_bottom:
		LD HL, (tom_pos:)
		LD A, H
		ADD A, 0x08
		LD H, A
		CALL calc_screen_pos:
		LD A, (HL)
		INC HL
		OR (HL)
		RET Z
		XOR A
		LD (tom_dir_y:), A
		RET

tom_pos:
tom_pos_x:
		defb 0x00
tom_pos_y:
		defb 0x00
tom_dir:
tom_dir_x:
		defb 0x00 // left: 0xff; right: 0x01
tom_dir_y:
		defb 0x01 // up: 0xff; down: 0x01
tom_jump:
		defb 0x00 // ff: blocked
				  // 00: not jumping
		          // n: jump count							
		
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
