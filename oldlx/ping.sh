#!/bin/bash

### Find NDP IP address on local network
### Pings all IP addresses from 10.0.0.1 - 10.0.0.254 

### Usage: ./ping.sh
### Notes: 10.0.0.100 - default NDP IP (needs setup)
### Notes: 10.0.0.10  - usully ethernet adapter (ignore)

for ip in $(seq 1 254); do (ping -c1 -t1 10.0.0.$ip 2>/dev/null | grep "bytes from" &); done