This folder stores various TAPE-folder, that are stored and read 
via the CSAVE, CLOAD and CRUN commands.

TAPE controle is done by http-REST or by the INP() / OUT() functions:


Endpoint     | Method | Request | Response | Description
-------------|--------|---------|----------|-------------
/tape        | GET    |         | String   | Namen des eingelegten Tapes lesen
/tape/{name} | POST   |         | String   | Type mit angegebenem Namen einlegen
/tape/slot   | GET    |         | Integer  | aktuellen Slot des Tapes lesen
/tape/slot/{id} | GET | Integer |          | Tape zu angegebenem Slot spulen
/tape/play   | POST   |         | Integer  | Tape starten; gibt Slot zurück
/tape/record | POST   |         | Integer  | Aufnahme starten; gibt Slot zurück
/tape/stop   | POST   |         | Integer  | Tape stoppen; gibt Slot zurück


PORT | IN / OUT | Description
-----|----------|-------------
254  | OUT      | 0: STOP, 1: PLAY, 16: RECORD
255  | OUT      | REWIND to position [n]
254, 255  | IN  | get LSB / MSB of tape position
