#!/bin/bash

###  script so that you can ssh to any entwined pi that has internet access using a reverse ssh tunnel
###  each pi sets up a tunnel using 950arnold.ddns.net as the remote server
###  each pi has to have a dedicated name, and ssh port defined in ports.json and saved to git
###  only use ports from 9070-9090
###  we are using the remote server 950arnold.ddns.net which is always on the internet and accessible remotely 
###  as our ssh tunnel gateway



if [ $# -eq 0 ]
	then
	    echo "tunnel.sh: requires the name of a tunnel in tunnels.json"
	    exit 1
	fi

if - test -f "ports.json"; then
    echo "$ports.json  exists."
fi
echo "Creating tunnel config for  ${1}...."
echo "Getting port for config ${1} from tunnels.json"

sudo apt-get install jq

PORT=`jq 'select(.message.name == "'${1}'") | .message.port' ports.json`
SSH=`jq 'select(.message.name == "'${1}'") | .message.ssh' ports.json`
if [ -z "$PORT" ]
	then
	    echo "tunnel.sh: cannot figure out what port to use for ssh tunnel setup, exiting early "
	    exit 1
	fi


# set up tunnel
echo "Setting up autossh tunnel using port $PORT" 
### setting up ssh keys
### ssh-keygen -t rsa -N ''
ssh -p 9091 ent@950arnold.ddns.net
ssh-copy-id -p 9091 -i ~/.ssh/id_rsa.pub ent@950arnold.ddns.net
ssh -p 9091 ent@950arnold.ddns.net "cat ~/.ssh/id_ed25519.pub" | tee -a ~/.ssh/authorized_keys


sudo apt-get install autossh
sudo apt-get install jq
SERVICE="entwined-tunnel.service"
echo "Updating port in $SERVICE"
cp  "${SERVICE}" "${SERVICE}.bak"
sed -i "s/\*:.*localhost/\*:${PORT}:localhost/g" $SERVICE

sudo cp $SERVICE /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl restart $SERVICE
sudo systemctl enable  $SERVICE

echo "adding cron job to restart tunnel every 15 mins"
CRONSTRING=$(crontab -l)
if [[ "$CRONSTRING" != *"entwined-tunnel"* ]];  then
	(crontab -l 2>/dev/null; echo "*/15 * * * * sudo systemctl restart entwined-tunnel") |  crontab -
fi

echo "Tunnel Creation Done: Test out new tunnel with this ssh cmd:$SSH"
