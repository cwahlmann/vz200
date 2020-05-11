#!/bin/bash
# ./setaudio.sh

xset s off 
xset -dpms
xset s noblank

cd vz200
JAR_FILE=`ls -1Nt vz200-*-all.jar`
java -jar $JAR_FILE
