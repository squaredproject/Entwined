#!/bin/sh

echo "Rasberry Pi Entwined Setup"

HOME=/home/entwined

#######################
## Create User Entwineds ##
#######################
echo -e "\n\n\n*********** Creating new user entwined **************\n\n\n\n\n"
sudo useradd -m -d $HOME -s /bin/bash -p "thetrees!" entwined
sudo chmod -R a+w /home/entwined

git config --global user.email "mizpoon@burningart.com"
git config --global user.name "Entwined Pi"

#######################
## Update debian ######
#######################
#echo -e "*********** Updating OS packages **************"
sudo apt-get update
sudo apt-get dist-upgrade

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

 #####################################
## Entwined Service ##
#####################################
cd $HOME

echo -e "\n\n*********** Setting up entwined services **************\n\n"

cd $HOME

sudo cp Entwined/oldlx/pi_setup/lx-headless.service /etc/systemd/system/
sudo cp Entwined/oldlx/pi_setup/brightness-toggle.service /etc/systemd/system/

sudo systemctl enable  lx-headless
sudo systemctl start  lx-headless

sudo systemctl enable  brightness-toggle
sudo systemctl start  brightness-toggle

#####################
####### Slow frame ###
####### Rate Fix   ###
######################
sudo sh -c 'sudo echo "net.ipv4.neigh.eth0.unres_qlen=1" >  /etc/sysctl.conf '
sudo sh -c 'echo "net.ipv4.neigh.eth0.unres_qlen_bytes=4096" >>  /etc/sysctl.conf '


######################
####### Network ######
######################
### See https://www.raspberrypi.org/documentation/configuration/wireless/access-:
######################

cd $HOME/Entwined/oldlx/pi_setup/

## install hostapd & others
sudo apt install -y hostapd dnsmasq 

sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

## enable hostapd
sudo systemctl unmask hostapd
sudo systemctl enable hostapd
sudo cp ./hostapd.conf /etc/hostapd/

## define wlan1 wireless interface
cat dhcpcd.conf >> /etc/dhcpcd.conf

## enable routing
sudo cp routed-ap.conf /etc/sysctl.d/
sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
sudo netfilter-persistent save

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






sudo cp dnsmasq.conf /etc/

## ensure wireless operation
sudo rfkill unblock wlan

### enable ssh
echo -e "*********** enabling ssh access  **************"
sudo apt-get --assume-yes install openssh-server
sudo systemctl enable ssh

### enable Avahi mDNS so you can access the pi as pi.local
### this is required for the ipad application to connect to the pi
echo -e "*********** enabling Avahi mDNS  **************"
sudo apt-get install avahi-daemon
sudo sed -i 's/^#host-name.*$/host-name=pi/' /etc/avahi/avahi-daemon.conf
sudo sed -i 's/^#domain-name.*$/domain-name=local/' /etc/avahi/avahi-daemon.conf
sudo systemctl enable avahi-daemon
sudo systemctl restart avahi-daemon

## reboot all services
sudo systemctl reboot
