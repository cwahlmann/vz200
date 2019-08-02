#!/bin/bash
# ./setaudio.sh

xset s off 
xset -dpms
xset s noblank

cd vz200
java -jar vz200-all.jar
