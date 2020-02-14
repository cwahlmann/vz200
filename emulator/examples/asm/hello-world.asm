.org 0x7200
.run 0x7200

.def screen: 0x7000

LD HL, screen:
LD BC, 0x0200
LD D, 0x00

loop:

LD (HL), D
INC HL
INC D
DEC BC
LD A, B
OR C

JR NZ, loop:

RET
