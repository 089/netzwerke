#!/bin/bash

# U1_Aufg2
# "Schreiben Sie ein Programm, das die Standardeingabe 
# in eine Datei mit festgelegtem Namen kopiert und da-
# nach die Meldung „Zugriff aufgezeichnet!“ ausgibt."


# Dateiname
FILE=/tmp/output.txt

# HTTP-Header einlesen
while read LINE; do
	echo "$LINE" >> $FILE

	# Zeile mit Zeilenumbruch?
	if [ "$LINE" == "" ]; then
		break
	fi
done

# Ausgabe
echo "Zugriff aufgezeichnet!"
