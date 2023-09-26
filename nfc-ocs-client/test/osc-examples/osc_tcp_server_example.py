"""TCP Server"""


import socket

from pythonosc import osc_bundle
from pythonosc import osc_message
from pythonosc.dispatcher import Dispatcher

import argparse

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", default="127.0.0.1", help="The ip to listen on")
    parser.add_argument("--port", type=int, default=7777, help="The port to listen on")
    args = parser.parse_args()

    dispatcher = Dispatcher()
    dispatcher.map("/channel/0/pattern/pattern1/enable", print)

    print("Serving on {}".format(args.ip))

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind((args.ip, args.port)) 
    s.listen(1)

    conn, addr = s.accept()
    while(1):
        data = conn.recv(200) 
        dispatcher.call_handlers_for_packet(data, "/channel/0/pattern/pattern1/enable")
    conn.close()