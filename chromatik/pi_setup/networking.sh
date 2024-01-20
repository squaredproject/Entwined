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
sudo apt-mark hold avahi-daemon dhcpcd dhcpcd5 ifupdown isc-dhcp-client isc-dhcp-common libnss-mdns openresolv raspberrypi-net-mods rsyslog
sudo systemctl enable systemd-networkd.service systemd-resolved.service


# static ip address for eth0
sudo cat > /etc/systemd/network/04-wired.network <<EOF
[Match]
Name=e*

[Network]
Address=10.0.0.10/16
#MulticastDNS=yes
EOF

# wifi ip
sudo cat > /etc/systemd/network/08-wifi.network <<EOF
[Match]
Name=wl*

[Network]
Address=10.0.0.10/16
MulticastDNS=yes
EOF

