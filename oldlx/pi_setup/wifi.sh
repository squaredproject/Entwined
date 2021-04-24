# https://www.raspberrypi.org/documentation/configuration/wireless/access-point-routed.md


## install hostapd & others
sudo apt install hostapd
sudo apt install dnsmasq

sudo DEBIAN_FRONTEND=noninteractive apt install -y netfilter-persistent iptables-persistent

## enable hostapd
sudo systemctl unmask hostapd
sudo systemctl enable hostapd

## define wlan1 wireless interface
cat wlan1.conf >> /etc/dhcpcd.conf

## enable routing
sudo cp routed-ap.conf /etc/sysctl.d/
sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
sudo netfilter-persistent save

sudo cp dnsmasq.conf /etc/

###


## ensure wireless operation
sudo rfkill unblock wlan
sudo cp ./hostapd.conf /etc/hostapd/

## reboot all services
sudo systemctl reboot
