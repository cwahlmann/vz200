.org 0x7200
.run 0x7200

.def screen: 0x7000

        JP start:
text:   defs "***** Hallo Welt!!! ------ "
        defb 0x00
start:  LD HL, screen:
        LD BC, 0x0200
loop1:  LD DE, text:
loop2:  LD A, (DE)
        OR A
        JR Z, loop1:
        LD (HL), A
        INC HL
        INC DE
        DEC BC
        LD A, B
        OR C
        JR NZ, loop2:
        LD HL, screen:
        LD BC, 0x0200
        JR loop2:
        