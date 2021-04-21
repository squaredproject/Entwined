#!/bin/sh

echo "Rasberry Pi Entwined Setup"

HOME=/home/entwined

#######################
## Create User Entwineds ##
#######################
echo -e "\n\n\n*********** Creating new user entwined **************\n\n\n\n\n"
sudo useradd -m -d $HOME -s /bin/bash -p "thetrees!" entwined
sudo chmod -R a+w /home/entwined


#######################
## Update debian ######
#######################
#echo -e "*********** Updating OS packages **************"
sudo apt-get update
#sudo apt-get dist-upgrade


#######################
## Install Java 8 Open JDK  ######
#######################
sudo apt -y install openjdk-8-jdk

######################
## Compile Entwined ##
######################
cd $HOME
echo -e "\n\n*********** Compiling Entwined **************\n\n"
cd $HOME;  git clone https://github.com/squaredproject/Entwined.git;
cd Entwined/oldlx/  && sh compile.sh && cd $HOME
echo -e "\n\n*********** Done compiling entwined **************\n\n"

#####################################
## Entwined Service ##
#####################################
cd $HOME

echo -e "\n\n*********** Setting up entwined services **************\n\n"

cd $HOME

sudo cp Entwined/oldlx/lx-headless.service /etc/systemd/system/
sudo cp Entwined/oldlx/brightness-toggle.service /etc/systemd/system/

sudo systemctl enable  lx-headless
sudo systemctl start  lx-headless

sudo systemctl enable  brightness-toggle
sudo systemctl start  brightness-toggle


######################
####### Network ######
######################
### See https://www.raspberrypi.org/documentation/configuration/wireless/access-point.md

cd  $HOME/Entwined/pi/

echo -e "\n\n*********** configuring hostapd **************"
echo -e "\n\n\n*********** configuring wifi access point **************\n\n\n"
echo -e "*********** warning: running this script more than once  **************"
echo -e "*********** warning: appends lines to /etc/rc.local /etc/dhcpcd.conf /etc/sysctl.conf   **************"

sudo apt --assume-yes install dnsmasq hostapd bridge-utils -qq

echo -e "********** hostapd setup *****************"
sudo cp hostapd.conf  /etc/hostapd/hostapd.conf
sudo cp hostapd  /etc/default/hostapd

echo -e "********** editing etc/dnsmasq.conf *****************"
sudo cp dnsmasq.conf /etc/dnsmasq.conf

echo -e "********** editing /etc/dhcpcd.conf *****************"
sudo cp  /etc/dhcpcd.conf /etc/dhcpcd.conf.bak
echo "interface wlan1" >> /etc/dhcpcd.conf
echo "\t static ip_address=192.168.4.1/24" >> /etc/dhcpcd.conf
echo "\t nohook wpa_supplicant" >> /etc/dhcpcd.conf

echo "\n\ninterface eth0" >> /etc/dhcpcd.conf
echo "\tstatic ip_address=10.0.0.10/24" >> /etc/dhcpcd.conf

sudo systemctl unmask hostapd
sudo systemctl enable hostapd
sudo systemctl restart hostapd
sudo systemctl restart dhcpcd
sudo systemctl reload  dnsmasq

### Uncomment line re IP forwarding
echo -e "********** ip forwarding *****************"
sudo cp /etc/sysctl.conf  /etc/sysctl.conf.orig
sudo sed -i 's/^#net.ipv4.ip_forward=1/net.ipv4.ip_forward=1/' /etc/sysctl.conf

### Add masquerade for outbound traffic
sudo iptables -t nat -A  POSTROUTING -o wlan0 -j MASQUERADE

### Save IP tables
sudo sh -c "iptables-save > /etc/iptables.ipv4.nat"

### load IP tables on reboot
sudo cp /etc/rc.local /tmp/rc.local.orig
sudo sed -i 's/exit 0/iptables-restore < \/etc\/iptables.ipv4.nat/' /etc/rc.local


### enable ssh
echo -e "*********** enabling ssh access  **************"
sudo apt-get install openssh-server
sudo systemctl enable ssh

### enable Avahi mDNS so you can access the pi as rasberry.local on mac machines
### this is required for the ipad application to connect to the pi
### ipad app is hardcoded to connect to hostname odroid.local
### SO SET THE HOSTNAME TO odroid.local (even though this is a rasberry pi)
echo -e "*********** enabling Avahi mDNS  **************"
sudo apt-get install avahi-daemon
sudo sed -i 's/^#host-name.*$/host-name=pi/' /etc/avahi/avahi-daemon.conf
sudo systemctl enable avahi-daemon
sudo systemctl restart avahi-daemon

./wifi.sh

echo -e "*********** Done with hostapd **************"




 
### for some reason, networking doesn't work until you reboot
echo -e "*********** Rebooting to clean network configution issues *************"
sleep 2
echo -e "*********** Rebooting to clean network configution issues *************"
sleep 2
echo -e "*********** Rebooting to clean network configution issues *************"
sleep 4
sudo reboot
