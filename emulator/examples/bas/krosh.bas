0 REM KROSH ADVENTURE
1 REM --- PLAYER ---
2 REM LE=LEBENSKRAFT, LM=MAX.LE
3 REM G=GOLD, AT=ATTACKE, PA=PARADE
4 REM GE=GESCHICKLICHKEIT, KK=KOERPERKRAFT
5 REM R$=RUESTUNG, RN$/RA$/RD$=ARTIKEL, RS=RUESTSCHUTZ
6 REM W$=WAFFE, WN$/WA$/WD$=ARTIKEL, TP=TREFFERPUNKTE+W6
10 REM --- GEGNER --
11 REM E0=STATUS RABE 
12 REM (0=NEUGIERIG,1=ANGRIFF,2=FRIEDLICH,99=TOT)
13 E0=0
14 REM --- KAMPF ---
15 REM EL=LEBENSKRAFT
16 REM EA=ATTACKE, EP=PARADE
17 REM ER=RUESTUNG, ET=TREFFERPUNKTE
18 REM E$=NAME, EN$/EA$/ED$=ARTIKEL
30 REM ---START---
35 CLS:PRINT "WIE HEISST DU";
40 INPUT SP$
45 CLS
50 PRINT "WILLKOMMEN, ";SP$;" !"
55 PRINT
60 PRINT "DIES IST KROSH, DER ALPTRAUM"
70 PRINT "DEINER SCHLAFLOSEN NAECHTE."
80 PRINT
90 LM=30+RND(15):LE=LM
100 G=3+RND(3)
105 R$="LUMPEN":RS=0:RN$="DIE":RA$="DIE":RD$="DEN"
106 W$="STECKEN":TP=0:WN$="EIN":WA$="EINEN":WD$="EINEM"
107 AT=7+RND(6):PA=7+RND(6):GE=7+RND(6):KK=7+RND(6)
110 PRINT "DU STARTEST MIT"
120 GOSUB 10000:REM STATUS
130 GOSUB 10100:REM PAUSE
200 REM --- LICHTUNG ---
205 CLS
210 PRINT"DU STEHST AM RANDE EINER"
215 PRINT"LICHTUNG. EINE URALTE EICHE"
220 PRINT"SPANNT IHRE KNORRIGEN AESTE"
230 PRINT"UEBER DEN WEG.":PRINT
240 IF E0=0 THEN 250:REM NEUGIERIG
241 IF E0=1 THEN 270:REM ANGRIFF
242 IF E0=2 THEN 280:REM FRIEDLICH
243 GOTO 290:REM TOT / WEGE
250 REM - NEUGIERIG
251 PRINT "EIN SCHWARZER RABE FLATTERT AUS"
252 PRINT "DEN ZWEIGEN AUF DICH HERAB."
253 PRINT "ER SCHAUT DICH AUS KLUGEN AUGEN"
254 PRINT "FRAGEND AN."
255 GOSUB 10200:REM SAG WAS
256 IF S$<>"" THEN 270:REM ANGRIFF
257 PRINT "ER LEGT DEN KOPF SCHRAEG, ALS"
258 PRINT "WOLLTE ER ETWAS SAGEN..."
259 GOSUB 10200:REM SAG WAS
260 IF S$<>"" THEN 270:REM ANGRIFF
261 E0=2:GOTO 280:REM FRIEDLICH
270 REM - ANGRIFF
271 E0=1
272 PRINT "KRAH!! DER RABE HACKT MIT"
273 PRINT "SEINEM SCHNABEL AUF DICH EIN"
274 EL=10:EA=9:EP=7:ER=0:ET=0
275 E$="RABE":EN$="DER":EA$="DEN":ED$="DEM"
276 GOSUB 10500
277 IF EL<=0 THEN E0=99
278 GOTO 290:REM WEGE
280 REM - FRIEDLICH
281 PRINT "DER RABE ZIEHT SEINE KREISE"
282 PRINT "  KRAH KRAH ..."
290 REM - WEGE
291 PRINT:PRINT "NACH SUEDWESTEN FUEHRT DER WEG"
292 PRINT "AN EINEM KLEINEN BACH ENTLANG."
293 PRINT "NACH SUEDEN FUEHRT EIN HOHLWEG"
294 PRINT "TIEFER IN WALD."
295 PRINT "WOHIN MOECHTEST DU GEHEN,":PRINT "SW ODER S";
296 INPUT S$
297 IF S$="SW" THEN 400:REM BACH
298 IF S$="S" THEN 600:REM GRUBE
299 GOTO 296
400 REM --- BACH ---
401 CLS: PRINT "BACH"
580 REM - WEGE
581 PRINT:PRINT "DER WEG FUEHRT VON NORDOST"
582 PRINT "WEITER NACH SUEDEN."
583 PRINT "IM UNTERHOLZ IST EIN KLEINER"
584 PRINT "TRAMPELPFAD ZU SEHEN, DER SICH"
585 PRINT "OESTLICH IM DICKICHT VERLIERT."
585 PRINT "WOHIN MOECHTEST DU GEHEN,"
586 PRINT "S, NO ODER O";
590 INPUT S$
591 IF S$="S" THEN 1200:REM BRUECKE
592 IF S$="NO" THEN 200:REM LICHTUNG
593 IF S$="O" THEN 800:REM WILDPFAD
594 GOTO 590
600 REM --- GRUBE ---
601 CLS: PRINT "GRUBE"
602 PRINT "DER WALD LICHTET SICH UND DU"
603 PRINT "KOMMST AN DEN RAND EINER"
780 PRINT "SANDIGEN GRUBE."
780 REM - WEGE
781 PRINT "ABGESEHEN VON EINEM SCHMALEN"
782 PRINT "WILDPFAD, DER WESTLICH INS"
783 PRINT "UNTERHOLZ FUEHRT, BLEIBT NUR"
784 PRINT "DER WEG ZURUCK NACH NORDEN."
785 PRINT "WOHIN MOECHTEST DU"
786 PRINT "GEHEN, W ODER N";
790 INPUT S$
791 IF S$="W" THEN 800:REM WILDPFAD
792 IF S$="N" THEN 200:REM LICHTUNG
793 GOTO 790
800 REM --- WILDPFAD ---
801 CLS: PRINT "WILDPFAD"
980 REM - WEGE
981 PRINT "DER PFAD SCHLAENGELT SICH VON"
982 PRINT "OST NACH WEST DURCH DAS DICK-"
983 PRINT "ICHT DES WALDES. IM SUEDEN"
984 PRINT "HEBEN SICH EINIGE FELSEN"
985 PRINT "SCHWARZ GEGEN DIE LETZTEN"
986 PRINT "SONNENSTRAHLEN DES ABENDS AB."
988 PRINT "WOHIN MOECHTEST DU GEHEN,"
898 PRINT "O, W ODER S";
990 INPUT S$
991 IF S$="W" THEN 400:REM BACH
992 IF S$="O" THEN 600:REM GRUBE
993 IF S$="S" THEN 1000:REM HOEHLE
994 GOTO 990
1000 REM --- HOEHLE ---
1001 CLS: PRINT "HOEHLE"
1180 REM - WEGE
1181 PRINT "DER EINZIGE WEG ZURUECK FUEHRT"
1182 PRINT "UEBER DEN WILDPFAD IM NORDEN."
1183 GOSUB 10100:REM PAUSE
1194 GOTO 800

