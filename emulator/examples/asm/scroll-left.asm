.org 0x7200
.run 0x7200

.def screen: 0x7000

LD HL, screen:
LD DE, screen:
INC DE
LD BC, 0x01ff

loop:

LD A, (DE)
LD (HL), A
INC HL
INC DE
DEC BC
LD A, B
OR C
JR NZ, loop:

LD HL, screen:
LD DE, 0x0020
ADD HL, DE
DEC HL
LD B, 0x10
LD A, 0x20

loop2:

LD (HL), A
ADD HL, DE
DEC B
JR NZ, loop2:
RET
