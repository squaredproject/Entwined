#!/bin/bash

CRONSTRING=`crontab -l`

echo "Adding statuscake"
if [[ "$CRONSTRING" != *"statuscake"* ]];  then
        (crontab -l 2>/dev/null; echo "*/5 * * * * /home/pi/Entwined/chromatik/installations/$1/statuscake.sh") | crontab -
echo "Adding statuscake ping script"
fi

LIGHTS=$"lights_on"

if [[ "$CRONSTRING" != *"$LIGHTS"* ]];  then
    echo "NO CRON $CRONSTRING"
        (crontab -l 2>/dev/null; echo "0 5 * * * cd /home/pi/Entwined/ddptest; ./lights_on.sh > /tmp/lights_on 2>&1") | crontab -
        (crontab -l 2>/dev/null; echo "0 22 * * * cd /home/pi/Entwined/ddptest/; ./lights_out.sh > /tmp/lights_out 2>&1") | crontab -
        echo "Creating cron jobs to turn lights on and off"
fi
