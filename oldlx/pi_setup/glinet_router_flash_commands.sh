#set the static ip to 10.0.1.1
sed -i 's/192.168.*/10.0.1.1/g' /etc/config/dhcp 
#move netmask to 255.255.0.0 instead of default 255.255.255.0
sed -i 's/NETMASK.*/NETMASK=255.255.0.0"/g' /bin/ipcalc.sh 
sed -i 's/255.255.255.0/255.255.0.0/g' /etc/config/network /etc/init.d/dnsmasq
sed -i 's/$netmask/255.255.0.0/g' /etc/init.d/dnsmasq
#set wan to lan port so the wired bridge works
sed -i "s/'general'/'general'\n\toption wan2lan '1'/g"   /etc/config/glconfig
#sed -i "s/ifname 'eth0.1'/ifname 'eth0.1 eth0.2'/g" /etc/config/network # net routers use different ifname
sed -i "/option ifname 'eth0'/d" /etc/config/network 
#only replace the first occurence of 192.168.*.* in this file
grep 10.0.1.1 /etc/config/network > /dev/null; if [ $? -eq 1 ]; then sed -i "s/`grep 192.168 /etc/config/network  | head -n1`/\toption ipaddr '10.0.1.1'\n\toption ifname 'eth1 eth0'/g" /etc/config/network ; fi
#replace the existing network with entwined wifi network
sed -i 's/SSID1.*/SSID1=entwined/g' /etc/wireless/mt7628/mt7628.dat
sed -i 's/WPAPSK1.*/WPAPSK1=theshrub!/g' /etc/wireless/mt7628/mt7628.dat
sed -i "s/option key.*/option key 'theshrub!'/g" /etc/config/wireless
sed -i "s/option ssid.*/option ssid 'entwined'/g" /etc/config/wireless 