1200 REM --- BRUECKE ---
1201 CLS: PRINT "BRUECKE"
1380 REM - WEGE
1381 PRINT "DIE BRUECKE FUEHRT UEBER DEN"
1382 PRINT "BACH WEITER NACH SUEDEN."
1388 PRINT "WOHIN MOECHTEST DU GEHEN,"
1389 PRINT "N ODER S";
1390 INPUT S$
1391 IF S$="N" THEN 400:REM BACH
1392 IF S$="S" THEN 1400:REM SCHLOSS
1399 GOTO 1390

1400 REM --- SCHLOSS ---
1401 CLS: PRINT "SCHLOSS"
1580 REM - WEGE
1581 PRINT ""
1588 PRINT "WOHIN MOECHTEST DU GEHEN,"
1589 PRINT "N ODER S";
1590 INPUT S$
1591 IF S$="N" THEN 1200:REM BRUECKE
1592 IF S$="S" THEN 1600:REM SAAL
1599 GOTO 1590

1600 REM --- SAAL ---
1601 CLS: PRINT "SAAL"
1780 REM - WEGE
1781 PRINT ""
1788 PRINT "WOHIN MOECHTEST DU GEHEN,"
1789 PRINT "N, T)URM ODER G)EWOELBE";
1790 INPUT S$
1791 IF S$="N" THEN 1400:REM SCHLOSS
1792 IF S$="T" THEN 1800:REM TURM
1792 IF S$="G" THEN 2000:REM GEWOELBE
1799 GOTO 1790

1800 REM --- TURM ---
1801 CLS: PRINT "TURM"
1980 REM - WEGE
1981 PRINT ""
1990 GOSUB 10100:REM PAUSE
1999 GOTO 1600: REM SAAL

