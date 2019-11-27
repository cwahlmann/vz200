screen_data:
		defw 0x0000 

; draw a block on a fix pos
; input: L xpos (00-0f)
;		 H ypos (00-07)
;        A tile nr

block_data: 
		defw 0x0000
		 
draw_block:
		SLA L 			; multiply xpos with 2
						; ypos in msb means y already multiplied by 256, that is 8 lines
		LD DE, (screen_data:)
		ADD HL, DE		
		LD DE, (block_data:)

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
		
draw_block_loop_1:
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
		JR NZ, draw_block_loop_1:
		
		POP HL
		RET

sprite_data: 
		defw 0x0000

; draw a sprite
; input: L: xpos
;        H: ypos
;        A: offset for sprite no (xpos and 0x03 will be added)
draw_sprite:
		LD B, A 	; save A
		LD A, L		; take last two bits of xpos
		AND 0x03 	 
		ADD A, B	; and add to offset of sprite no
					
					; the screen column is xpos / 4
		SRL L		; / 2
		SRL L		; / 4
		LD E, L		; 
		LD D, 0x00  
		
		LD L, H		; the screen position is row * 32
		LD H, 0x00
		ADD HL, HL 	; x2
		ADD HL, HL 	; x4
		ADD HL, HL 	; x8
		ADD HL, HL 	; x16
		ADD HL, HL 	; x32
		
		ADD HL, DE 	; add column
		
		LD DE, (screen_data:) 			
		ADD HL, DE	; add screen offset			
 		PUSH HL		; and save screen position

; calculate sprite data address: no * 16 + sprite_data:

		LD DE, (sprite_data:)
		LD L, A
		LD H, 0x00
		ADD HL, HL	; *2
		ADD HL, HL	; *4
		ADD HL, HL	; *8
		ADD HL, HL	; *16
		ADD HL, DE
		
		LD E, L
		LD D, H		; save to DE

		POP HL		; restore screen position 

		LD C, 0x08
		
draw_sprite_loop_1:
		INC HL

		CALL draw_sprite_byte:

		DEC HL
		INC DE

		CALL draw_sprite_byte:

		INC DE
		
		PUSH DE		; save current sprite data address			
		LD DE, 0x0020
		ADD HL, DE
		POP DE		; restore current sprite data address

		DEC C
		LD A, C
		OR A
		JR NZ, draw_sprite_loop_1:
		
		RET

; draws one byte from sprite data address to screen
; input: DE - sprite data address
; 		 HL - screen position
; used registers:  B, A
 
draw_sprite_byte:
		LD A, (DE)	// get byte from sprite_data
		LD B, A		// and create a mask
		AND 0x55
		SLA A
		OR B
		LD B, A
		AND 0xaa
		SRL A
		OR B
		XOR 0xff

		AND (HL)	// clear points
		LD B, A

		LD A, (DE)	// and set points from sprite
		OR B
		LD (HL), A	// draw result
		RET		
		