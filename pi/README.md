# 2Squared Pi Install

Configure a rasberry pi to run 2Squared

### Instructions

  - Setup rasberry pi with a keyboard/mouse and monitor and USB wifi antenna
  - Using a seoarate computer burn latest rasberrian image to SD card
  -- Download Etcher https://www.balena.io/etcher/
  -- Download latest rasberrian image https://www.raspberrypi.org/downloads/raspbian/
  -- Burn image to SD card
  -- Put SD card in rasberry pi & start pi
  -- Follow instructions on screen, reset pi password and connect to wifi
  -- Open console as user pi
  -- git clone https://github.com/squaredproject/2squared.git
  -- cd 2squared/pi
  -- ./pi_setup.sh
- After a reboot, you should be able to connect to mini-trees wifi AP
- Check by running ssh pi@192.169.4.1

