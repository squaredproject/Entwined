#!/bin/bash

## deinstall old networking
sudo systemctl daemon-reload
sudo systemctl disable --now ifupdown dhcpcd dhcpcd5 isc-dhcp-client isc-dhcp-common rsyslog
sudo apt --autoremove purge ifupdown dhcpcd dhcpcd5 isc-dhcp-client isc-dhcp-common rsyslog
sudo rm -r /etc/network /etc/dhcp

# setup/enable systemd-resolved and systemd-networkd
sudo systemctl disable --now avahi-daemon libnss-mdns
sudo apt --autoremove purge avahi-daemon
sudo apt install libnss-resolve
sudo ln -sf /run/systemd/resolve/stub-resolv.conf /etc/resolv.conf
sudo apt-mark hold dhcpcd dhcpcd5 isc-dhcp-client isc-dhcp-common libnss-mdns openresolv raspberrypi-net-mods rsyslog
sudo systemctl enable systemd-networkd.service systemd-resolved.service


# static ip address for eth0
# static ip address for wlan
sudo cp  ./04-wired.network /etc/systemd/network/
sudo cp  ./08-wifi.network /etc/systemd/network/

#sudo systemctl restart systemd-networkd
sudo reboot
