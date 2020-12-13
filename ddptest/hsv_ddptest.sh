#!/bin/bash
# small
python3 ddptest.py --host 10.0.0.131 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.184 --leds 480 --pattern hsv &
sleep 10
# medium
python3 ddptest.py --host 10.0.0.110 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.179 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.199 --leds 480 --pattern hsv &
sleep 10
#shurb
python3 ddptest.py --host 10.0.0.206 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.209 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.212 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.216 --leds 480 --pattern hsv &
sleep 10
python3 ddptest.py --host 10.0.0.218 --leds 480 --pattern hsv &
sleep 10
sleep 3h
