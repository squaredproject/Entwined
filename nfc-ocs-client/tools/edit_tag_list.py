import nfc
from binascii import hexlify
import json
import signal
import time 
import os

start_time_ms = 0
dicti = {}


class Sighandler:
    """SIGTERM and SIGINT handler"""

    def __init__(self) -> None:
        self.sigint = False

    def signal_handler(self, sig, han):
        """Ack received handler"""
        self.sigint = True
        print(f"\n***{signal.Signals(sig).name} received. Exiting...***")

def edit_json(tag): 
    global dicti
    tag_id = hexlify(tag.identifier).decode().upper()
    color = input("new tag, enter color (options y, b, r, o)")
    oneshot = input("new tag, enter one shot (options y, n)")
    dicti[tag_id] = ["name", color, oneshot]
    print(f'{dicti}')
    
    return True

def print_json(tag):
    global dicti 
    tag_id = hexlify(tag.identifier).decode().upper()
    values = dicti[tag_id]
    print(f'{tag_id}, {values}')


def start_poll(targets):
    """Start the stop watch. Must return targets to clf"""
    global start_time_ms 
    start_time_ms = time.time_ns() / 1000000
    return targets

def timeoutss():
    """
    Return whether time > TIMEOUT_S has elapsed since last call of start_poll()
    """
    elapsed = (time.time_ns() / 1000000) - start_time_ms
    return elapsed > 1000

if __name__ == "__main__":
    clf = nfc.ContactlessFrontend('ttyUSB2')

    handler = Sighandler()
    signal.signal(signal.SIGINT, handler.signal_handler)
    signal.signal(signal.SIGTERM, handler.signal_handler)

    global f
    f = open("elder_mother_config.json", "r")
    dicti = json.load(f)

    while not handler.sigint:
        try:
            tag = clf.connect(rdwr={'on-startup': start_poll,'on-connect': print_json, "iterations": 1,"interval": 0.5}, terminate=timeoutss)
            if tag is None:
                os.system('clear')
            time.sleep(0.1)
        except Exception as uknown_exception:
            print(f'{unknown_exception}')
            clf.close()
    clf.close()

    # f = open("elder_mother_config.json", "w")
    # f.write(json.dumps(dicti))