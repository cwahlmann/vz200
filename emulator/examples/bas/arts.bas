0 GOTO1000
4 COLOR,0:MODE(1)
5 MC=4
6 S0=RND(0)*4+3:S1=RND(4)+2:C0=RND(0)*3+2:C1=RND(4)+2
7 S2=RND(0)*3+1:S3=RND(0):S4=RND(0)
8 S5=RND(0)*6:C2=RND(0)*6
20 FOR J=0 TO 63:X=J
30 FOR I=0 TO 127:Y=I
40 GOSUB 100
50 COLOR C+1:SET(I,J)
60 NEXTI:NEXTJ
70 SOUND 0,9:GOTO 6
100 A=(X*0.6-GW/2)/(GW/2)
110 B=(Y*0.6-GH/2)/(GH/2)
120 GOSUB 210
130 IF DI=0 THEN C=INT(E) :ELSE C=INT(E+RND(0))
140 IF C<0 THEN C=C+INT(ABS(C+1)/MC+1)*MC :ELSE C=C-INT(C/MC)*MC
160 RETURN
200 S0=6:S1=3:C0=4:C1=3:S2=2:S3=0.4:S4=0.5:S5=0:C2=0
210 E=SIN((A-SIN(B*S2+S3)*S4+S5)*S0)*S1 + COS(B*C0+C2)*C1
220 RETURN
300 COLOR,1:CLS:PRINT"PLEASE WAIT..."
301 DIM CC(10)
302 FOR I=1 TO 8:CC(I+1)=127+16*I:NEXTI
303 CC(0)=128:CC(1)=96
305 MC=10
310 S0=RND(0)*4+3:S1=RND(4)+2:C0=RND(0)*3+2:C1=RND(4)+2
315 S2=RND(0)*3+1:S3=RND(0):S4=RND(0)
317 S5=RND(0)*6:C2=RND(0)*6
320 FOR J%=0 TO 15:Y=J%
330 FOR I%=0 TO 31:X=I%
335 AD%=28672+J%*32+I%
336 W%=PEEK(AD%):POKEAD%,(W%-(W%AND1)*2+1)OR128
340 GOSUB 100
350 POKEAD%+512,CC(C)
355 POKEAD%,W%
360 NEXTI%:NEXTJ%
370 FORI=0TO511:POKE28672+I,PEEK(28672+I+512):NEXTI
380 GOTO310
400 COLOR,0
401 FORP=0TO3:OUT32,GM*4+P:MODE(1):NEXTP
405 MC=4
406 S0=RND(0)*4+3:S1=RND(4)+2:C0=RND(0)*3+2:C1=RND(4)+2
407 S2=RND(0)*3+1:S3=RND(0):S4=RND(0)
408 S5=RND(0)*6:C2=RND(0)*6
410 J=0:P=0:Z=0
415 OUT32,GM*4+P
420 Y=J
430 FOR I=0 TO GW-1:X=I
440 GOSUB 100
450 COLOR C+1:SET(I,Z)
460 NEXTI:J=J+1:IF J>=GH THEN 490
470 Z=Z+1:IF Z<PH THEN420
480 Z=0:P=P+1:GOTO415
490 SOUND 0,9:GOTO406
500 COLOR,0
501 FORP=0TO3:OUT32,GM*4+P:MODE(1):NEXTP
505 MC=4
506 S0=RND(0)*4+3:S1=RND(4)+2:C0=RND(0)*3+2:C1=RND(4)+2
507 S2=RND(0)*3+1:S3=RND(0):S4=RND(0)
508 S5=RND(0)*6:C2=RND(0)*6
510 J=0:P=0:Z=0
515 OUT32,GM*4+P
520 Y=J
530 FOR I=0 TO GW-1:X=I
540 GOSUB 100
550 COLOR C+1:SET((I AND 63)+(Z AND 1)*64,Z/2)
560 NEXTI:J=J+1:IF J>=GH THEN 590
570 Z=Z+1:IF Z<PH THEN520
580 Z=0:P=P+1:GOTO515
590 SOUND 0,9:GOTO506
600 COLOR,0
601 FORP=0TO3:OUT32,GM*4+P:MODE(1):NEXTP
605 MC=2
606 S0=RND(0)*4+3:S1=RND(4)+2:C0=RND(0)*3+2:C1=RND(4)+2
607 S2=RND(0)*3+1:S3=RND(0):S4=RND(0)
608 S5=RND(0)*6:C2=RND(0)*6
610 J=0:P=0:Z=0
615 OUT32,GM*4+P:A%=28672
620 Y=J
625 X=0
630 FOR IC%=0 TO GW/8-1
635 M%=128
637 FOR IB%=0 TO 7
640 GOSUB 100
650 C%=C:GOSUB700
655 X=X+1:M%=M%/2
657 NEXT IB%
658 A%=A%+1
660 NEXT IC%
665 J=J+1:IF J>=GH THEN 690
670 Z=Z+1:IF Z<PH THEN620
680 Z=0:P=P+1:GOTO615
690 SOUND 0,9:GOTO606
700 REM DRAW MONO POINT
760 IF C%>0 THEN 790
770 POKEA%,PEEK(A%)AND(255-M%)
780 RETURN
790 POKEA%,PEEK(A%)OR M%
799 RETURN
1000 REM MENU
1010 COLOR,1:CLS
1011 PRINT "DITHERING (J/N)";
1012 INPUT S$
1013 IF S$="J" THEN DI=1:GOTO1020
1014 IF S$="N" THEN DI=0:GOTO1020
1015 GOTO1012
1020 PRINT"WAEHLE DIE GRAFIKAUSLOESUNG:"
1030 PRINT
1040 PRINT "0 - 32 X 16"
1050 PRINT "1 - 64 X 64"
1060 PRINT "2 - 128 X 64"
1070 PRINT "3 - 128 X 96"
1080 PRINT "4 - 128 X 192"
1085 PRINT "5 - 256 X 192 S/W"
1090 PRINT
1100 INPUT A$
1110 IF A$="0" THEN GW=64:GH=64:GOTO300
1120 IF A$="1" THEN GM=0:GW=64:GH=64:PH=64:GOTO500
1130 IF A$="2" THEN GM=2:GW=128:GH=64:PH=64:GOTO400
1140 IF A$="3" THEN GM=4:GW=128:GH=96:PH=64:GOTO400
1150 IF A$="4" THEN GM=6:GW=128:GH=192:PH=64:GOTO400
1160 IF A$="5" THEN GM=7:GW=256:GH=192:PH=64:GOTO600
1170 GOTO1100