#!/bin/sh

FILE="$1"
FILE_NAME="${FILE%.*}"

if [ ! -z "$FILE" ] && [ -f "$FILE" ]
then
	neato -Tps -Goverlap=scale -Gsplines=true $FILE -o $FILE_NAME.ps
else
	echo "Please, provide file name"
fi