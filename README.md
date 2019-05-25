# vz200
Nachbau eines VZ200-Color-Computers (1983)

## 3d-Modell
erstellt mit Autodesk Fusion (Startup-Lizenz nur für private Nutzung)

### Drucke
STL-Dateien aus Fusion werden mit Makerbot Cura 4.0 druckfertig gemacht -> gcode für Creality Ender 3.
Typische Auflösung: x/y 0.4mm (abhängig von der Düse), z 0.05-3mm (Schrittmotor, Extrusion)
Düsen von 0.2, 0.3, 0.5 und 0.6 stehen demnächst auch zur Verfügung, wobei die Gefahr der Verstopfung mit kleineren Durchmessern steigt. Holz besser mit 0.5 oder 0.6 drucken.

Wichtige Druckparameter: Wandstärke, Füllung (Stärke, Muster), Geschwindigkeit, Schichtendicke und abhängig vom Filament Düsen- und Betttemperatur sowie ggf. Anpassungen an der Fließgeschwindigkeit.

Gedruckt wird mit PLA (Gehäuse) und TPA-Filament (Tastatur). 

PLA entspricht hartem Plastik, bis ca. 60 Grad Celsius verformungssteif, danach wird es weich. Große Auswahl an Farben. 

TPU ist ein flexibles Filament, das sich gut für Handyhüllen oder Gummitastaturen eignet. Die Farbauswahl ist jedoch sehr beschränkt, so dass wir das karamellbraun der Originaltasten nicht direkt drucken können. Aktuell verfügbar ist ein mitteldunkles Blau. 

Für den VZ 200 nehmen wir Weiß (Gehäuse), schokoladenbraun (Tastaturabdeckung) [Weiß, da die Tastaturabdeckung noch mit einer transparenten bedruckten Folie beklebt wird, und weiß beim Druck transparent gedruckt wird] und holzbraun für die Tastatur (holzartig) oder eben blau (gummiartig).

Auch möglich ist PET-G, eine Abwandlung des in der Industrie üblichen PET mit denselben Eigenschaften.

Sonder-Filamente sind möglich (schwach leitend, fluoreszierend, transparent).

Hinweis: Löten auf den schwach leitenden Filamenten ist nicht möglich.

## Tastatur

eigenes Platinenlayout mit Dip-Schaltern, dir über eine Gummi-Tastaturmappe betätigt werden.
Tastaturcontroller: KEYWARRIOR24_8_MODUL_KW24_8_MOD 

## Display

JOY-iT 5“ HDMI Touchscreen Display

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

## Nützliche Links

Raspi-Emulator für Windows (leider schon 7 Jahre alt)
https://sourceforge.net/projects/rpiqemuwindows/

