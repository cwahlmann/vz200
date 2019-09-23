.org 0x8000
.run 0x8000

.def screen: 0x7000
.def intadr: 0x787d

        DI
        LD HL, intadr:
        LD (HL), 0xc3
        INC HL
        LD DE, intloop:
        LD (HL), E
        INC HL
        LD (HL), D
        OR A
        LD (0x78de), A
        EI
wait:   JR wait:

.include lib/messages.asm

intloop: DI
        PUSH HL
        PUSH DE
        PUSH BC
        PUSH AF

        LD A, (count1:)
        DEC A
        LD (count1:), A
        OR A
        JR NZ, next0:
        LD A, 0x04
        LD (count1:), A

        CALL textscroll:
        CALL colorlines:

next0:  LD A, (count2:)
        DEC A
        LD (count2:), A
        OR A
        JR NZ, next01:
        LD A, 0x02
        LD (count2:), A

        CALL sinus:

next01: LD A, (count3:)
        DEC A
        LD (count3:), A
        OR A
        JR NZ, exit:
        LD A, 0x04
        LD (count3:), A

        CALL sound:

exit:   POP AF
        POP BC
        POP DE
        POP HL
        EI
        RETI

txtptr: defw txt:
count1:  defb 0x01
count2:  defb 0x02
count3:  defb 0x03

textscroll: LD HL, (txtptr:)
        LD DE, screen:
        LD B, 0x40

        INC HL
        LD A, (HL)
        OR A
        JR NZ, next1:

        LD HL, txt:

next1:  LD (txtptr:), HL

loop1:  LD A, (HL)
        LD (DE), A
        INC DE
        INC HL

        LD A, (HL)
        OR A
        JR NZ, next2:

        LD HL, txt:

next2:  DEC B
        JR NZ, loop1:

        RET

color:  defb 0x8f
colorlines: LD HL, screen:
            LD DE, 0x0040
            ADD HL, DE
            LD B, 0x07
loop_col1:  LD C, 0x20
            LD A, (color:)
loop_col2:  LD (HL), A
            INC HL
            DEC C
            JR NZ, loop_col2:

            ADD A, 0x10
            CP 0x0F
            JR NZ, next_col1:

            LD A, 0x8f

next_col1:    LD (color:), A
            DEC B
            JR NZ, loop_col1:
            RET

sin_start0: defb 0x0c
            defb 0x0b, 0x0b
            defb 0x0a, 0x0a, 0x0a
sin_start1: defb 0x09, 0x09, 0x09, 0x09, 0x09
            defb 0x0a, 0x0a, 0x0a
            defb 0x0b, 0x0b
            defb 0x0c
            defb 0x0d, 0x0d
            defb 0x0e, 0x0e, 0x0e
            defb 0x0f, 0x0f, 0x0f, 0x0f, 0x0f
            defb 0x0e, 0x0e, 0x0e
            defb 0x0d, 0x0d
            defb 0xff

sina:       defw sin_start0:
sinb:       defw sin_start1:

sinus:      LD HL, 0x7120
            LD DE, 0x7121
            LD C, 0xe0
sinus_l1:   LD A, (DE)
            LD (HL), A
            INC HL
            INC DE
            DEC C
            JR NZ, sinus_l1:

            LD HL, 0x713f
            LD DE, 0x0020
            LD C, 0x07
sinus_l2:   LD (HL), 0x20
            ADD HL, DE
            DEC C
            JR NZ, sinus_l2:

            LD HL, (sina:)
            INC HL
            LD A, (HL)
            CP 0xff
            JR NZ, sinus_n1:
            LD HL, sin_start0:
            LD A, (HL)
sinus_n1:   LD (sina:), HL
            CALL sin_pos:
            LD (HL), 0x2a

            LD HL, (sinb:)
            INC HL
            LD A, (HL)
            CP 0xff
            JR NZ, sinus_n2:
            LD HL, sin_start0:
            LD A, (HL)
sinus_n2:   LD (sinb:), HL
            CALL sin_pos:
            LD (HL), 0x2e
            RET

sin_pos:    LD L, A
            LD H, 0x00
            ADD HL, HL
            ADD HL, HL
            ADD HL, HL
            ADD HL, HL
            ADD HL, HL
            LD DE, 0x701f
            ADD HL, DE
            RET

snd_ptr:    defw 0x0000
sound:      LD HL, (snd_ptr:)
            LD A, (HL)
            INC HL
            LD (snd_ptr:), HL
            LD L, A
            LD H, 0x00
            INC HL
            LD BC, 0x0008
            CALL 0x345c
            RET
