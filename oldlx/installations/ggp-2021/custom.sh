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

echo "Setting ndbs for ddptest: $1"
cp $1/ndb_ips.txt ../../ddptest/

