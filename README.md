# vz200
Nachbau eines VZ200-Color-Computers (1983)

## 3d-Modell
erstellt mit Autodesk Fusion (Lizenz nur für private Nutzung)
## Tastatur
eigenes Platinenlayout mit Dip-Schaltern, dir über eine Gummi-Tastaturmappe betätigt werden.
Tastaturcontroller: 
## Display
## Rechner

- Raspbian
- openjdk-8
- alsa-Treiber
- Umstellen audio auf headphone
```
sudo raspi-config
```

- jar kopieren nach `~/vz200`
- Start-Skript erstellen `~/vz200.sh`:

```bash
#!/bin/bash
cd vz200
java -jar vz200-all.jar
```

- .desktop erstellen für autostart `~/.config/autostart/vz200.desktop`:

```
[Desktop Entry]
Name=VZ200
Comment=VZ200 Emulator starten
Type=Application
Exec=./vz200.sh
Terminal=true
```


## Emulator
Der genutzte Emulator ist eine angepasste Version des Java-Emulators 'jemu': http://jemu.winape.net/

### Port Konfiguration

application.properties (Default 8080):

```
server.port = 10101
```

### Erweiterung laden / speichern von .vz:

```basic
OUT 252,1
READY
```

PORT | IN / OUT | Beschreibung
-----|----------|-------------
252  | OUT      | LOAD .vz program no. [n]
253  | OUT      | SAVE .vz program no. [n]

(werden in [home]/vz200/vz abgelegt)
  
### Erweiterung Tape-Controle

```basic
PRINT INP(254)
10
READY
```

PORT | IN / OUT | Beschreibung
-----|----------|-------------
254  | OUT      | 0: STOP, 1: PLAY, 16: RECORD
255  | OUT      | REWIND to position [n]
254, 255  | IN  | get LSB / MSB of tape position

(werden in [home]/vz200/tape abgelegt)

### REST-Interface

```bash
curl -X POST http://localhost:8080/vz200/vz 
     -H "Content-Type:application/octet-stream" 
     --data-binary @/D/Downloads/8bit/vz200/jvz_021/vz_files/games_autostart/CRASH.vz
```
Basis-Pfad: [HOST]:[PORT]/vz200

Endpunkt | Method | Request | Response | Beschreibung
---------|--------|---------|----------|-------------
/        | GET    |         | String   | Info
/reset   | POST   |         | String   | Reset Computer
/vz      | POST   | application/octet-stream | String | .vz-Programm einspielen
/vz      | GET    |         | application/octet-stream | .vz-Programm auslesen
/bas     | POST   | application/octet-stream | String | Basic-Programm-Source einspielen
/asm     | POST   | application/octet-stream | String | Assembler-Programm-Source einspielen und starten
/asm/{von[-bis]} | GET    | | String | Speicherbereich als Maschinenprogramm auslesen
/hex     | POST   | application/octet-stream | String | Hexadezimalen Source einspielen und starten
/hex/{von[-bis]} | GET    | | String | Speicherbereich in hexadezimalem Format auslesen
/printer/flush | GET | | String | zuletzt gedruckte Zeilen auslesen
/tape    | GET    |         | String | Namen des eingelegten Tapes lesen
/tape/{name} | POST    |        | String | Type mit angegebenem Namen einlegen
/tape/slot | GET    |         | Integer | aktuellen Slot des Tapes lesen
/tape/slot/{id} | GET    | Integer | | Tape zu angegebenem Slot spulen
/tape/play | POST | | Integer | Tape starten; gibt Slot zurück
/tape/record | POST | | Integer | Aufnahme starten; gibt Slot zurück
/tape/stop | POST | | Integer | Tape stoppen; gibt Slot zurück


