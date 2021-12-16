#!/bin/sh

###
### any custom scripts for this pi go here
###
echo "Reno Entwined custom scripts"

echo "Installing status cake monitoring"
CAKE="https://push.statuscake.com/?PK=0f3ecb0c6b5698b&TestID=6083753&time=0"


#write out current crontab
crontab -l > /tmp/mycron

#echo new cron into cron file
echo "*/5 * * * * /usr/bin/curl ${CAKE} >/tmp/curl 2>&1" >> /tmp/mycron
#install new cron file
crontab /tmp/mycron
