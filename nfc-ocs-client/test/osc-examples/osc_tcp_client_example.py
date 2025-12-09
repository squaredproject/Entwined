"""Small example OSC client

This program sends 10 random values between 0.0 and 1.0 to the /filter address,
waiting for 1 seconds between each value.
"""
import argparse
import time

import sys

sys.path.append(".")
from osc_tcp_client import OscTcpClient


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", default="127.0.0.1", help="The ip of the OSC server")
    parser.add_argument(
        "--port", type=int, default=7777, help="The port the OSC server is listening on"
    )
    args = parser.parse_args()

    client = OscTcpClient(args.ip, args.port)

    for x in range(10):
        address = "/channel/0/pattern/pattern1/enable"
        value = "T\n"
        try: 
            client.send_message(address, value)
            time.sleep(0.5)
            print(f"Sent {address}/{value} to {args.ip} @ {args.port}")
        except Exception as unknown_err:
            print(f'Error: {unknown_err}')
