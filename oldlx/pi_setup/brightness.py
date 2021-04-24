#!/usr/bin/env python3

# Request: {"method":"setBrightness","params":{"brightness":0}}
# Request: {"params":{"brightness":0},"method":"setBrightness"}
#
# Simply sends these requests to an endpoint, consumes whatever responses if any, and gracefully closes the socket
#
#
# usage: --host -> the host of the LX server processing system ( ie raspberry pi )
#        --brightness -b -> the value from 0 to 100 to set the brightness to
#

import socket
import time
import argparse

# brightness is 0 to 100
# this is differetn from the code where brightness is 0 to 1 but easier to remember
#
# Server port is always 5204. Can't be changed server side so don't bother here if you want add the args!
#
# not for some reason, the second one is not received. Luckly the first one works just fine it seems.
#

def send_brightness(host: str, brightness: int):

	val = brightness / 100.0
	m1 = "{{\"method\":\"setBrightness\",\"params\":{{\"brightness\":{}}}}}\n".format(val)
	b1 = m1.encode()

	m2 = "{{\"params\":{{\"brightness\":{}}},\"method\":\"setBrightness\"}}\n".format(val)
	b2 = m2.encode()


	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect((host, 5204))

	s.sendall(b1)
	time.sleep(0.2)

	s.sendall(b2)
	time.sleep(0.2)

	s.shutdown(socket.SHUT_RDWR)

	s.close()


def arg_init():
    parser = argparse.ArgumentParser(prog='ddptest', description='send a single json command to server')
    parser.add_argument('--host', type=str, required=True, help='IP address for destination')
    parser.add_argument('--brightness', '-b', type=int, required=True, help='one of: palette, hsv, order, shrub_rank, shrub_rank_order, cube_order, cube_color, black')

    args = parser.parse_args()
    return(args)

# inits then pattern so simple

def main():
    args = arg_init()

    send_brightness(args.host, args.brightness)



# only effects when we're being run as a module but whatever
if __name__ == '__main__':
    main()
