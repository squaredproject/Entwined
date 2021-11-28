#!/bin/bash

CRONSTRING=$(crontab -l)
LIGHTS=$"lights_on"

if [[ "$CRONSTRING" != *"$LIGHTS"* ]];  then
        (crontab -l 2>/dev/null; echo "5 18 * * * /home/entwined/Entwined/ddptest/lights_on.sh") | crontab -
        (crontab -l 2>/dev/null; echo "11 22 * * * cd /home/entwined/Entwined/ddptest/; ./lights_out.sh") | crontab -
        echo "Creating cron jobs to turn lights on and off"
fi


echo "Adding statuscake"
if [[ "$CRONSTRING" != *"statuscake"* ]];  then
        (crontab -l 2>/dev/null; echo "*/5 * * * * /home/entwined/Entwined/oldlx/installations/ggp-2021/statuscake.sh") | c$fi
