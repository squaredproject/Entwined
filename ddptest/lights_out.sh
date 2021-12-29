#!/bin/bash

sudo service lx-headless stop
#sudo nmap  -sn 10.0.0.100-255 -oG - | awk '/Up$/{print $2}' > /home/entwined/Entwined/ddptest/ndb_ips.txt
xs="1 2 3"
for x in $xs; do
	echo LIGHTS OUT $x	
	cat ndb_ips.txt | while read ip; do
	python3 ddptest.py --pattern black --leds 480 --host $ip
done
sleep 5
done
