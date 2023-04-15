#!/bin/sh

echo "Rasberry Pi Entwined Setup"

HOME=/home/pi

#don't have all the pi checkins with the name name, force user to change
#git config --global user.email "mizpoon@burningart.com"
#git config --global user.name "Entwined Pi"

#######################
## Update debian ######
#######################
echo -e "*********** Updating OS packages **************"
sudo apt-get update
sudo apt-get dist-upgrade

sudo apt-get install -y emacs dos2unix figlet
echo "figlet \"entwined meadow\"" >> ~/.bash_profile
######################
## Install Entwined ##
######################
# no point in doing this because if we can execute this we already have entwined
#cd $HOME
#echo -e "\n\n********** downloading Entwined **************\n\n"
#cd $HOME;  git clone git@github.com:squaredproject/Entwined.git;

#####################################
## Temurin JDK 17 required
# https://blog.adoptium.net/2021/12/eclipse-temurin-linux-installers-available/
#####################################

echo -e "\n\n********** downloading Install correct java **************\n\n"
sudo apt-get install -y wget apt-transport-https gnupg
sudo apt install maven
sudo apt install -y wget apt-transport-https
sudo mkdir -p /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

sudo apt update
sudo apt upgrade
sudo apt install temurin-17-jdk --fix-missing
sudo update-alternatives --config java

#####################################
## Entwined Service ##
#####################################
cd $HOME

echo -e "\n\n*********** Setting up entwined services **************\n\n"

cd $HOME/Entwined/chromatik/pi_setup

sudo cp chromatik.service /etc/systemd/system/
sudo systemctl enable chromatik

sudo cp brightness-toggle.service /etc/systemd/system/
sudo systemctl enable brightness-toggle

## AUTHORIZE LICENSE
# java -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies-linux.jar heronarts.lx.studio.Chromatik --authorize  __LICENSE_KEY__
echo -e "\n\n ****************** Please check script to execute file for getting a production license\n\n"

cd ..; ./build.sh ; cd -

echo -e "\n\n ****************** Please replace with installation you want: this is ggp-2022\n\n"

cd ../installations; ./install.sh ggp-2022

## install hostapd & others
sudo apt install -y hostapd dnsmasq 

## unblock wlan access
sudo rfkill unblock wlan

sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

# copy the info of the access point to connect to
echo -e "\n\n ****************** Edit wpa_suplicant if you have a non-MIFI to connect to\n\n"
sudo cp ./wpa_supplicant.conf /etc/wpa_supplicant/

## define wlan1 wireless interface
sudo cat dhcpcd.conf >> /etc/dhcpcd.conf

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

### enable ssh
#   ASSUME SSH ALREADY INSTALLED OR YOU WOULDN'T BE ABLE TO EXECUTE THE SCRIPT
#echo -e "*********** enabling ssh access  **************"
#sudo apt-get --assume-yes install openssh-server
#sudo systemctl enable ssh

### enable Avahi mDNS so you can access the pi as pi.local
### this is required for the ipad application to connect to the pi (is this true?)
echo -e "*********** enabling Avahi mDNS  **************"
sudo apt-get install avahi-daemon
sudo sed -i 's/^#host-name.*$/host-name=pi/' /etc/avahi/avahi-daemon.conf
sudo sed -i 's/^#domain-name.*$/domain-name=local/' /etc/avahi/avahi-daemon.conf
sudo systemctl enable avahi-daemon
sudo systemctl restart avahi-daemon

#####################
####### Slow frame ###
####### Rate Fix   ###
######################
sudo sh -c 'sudo echo "net.ipv4.neigh.eth0.unres_qlen=1" >>  /etc/sysctl.conf '
sudo sh -c 'echo "net.ipv4.neigh.eth0.unres_qlen_bytes=4096" >>  /etc/sysctl.conf '

