// address to store program
.org 0x8000

// address to start
.run 0x8000

// text-screen address
.def screen: 0x7000

// jump to routine
			JP start:

// include text to display
.include message.asm
	
// routine to display a text
start:		LD HL, message:
			LD BC, screen:
loop:		LD A, (HL)
			; check if A is 0x00, if true, exit
			OR A
			RET Z
			LD (BC), A
			INC HL
			INC BC
			JR loop:
