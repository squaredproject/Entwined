#!/bin/bash

echo "Backing up code to home directory: ~/Entwined.bak"
cp -rf ../../../Entwined ~/Entwined.bak

CRONSTRING=`crontab -l`
LIGHTS=$"lights_on"

if [[ "$CRONSTRING" != *"$LIGHTS"* ]];  then
    echo "NO CRON $CRONSTRING"
        (crontab -l 2>/dev/null; echo "0 5 * * * /home/entwined/Entwined/ddptest/lights_on.sh") | crontab -
        (crontab -l 2>/dev/null; echo "0 22 * * * cd /home/entwined/Entwined/ddptest/; ./lights_out.sh") | crontab -
        echo "Creating cron jobs to turn lights on and off"
fi


echo "Adding statuscake"
if [[ "$CRONSTRING" != *"statuscake"* ]];  then
        (crontab -l 2>/dev/null; echo "*/5 * * * * /home/entwined/Entwined/oldlx/installations/$1/statuscake.sh") | crontab -
echo "Adding statuscake ping script"
fi

echo "Adding ddptest script for footlight color"
if [[ "$CRONSTRING" != *"ddptest.py"* ]];  then
        (crontab -l 2>/dev/null; echo "*/10 * * * * /home/entwined/Entwined/ddptest/ddptest.py  --cubes 24 --lpc 1 --pattern cube_color --color orange --host 10.0.0.123") | crontab -
        (crontab -l 2>/dev/null; echo "*/10 * * * * /home/entwined/Entwined/ddptest/ddptest.py  --cubes 24 --lpc 1 --pattern cube_color --color orange --host 10.0.0.125") | crontab -
	(crontab -l 2>/dev/null; echo "*/10 * * * * /home/entwined/Entwined/ddptest/ddptest.py  --cubes 24 --lpc 1 --pattern cube_color --color orange --host 10.0.0.126") | crontab -
	echo "Adding ddptest script for footlight color"
fi

echo "Setting ndbs for ddptest: $1"
cp $1/ndb_ips.txt ../../ddptest/

