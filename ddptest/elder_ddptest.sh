#!/bin/bash
# small
pat="cube_order"
python3 ddptest.py --lpc 1 --host 10.0.0.201 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.202 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.203 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.204 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.205 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.206 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.207 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.208 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.209 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.210 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.211 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.213 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.214 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.215 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.216 --cubes 256 --pattern "$pat" &
python3 ddptest.py --lpc 1 --host 10.0.0.217 --cubes 256 --pattern "$pat" &

sleep 3h
