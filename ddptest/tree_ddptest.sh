#!/bin/bash
# small
python3 ddptest.py --lpc 6 --host 10.0.0.111 --cubes 81 --pattern cube_order &
python3 ddptest.py --lpc 6 --host 10.0.0.112 --cubes 81 --pattern cube_order &
python3 ddptest.py --lpc 6 --host 10.0.0.121 --cubes 81 --pattern cube_order &
python3 ddptest.py --lpc 6 --host 10.0.0.122 --cubes 81 --pattern cube_order &
sleep 3h
