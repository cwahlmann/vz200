# vz200
Nachbau eines VZ200-Color-Computers (1983)

## 3d-Modell
erstellt mit Autodesk Fusion (Lizenz nur für private Nutzung)
## Tastatur
eigenes Platinenlayout mit Dip-Schaltern, dir über eine Gummi-Tastaturmappe betätigt werden.
Tastaturcontroller: 
## Display
## Rechner
## Emulator
Der genutzte Emulator ist eine angepasste Version des Java-Emulators 'jemu': http://jemu.winape.net/

### Erweiterung laden / speichern von .vz:

PORT | IN / OUT | Beschreibung
-----|----------|-------------
252  | OUT      | LOAD .vz program no. [n]
253  | OUT      | SAVE .vz program no. [n]

(werden in [home]/vz200/vz abgelegt)
  
### Erweiterung Tape-Controle

PORT | IN / OUT | Beschreibung
-----|----------|-------------
254  | OUT      | 0: STOP, 1: PLAY, 16: RECORD
255  | OUT      | REWIND to position [n]
254, 255  | IN  | get LSB / MSB of tape position

(werden in [home]/vz200/tape abgelegt)
