#!/bin/sh

echo "Rasberry Pi Entwined Setup"

HOME=/home/pi

git config --global user.email "mizpoon@burningart.com"
git config --global user.name "Entwined Pi"

#######################
## Update debian ######
#######################
#echo -e "*********** Updating OS packages **************"
#sudo apt-get update
#sudo apt-get dist-upgrade

sudo apt-get install emacs

sudo systemctl enable  brightness-toggle
sudo systemctl start  brightness-toggle

sudo apt-get install dos2unix
sudo apt-get install figlet
echo "figlet \"entwined meadow\"" >> ~/.bash_profile
######################
## Install Entwined ##
######################
cd $HOME
echo -e "\n\n********** downloading Entwined **************\n\n"
#cd $HOME;  git clone git@github.com:squaredproject/Entwined.git;

#####################################
## Temurin JDK 17 required
# https://blog.adoptium.net/2021/12/eclipse-temurin-linux-installers-available/
#####################################

sudo apt-get install -y wget apt-transport-https gnupg
#  THIS COMMAND HAS TO BE PHYSICALLY RUN AS ROOT, SUDO DOESNT WORK
# wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add -
#sudo echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
#sudo apt-get install temurin-17-jdk

sudo apt install maven
sudo apt install -y wget apt-transport-https
sudo mkdir -p /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list

sudo apt update
sudo apt upgrade
sudo apt install temurin-8-jdk
sudo apt install temurin-17-jdk --fix-missing
sudo update-alternatives --config java
exit 1

#####################################
## Entwined Service ##
#####################################
cd $HOME

echo -e "\n\n*********** Setting up entwined services **************\n\n"

cd $HOME

sudo cp Entwined/oldlx/pi_setup/chromatik.service /etc/systemd/system/
sudo cp Entwined/oldlx/pi_setup/brightness-toggle.service /etc/systemd/system/

sudo systemctl enable chromatik
sudo systemctl enable brightness-toggle


cd $HOME/Entwined/oldlx/pi_setup/

## install hostapd & others
sudo apt install -y hostapd dnsmasq 

## unblock wlan access
sudo rfkill unblock wlan

sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

## do not enable hostapd
#sudo systemctl unmask hostapd
#sudo systemctl enable hostapd
#sudo cp ./hostapd.conf /etc/hostapd/
sudo cp ./wpa_supplicant.conf /etc/wpa_supplicant/

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



exit 1

#######################
## Install Java 8 Open JDK  ######
#######################
sudo apt -y install openjdk-8-jdk

######################
## Install Entwined ##
######################
cd $HOME
echo -e "\n\n*********** Compiling Entwined **************\n\n"
git config --global user.name "Raspberry Pi"
cd $HOME;  git clone git@github.com:squaredproject/Entwined.git;
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

sudo cp Entwined/oldlx/pi_setup/chromatik.service /etc/systemd/system/
sudo cp Entwined/oldlx/pi_setup/brightness-toggle.service /etc/systemd/system/

sudo systemctl enable  lx-headless
sudo systemctl start  lx-headless



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

## unblock wlan access
sudo rfkill unblock wlan

sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

## do not enable hostapd
#sudo systemctl unmask hostapd
#sudo systemctl enable hostapd
#sudo cp ./hostapd.conf /etc/hostapd/
sudo cp ./wpa_supplicant.conf /etc/wpa_supplicant/

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


## delete old install
cd /home/pi; rm -rif  Entwined

## reboot all services
sudo systemctl reboot
