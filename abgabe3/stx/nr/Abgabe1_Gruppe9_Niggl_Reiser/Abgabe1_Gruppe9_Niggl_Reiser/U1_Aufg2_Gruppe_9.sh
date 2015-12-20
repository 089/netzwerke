#!/bin/bash

# U1 Aufgabe 2
# Niggl, Reiser

# outputfile
FILE=/home/network/Dokumente/Aufgabe_1.2/output.txt

# read http header
while read LINE; do
	# write line to outputfile
	echo "$LINE" >> $FILE

	# line with "newLine" break
	if [ "$LINE" == "" ]; then
		break
	fi
done

# feedback at the end
echo "Zugriff aufgezeichnet!"