2000 REM --- GEWOELBE ---
2001 CLS: PRINT "GEWOELBE"
2180 REM - WEGE
2181 PRINT ""
2188 PRINT "WOHIN MOECHTEST DU GEHEN,"
2189 PRINT "S)AAL, O ODER W";
2190 INPUT S$
2191 IF S$="S" THEN 1600:REM SAAL
2192 IF S$="O" THEN 2200:REM KERKER
2192 IF S$="W" THEN 2400:REM LABOR
2199 GOTO 2190

2200 REM --- KERKER ---
2201 CLS: PRINT "KERKER"
2380 REM - WEGE
2381 PRINT ""
2390 GOSUB 10100:REM PAUSE
2399 GOTO 2000: REM GEWOELBE

2400 REM --- LABOR ---
2401 CLS: PRINT "LABOR"
2580 REM - WEGE
2581 PRINT ""
2590 GOSUB 10100:REM PAUSE
2599 GOTO 2000: REM GEWOELBE

10000 REM ---STATUS---
10010 PRINT G;"GOLD UND";LE;"LEBENSKRAFT"
10015 PRINT "DU HAST";GE;"GESCHICK UND";KK;"KRAFT"
10020 PRINT "DU TRAEGST ";R$:PRINT"MIT RUESTSCHUTZ";RS
10030 PRINT "UND ";WA$;" ";W$
10040 PRINT"MIT ATTACKE";AT;", PARADE";PA
10050 PRINT"UND SCHADEN W6";
10060 IF TP>0 THEN PRINT " +";TP ELSE PRINT
10099 RETURN
10100 REM ---PAUSE---
10110 PRINT:PRINT"WEITER MIT W..."
10120 IF INKEY$<>"W" THEN 10120
10130 RETURN
10200 REM ---SAG WAS---
10205 INPUT S$
10210 IF S$="" THEN PRINT "DU SCHWEIGST.":RETURN
10220 PRINT "DU SAGST '";S$;"'."
10230 RETURN
10500 REM ---KAMPF---
10510 REM GEGNER GREIFT AN
10520 A=RND(20)
10530 IF A>EA THEN PRINT EN$;" ";E$;" VERFEHLT DICH.":GOTO 10550
10531 PRINT EN$;" ";E$;" GREIFT DICH AN."
10540 V=RND(20)
10541 IF V>PA THEN PRINT"DU WEHRST MIT ";WD$;" ";W$;" AB.":GOTO 10550
10542 T=RND(6)+ET-RS:IF T<0 THEN T=0
10543 LE=LE-T
10544 PRINT EN$;" ";E$;" TRIFFT DICH MIT";T
10545 IF LE<=0 THEN 10640: REM TOT
10546 PRINT "DU HAST NOCH";LE;"LEBENSPUNKTE"
10550 REM HELD IST DRAN
10551 PRINT"WAS WILLST DU TUN:"
10552 PRINT"A - ANGREIFEN"
10553 PRINT"W - WEGLAUFEN"
10554 INPUT S$
10555 IF S$="W" THEN 10560: REM FLUCHT
10556 IF S$="A" THEN 10580: REM ANGRIFF
10557 GOTO 10554
10560 REM FLUCHT
10565 IF RND(20)<=GE THEN PRINT"DU FLIEHST!":RETURN
10570 PRINT "DU BIST ZU LANGSAM.":GOTO 10510
10580 REM ANGRIFF
10581 A=RND(20):IF A<=AT THEN 10590
10582 PRINT"DU VERFEHLST ";EA$;" ";E$;".":GOTO 10510
10590 PRINT "DU GREIFST ";EA$;" ";E$;" MIT ";WD$;" ";W$;" AN."
10591 P=RND(20):IF P>EP THEN 10600
10592 PRINT EN$;" ";E$;" PARIERT.":GOTO 10510
10600 T=RND(6)+TP-ER:IF T<0 THEN T=0
10605 EL=EL-T
10610 PRINT "DU TRIFFST ";EA$;" ";E$;" MIT";T
10620 IF EL>0 THEN 10510
10630 PRINT EN$;" ";E$;" IST BESIEGT.":RETURN
10640 REM HELD STIRBT
10650 PRINT "DAS WAR ZU VIEL FUER DICH!"
10660 PRINT "ROECHELND BRICHST DU ZUSAMMEN."
10665 GOSUB 10100 
10670 PRINT "DIES IST DAS UNRUEHMLICHE ENDE"
10680 PRINT "VON ";SP$;". +RIP+"
10690 GOSUB 10000
19999 END
