10 REM --- HALLO-WELT-ASSEMBLER ---
20 A = 28672 + 512: REM *** HIRES-SCREEN-MEM
30 READ B:REM *** BYTE-LESEN
40 IF B > 255 THEN 80:REM *** FERTIG?
50 POKE A, B:REM *** BYTE-SCHREIBEN
60 A=A+1
70 GOTO 30
80 REM *** USR-POINTER-SCHREIBEN
90 POKE 30862, 0
100 POKE 30863, 114: REM 72
120 X=USR(X):REM *** MASCHINEN-PROGRAMN-STARTEN
130 GOTO 130
1000 DATA 33, 0, 112, 1, 0, 2, 22, 0, 114, 35, 20, 11, 120, 177, 32, 248, 201
1010 DATA 999
