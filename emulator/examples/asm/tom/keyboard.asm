// check if key is pressed
// input:  	A: keycode
// output: 	Z: if key pressed

check_key:
	PUSH HL
	PUSH DE
	// address entry in lookup table
	LD L, A 
	LD H, 0x00
	ADD HL, HL
	LD DE, key_lookup_table:
	ADD HL, DE
	INC HL
	// load bitmask to test key
	LD A, (HL)
	DEC HL
	// load lsb of latch address
	LD L, (HL)
	// msb of latch address
	LD H, 0x68
	// check against bitmask (Z = key is pressed)
	LD D, (HL)
	AND D
	POP DE
	POP HL
	RET	
	
// key codes
.def key_A: 0x00
.def key_B: 0x01
.def key_C: 0x02
.def key_D: 0x03
.def key_E: 0x04
.def key_F: 0x05
.def key_G: 0x06
.def key_H: 0x07
.def key_I: 0x08
.def key_J: 0x09
.def key_K: 0x0a
.def key_L: 0x0b
.def key_M: 0x0c
.def key_N: 0x0d
.def key_O: 0x0e
.def key_P: 0x0f
.def key_Q: 0x10
.def key_R: 0x11
.def key_S: 0x12
.def key_T: 0x13
.def key_U: 0x14
.def key_V: 0x15
.def key_W: 0x16
.def key_X: 0x17
.def key_Y: 0x18
.def key_Z: 0x19

.def key_0: 0x1a
.def key_1: 0x1b
.def key_2: 0x1c
.def key_3: 0x1d
.def key_4: 0x1e
.def key_5: 0x1f
.def key_6: 0x20
.def key_7: 0x21
.def key_8: 0x22
.def key_9: 0x23

.def key_SPACE:		0x24
.def key_CTRL:		0x25
.def key_SHIFT:		0x26
.def key_ENTER: 	0x27
.def key_DOT:   	0x28
.def key_COMMA: 	0x29
.def key_COLON: 	0x2a
.def key_SEMICOLON: 0x2b
.def key_MINUS:		0x2c

// keyboard lookup table
key_lookup_table:
key_lookup_A:		defw 0x10fd
key_lookup_B:		defw 0x01fb
key_lookup_C:		defw 0x08fb
key_lookup_D:		defw 0x08fd
key_lookup_E:	    defw 0x08fe
key_lookup_F:		defw 0x20fd
key_lookup_G: 		defw 0x01fd
key_lookup_H:   	defw 0x017f
key_lookup_I:   	defw 0x08bf
key_lookup_J:   	defw 0x207f
key_lookup_K:   	defw 0x087f
key_lookup_L:   	defw 0x027f
key_lookup_M:   	defw 0x20ef
key_lookup_N:   	defw 0x01ef
key_lookup_O:   	defw 0x02bf
key_lookup_P:   	defw 0x10bf
key_lookup_Q:		defw 0x10fe
key_lookup_R:  		defw 0x20fe
key_lookup_S:	  	defw 0x02fd
key_lookup_T:		defw 0x01fe
key_lookup_U:   	defw 0x20bf
key_lookup_V:		defw 0x20fb
key_lookup_W:		defw 0x02fe
key_lookup_X:		defw 0x02fb
key_lookup_Y:   	defw 0x01bf
key_lookup_Z:		defw 0x10fb

key_lookup_0:   	defw 0x10df
key_lookup_1:   	defw 0x10f7
key_lookup_2:   	defw 0x02f7
key_lookup_3:   	defw 0x08f7
key_lookup_4:   	defw 0x20f7
key_lookup_5:  		defw 0x01f7
key_lookup_6:   	defw 0x01df
key_lookup_7:   	defw 0x20df
key_lookup_8:   	defw 0x08df
key_lookup_9:   	defw 0x02df

key_lookup_SPACE:  	defw 0x10ef
key_lookup_CTRL:	defw 0x04fd
key_lookup_SHIFT:	defw 0x04fb
key_lookup_ENTER:  	defw 0x04bf
key_lookup_DOT:   	defw 0x02ef
key_lookup_COMMA:  	defw 0x08ef
key_lookup_COLON:  	defw 0x047f
key_lookup_SEMICOLON: defw 0x107f
key_lookup_MINUS:	defw 0x04df
