#!/usr/bin/env python3
import argparse
import socket

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Test for sending OSC like messages to Entwined')
    parser.add_argument('pattern', type=str, help='Name of pattern to invoke')
    parser.add_argument('channel', type=int, help='Channel [0,3]')
    parser.add_argument('onOff', type=str, help='Pattern on or off [true|false]')
    parser.add_argument('--port', type=int, help='Optional port id', default=7777)
    args = parser.parse_args()

    if args.port < 0:
        print(f"Port value {args.port} invalid")
        exit(-1)

    if args.channel < 0 or args.channel > 3:
        print(f"Channel value {args.channel} invalid")
        exit(-1)

    onOff = args.onOff in ['true', 'TRUE', 't', 'True']
    osc_string = f"channel/{args.channel}/pattern/{args.pattern} ,{'T' if onOff else 'F'}\n"

    # Open TCP connection
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
      s.connect(("127.0.0.1", args.port))
      print("Connected to server")
      s.sendall(bytes(osc_string, "ascii"))
      print("sent bytes")

    print(f"Sent data {osc_string} to localhost at port {args.port}")
    exit(0)
