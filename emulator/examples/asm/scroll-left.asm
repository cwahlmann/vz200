.org 0x7200         // Programm in Grafikspeicher laden
.run 0x7200         // und dort auch starten

.def screen: 0x7000 // Bildschirmspeicheradresse

LD HL, screen:      // HL = Bildschirm Startadresse
LD DE, screen:      // DE = Bildschirm Startadresse + 1
INC DE      
LD BC, 0x01ff       // BC = Zähler

loop:               // Schleife 1

LD A, (DE)          // Zeichen aus DE laden
LD (HL), A          // und nach HL schreiben
INC HL              // Adresspointer erhöhen
INC DE
DEC BC              // Zähler - 1
LD A, B             // Zähler = 0 ?
OR C
JR NZ, loop:        // nein, weiter

LD HL, screen:      // HL = Bildschirm Startadresse
LD DE, 0x0020       // DE = Zeilenlänge
ADD HL, DE          // HL + Zeilenlänge - 1 = Ende der 1. Zeile 
DEC HL             
LD B, 0x10          // B = Zähler für 16 Zeilen
LD A, 0x20          // A = Leerzeichen

loop2:              // Schleife
   
LD (HL), A          // Leerzeichen schreiben
ADD HL, DE          // nächste Zeile
DEC B               // Zähler - 1
JR NZ, loop2:       // weiter, solange Zähler != 0
RET                 // Ende
