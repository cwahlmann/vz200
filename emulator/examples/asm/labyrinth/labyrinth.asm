//   ***** LABYRINTH *****
//
// A MAZE DEMO BY FR3D
//
//          alias
//    Christian Wahlmann

.org 0x8000
.run 0x8000

.def screen: 0x7000
.def latch:  0x6800
.def data: 0x7200
.def map_width: 0x10
.def map_height: 0x60

        JP start_program:

.include utility.asm

start_program:
// install screen interrupt hook
//		LD DE, main:
//		CALL install_interrupt_main_loop:

        CALL show_screen:
loop:
        JP loop:

win_y:  defb 0x04
win_x:  defb 0x02
win_w:  defb 0x16
win_h:  defb 0x0a

px: defb 0x00
py: defb 0x00

show_screen:
// calculate pos on screen
    LD A, (win_y:)
    LD L, A
    LD H, 0x00
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL
    ADD HL, HL
    LD A, (win_x:)
    LD E, A
    LD D, 0x70
    ADD HL, DE
    LD DE, 0x0000
// loop
    LD A, (win_h:)
    LD B, A
show_screen_ly:
    LD A, (win_w:)
    LD C, A
    PUSH HL
show_screen_lx:
    LD A, (DE)
    LD (HL), A
    INC HL
    INC DE
    DEC C
    LD A, C
    OR A
    JR NZ, show_screen_lx:
    POP HL
    PUSH DE
    LD DE, 0x0020
    ADD HL, DE
    POP DE
    DEC B
    LD A, B
    OR A
    JR NZ, show_screen_ly:
    RET
