#!/bin/bash

CRONSTRING=`crontab -l`

echo "Adding statuscake"
if [[ "$CRONSTRING" != *"statuscake"* ]];  then
        (crontab -l 2>/dev/null; echo "*/5 * * * * /home/pi/Entwined/chromatik/installations/$1/statuscake.sh") | crontab -
echo "Adding statuscake ping script"
fi

