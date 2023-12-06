#!/bin/sh                                                                                                                                                                                  

### script to add cron tab to turn on and off entwined

# check to insure the entry does not already exist 
crontab -l | grep 'lights_on.sh' > /dev/null 2>&1 
if [ $? -eq 0 ]; then
    echo "Cron already exists... exiting..."
fi

# globals    
LOG="/tmp/log"
OFF=22
ON=5

OFF="* $OFF * * * cd /home/pi/Entwined/ddptest; ./lights_out.sh >> $LOG 2>&1"
ON="* $ON * * * cd /home/pi/Entwined/ddptest; ./lights_on.sh >> $LOG 2>&1"

# add to crontab
(crontab -l 2>/dev/null; echo "$OFF") | crontab -  
(crontab -l 2>/dev/null; echo "$ON") | crontab -

