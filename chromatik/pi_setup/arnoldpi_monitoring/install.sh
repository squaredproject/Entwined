#!/bin/bash

echo "Adding statuscake for arnold pi"
cp ./statuscake.sh ~/
if [[ "$CRONSTRING" != *"statuscake"* ]];  then
        (crontab -l 2>/dev/null; echo "*/5 * * * * /home/pi/statuscake.sh") | crontab -
echo "Adding statuscake ping script for arnold pi"
fi


