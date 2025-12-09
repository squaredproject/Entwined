#!/usr/bin/env python3
# 3waylabs creates LED controllers using a protocol called DDP
# DDP is a pretty sensible protocol, more straightforward and "internet style"

# WARNING has syntax that is python 3
# you will see strange looking errors in Python2
# not sure exactly which python is required...

# byte 0: flags: V V x T S R Q P
# V V: 2 - bits
#       for protocol version number, this document specifies version 1 (01).
# x: reserved
# T: timecode field added to end of header
#       if T & P are set, Push at specified time
# S: Storage.If set, data comes from Storage, not data - field.
# R: Reply flag, marks reply to Query packet. always set when any packet is sent
#       by a Display. if Reply, Q flag is ignored.
# Q: Query flag, requests len data from ID at offset(no data sent)
#       if clear, is a Write buffer packet
# P: Push flag, for display synchronization, or marks last packet of Reply
#
# byte 1: x x x x n n n n
#       x: reserved for future use(set to zero)
#       nnnn: sequence number from 1 - 15, or zero if not used
#           a sender can send duplicate packets with the same sequence
#           number and DDP header for redundancy. a receiver can ignore duplicates
#           received back - to - back. the sequence number is ignored if zero.
#
# byte 2: data type
#       data type, 0 = undefined [see notes below, still trying to figure this part out]
#
# byte 3: Source or Destination ID
#       0 = reserved
#       1 = default output device
#       2 = 249 custom IDs, (possibly defined via JSON config)
#       246 = JSON control(read / write)
#       250 = JSON config(read / write)
#       251 = JSON status(read only)
#       254 = DMX transit
#       255 = all devices
#
# byte 4 - 7: data offset in bytes( in units based on data - type.ie: RGB = 3 bytes = 1 unit) or bytes??
#       32 - bit number, MSB first
#
# byte 8 - 9: data length in bytes(size of data field when writing)
#       16 - bit number, MSB first
#           for Queries, this specifies size of data to read, no data field follows header.
#
# if T flag, header extended 4 bytes for timecode field ( not counted in data length)
# byte 10 - 13: timecode
#
# byte 10 or 14: start of data

import socket
import time
import argparse


DESTINATION_IP = "10.0.0.1"
DESTINATION_PORT = 4048
sock: socket = None
xmit_buf: bytearray = bytearray(0)
header_buf: bytearray = bytearray(0)

NUM_LEDS = 0
leds = bytearray(0)  # this constructor creates with the given length and filled with 0's
COLOR = "blue"

palette = {
    'red': (0xff, 0x00, 0x00),
    'green': (0x00, 0xff, 0x00),
    'blue': (0x00, 0x00, 0xff),
    'ltblue': (0xad, 0xd8, 0xe6),
    'dimblue': (0x1a, 0x22, 0x39),
    'yellow': (0xff, 0xff, 0x00),
    'black': (0x00, 0x00, 0x00),
    'white': (0xff, 0xff, 0xff),
    'cyan': (0x00, 0xff, 0xff),
    'magenta': (0xff, 0x00, 0xff),
    'gold': (0xff, 0xd7, 0x00),
    'brown': (0xa5, 0x2a, 0x2a),
    'chartreuse': (0x7f, 0xff, 0x00),
    'dark green': (0x00, 0x64, 0x00),
    'orange': (0xff, 0x67, 0x03)
}


# 'stolen' from stackoverflow, always easier than importing colorsys
# inputs from 0 to 1 but outputs from 0 to 255 (rounding of 255 values protably isn't right)

def hsv_to_rgb(h, s, v) -> tuple:
    if s == 0.0:
        r = (int) (v * 255)
        return (r, r, r)
    if (h > 1.0):
        h = h - int(h)
    i = int(h * 6.)  # XXX assume int() truncates!
    f = (h * 6.) - i
    p = v * (1. - s)
    q = v * (1. - s * f)
    t = v * (1. - s * (1. - f))

    p = int( p * 255)
    q = int( q * 255)
    t = int( t * 255)
    v = int( v * 255)

    i %= 6
    if i == 0: return (v, t, p)
    if i == 1: return (q, v, p)
    if i == 2: return (p, v, t)
    if i == 3: return (p, q, v)
    if i == 4: return (t, p, v)
    if i == 5: return (v, p, q)


# basic functions for creating and setting colors
# int is the number of color items to place (not bytes) - 0 means fill to the end of buff
# offset is in bytes, to account for potential headers
def color_fill(buf: bytearray, color: tuple, length: int = 0, offset: int = 0) -> None:
    if length == 0:
        length = int( (len(buf) - offset) / 3 ) # rounds down which is what we want
    o = offset
    for i in range(length):
        buf[o] = color[0]
        buf[o + 1] = color[1]
        buf[o + 2] = color[2]
        o += 3

# set a single LED to a color
def color_set(buf: bytearray, color: tuple, offset:int) -> None:
    # quicker to do the mod than if every time?
    offset *= 3
    offset %= len(buf)

    buf[ offset ] = color[0]
    buf[ offset+1] = color[1]
    buf[ offset+2] = color[2]


def leds_color_fill(color: tuple) -> None:
    color_fill(leds, color, NUM_LEDS)


def print_bytearray(b) -> None:
    l = len(b)
    o = 0
    while (l > 0):
        if (l >= 8):
            print('{:4x}: {:2x} {:2x} {:2x} {:2x} {:2x} {:2x} {:2x} {:2x}'.format(o, b[o+0],b[o+1],b[o+2],b[o+3],b[o+4],b[o+5],b[o+6],b[o+7]))
            l -= 8
            o += 8
        elif l == 7:
            print('{:4x}: {:2x} {:2x} {:2x} {:2x} {:2x} {:2x} {:2x} '.format(o, b[o+0], b[o+1], b[o+2], b[o+3], b[o+4], b[o+5], b[o+6]))
            l -= 7
            o += 7
        elif l == 6:
            print('{:4x}: {:2x} {:2x} {:2x} {:2x} {:2x} {:2x}'.format(o, b[o+0], b[o+1], b[o+2], b[o+3], b[o+4], b[o+5]))
            l -= 6
            o += 6
        elif l == 5:
            print('{:4x}: {:2x} {:2x} {:2x} {:2x} {:2x}'.format(o, b[o+0], b[o+1], b[o+2], b[o+3], b[o+4] ))
            l -= 5
            o += 5
        elif l == 4:
            print('{:4x}: {:2x} {:2x} {:2x} {:2x}'.format(o, b[o+0], b[o+1], b[o+2], b[o+3] ))
            l -= 4
            o += 4
        elif l == 3:
            print('{:4x}: {:2x} {:2x} {:2x}'.format(o, b[o+0], b[o+1], b[o+2]))
            l -= 3
            o += 3
        elif l == 2:
            print('{:4x}: {:2x} {:2x} '.format(o, b[o+0], b[o+1]))
            l -= 2
            o += 2
        elif l == 1:
            print('{:4x}: {:2x}'.format(o, b[o+0]))
            l -= 1
            o += 1


def network_init():
    global sock, NUM_LEDS, leds

    print("UDP target IP {} num leds {} len leds array {}".format( DESTINATION_IP,NUM_LEDS,len(leds) ) )
    #print("UDP target port:", DESTINATION_PORT)

    # create outbound socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # UDP
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # create an xmit buff with header
    global xmit_buf
    global header_buf
    # build a header
    b0 = (0x01 << 6) | 0x01 # version, plus push flag, all else 0
    b3 = 1

    # bytes 8, 9 are DATA LENGTH in bytes
    # // is integer "floor division"
    b8 = (len(leds) // 256) & 0xff
    b9 = len(leds) & 0xff

    # bytes 4,5,6,7 are OFFSET in data-type. Not clear why you would ever need this much offset
    header_buf = bytearray([b0, 0, 0, b3, 0, 0, 0, 0, b8, b9])
    xmit_buf = bytearray(int(len(leds) + len(header_buf)))
    xmit_buf[0:len(header_buf)] = header_buf


# transmit what is in the leds array - a little inefficient because it copies
def leds_send(leds=leds) -> None:
    global xmit_buf
    xmit_buf[10:] = leds
    # print_bytearray(xmit_buf)
    sock.sendto(xmit_buf, (DESTINATION_IP, DESTINATION_PORT))

# use this if you want to compute your own everything
def sendto(buf: bytearray) -> None:
    sock.sendto(buf, (DESTINATION_IP, DESTINATION_PORT))


# loop over all colors listed above, once a second
def pattern_palette():
    global leds
    while True:
        for name, color in palette.items():
            print('Sending color: {}'.format(name) )
            color_fill(leds, color)
            leds_send(leds)
            time.sleep(1.0)

# set to black and exit
def pattern_black():
    global leds
    color_fill(leds, palette['black'])
    leds_send(leds)

# set to color in palette and exit
def pattern_color(color: str):
    global leds
    color_fill(leds, palette[color])
    leds_send(leds)

# ring around the HSV, high fps
def pattern_hsv():
    global leds
    h = 0.0
    while True:
        h += 0.01
        color_fill(leds, hsv_to_rgb( h, 1.0, 1.0) )
        if h > 1.0:  h = 0.0
        leds_send(leds)
        time.sleep(0.2)

# just hit each pixel one by one:
def pattern_order():
    global leds, NUM_LEDS
    for i in range(NUM_LEDS * 3): leds[i] = 0x00
    i = 0
    while True:
        # note: colorset handles the wrapping
        color_set(leds,(255,255,255),i)
        color_set(leds,(0,0,0),i-1)
        i += 1
        leds_send(leds)
        time.sleep(0.4)

# light up cubes A, B, C, D, and E for each circular position in king shrub, one at a time.
# new style cubes (one led / 3 bytes per cube)
def pattern_order_new():
    global leds, NUM_LEDS
    for i in range(48*3*5): leds[i] = 0x00
    i = 0
    while True:

        for led in range(12):

            leds[led * 3 + 0] = 255
            leds[led * 3 + 1] = 255
            leds[led * 3 + 2] = 255

            leds[(led * 3 + 0) + 144] = 255
            leds[(led * 3 + 1) + 144] = 255
            leds[(led * 3 + 2) + 144] = 255

            leds[(led * 3 + 0) + 288] = 255
            leds[(led * 3 + 1) + 288] = 255
            leds[(led * 3 + 2) + 288] = 255

            leds[(led * 3 + 0) + 432] = 255
            leds[(led * 3 + 1) + 432] = 255
            leds[(led * 3 + 2) + 432] = 255

            leds[(led * 3 + 0) + 576] = 255
            leds[(led * 3 + 1) + 576] = 255
            leds[(led * 3 + 2) + 576] = 255

            leds_send(leds)
            time.sleep(0.4)

            for i in range(48*3*5): leds[i] = 0x00
            leds_send(leds)
        time.sleep(0.5)

# light up cubes A, B, C, D, and E for each circular position in king shrub, one at a time.
# old style cubes (four leds / 12 bytes per cube)
def pattern_order_old():
    global leds, NUM_LEDS
    for i in range(48*3*5): leds[i] = 0x00
    i = 0
    while True:

        for cube in range(12):
            for led in range(4):
                leds[(cube * 3 * 4) + (led * 3) + 0] = 255
                leds[(cube * 3 * 4) + (led * 3) + 1] = 255
                leds[(cube * 3 * 4) + (led * 3) + 2] = 255

                leds[(cube * 3 * 4) + (led * 3) + 0 + 144] = 255
                leds[(cube * 3 * 4) + (led * 3) + 1 + 144] = 255
                leds[(cube * 3 * 4) + (led * 3) + 2 + 144] = 255

                leds[(cube * 3 * 4) + (led * 3) + 0 + 288] = 255
                leds[(cube * 3 * 4) + (led * 3) + 1 + 288] = 255
                leds[(cube * 3 * 4) + (led * 3) + 2 + 288] = 255

                leds[(cube * 3 * 4) + (led * 3) + 0 + 432] = 255
                leds[(cube * 3 * 4) + (led * 3) + 1 + 432] = 255
                leds[(cube * 3 * 4) + (led * 3) + 2 + 432] = 255

                leds[(cube * 3 * 4) + (led * 3) + 0 + 576] = 255
                leds[(cube * 3 * 4) + (led * 3) + 1 + 576] = 255
                leds[(cube * 3 * 4) + (led * 3) + 2 + 576] = 255

            leds_send(leds)
            time.sleep(0.4)

            for i in range(48*3*5): leds[i] = 0x00
            leds_send(leds)
        time.sleep(0.5)

SHRUB_LEDS_PER_CUBE = 0
SHRUB_CUBES_PER_RANK = 12
SHRUB_RANKS = 5

# rank is 0 to 4
# cube is 0 to 11
def shrub_cube_set(rank:int , cube:int, color: tuple):
    start_cube = (rank * SHRUB_CUBES_PER_RANK) + cube
    start_led = SHRUB_LEDS_PER_CUBE * start_cube
    for i in range(SHRUB_LEDS_PER_CUBE):
        color_set(leds,color,i+start_led)

def shrub_rank_set(r:int):
    global leds, NUM_LEDS, SHRUB_LED_PER_CUBE, SHRUB_CUBES_PER_RANK, SHRUB_RANKS
    leds_color_fill( palette["black"] ) # do the dumb
    for i in range(SHRUB_RANKS):
        shrub_cube_set(r, i, palette["white"] )


# each rank in order
def pattern_shrub_rank():
    global leds, NUM_LEDS
    while True:
        for i in range(SHRUB_RANKS):
            shrub_rank_set(i)
            leds_send(leds)
            time.sleep(2.0)

# they say this will be good for testing
# for each rank, add each cube at 1 second, then hold the rank for 3 seconds
def pattern_shrub_rank_order():
    global leds, NUM_LEDS
    while True:
        for rank in range(SHRUB_RANKS):
            print(" testing rank: {}".format(rank))
            leds_color_fill ( palette["black"] )
            for cube in range(SHRUB_CUBES_PER_RANK):
                shrub_cube_set(rank, cube, palette["white"] )
                leds_send(leds)
                time.sleep(1.0)
            time.sleep(3.0)

## AARRRGGG! Need to parameterize... minitree has 4....
LEDS_PER_CUBE = 6

def cube_set(cube:int, color: tuple):
    global LEDS_PER_CUBE
    start_led = LEDS_PER_CUBE * cube
    for i in range(LEDS_PER_CUBE):
        # print("setting led {}".format(i+start_led))
        color_set(leds,color,i+start_led)

def pattern_cube_order(n_cubes: int):
    global leds, palette
    while True:
        #leds_color_fill ( palette["blue"] )
        leds_color_fill ( palette["red"] )
        leds_send(leds)
        time.sleep(1.0)
        for c_idx in range(n_cubes):
            cube_set(c_idx, palette["green"])
            leds_send(leds)
            time.sleep(0.5)

def pattern_strobe():
    global leds, palette
    while True:
        leds_color_fill ( palette["black"] )
        leds_send(leds)
        time.sleep(0.3)
        leds_color_fill ( palette["green"] )
        leds_send(leds)
        time.sleep(0.3)

def pattern_strobe_white():
    global leds, palette
    while True:
        leds_color_fill ( palette["black"] )
        leds_send(leds)
        time.sleep(0.3)
        leds_color_fill ( palette["white"] )
        leds_send(leds)
        time.sleep(0.3)

def pattern_strobe_all():
    global leds, palette
    while True:
        leds_color_fill ( palette["white"] )
        leds_send(leds)
        time.sleep(0.3) # white is important because it'll show a color cast
        leds_color_fill ( palette["red"] )
        leds_send(leds)
        time.sleep(0.9)
        leds_color_fill ( palette["green"] )
        leds_send(leds)
        time.sleep(0.3)
        leds_color_fill ( palette["blue"] )
        leds_send(leds)
        time.sleep(0.3)

palette_order = [
    (0xff, 0x00, 0x00),
    (0x00, 0xff, 0x00),
    (0x00, 0x00, 0xff),
    (0xff, 0xff, 0xff)
]

def pattern_cube_color(n_cubes: int):
    global leds, palette_order
    while True:
        for c_idx in range(n_cubes):
            cube_set(c_idx, palette_order[c_idx % len(palette_order) ])

        leds_send(leds)
        time.sleep(5)

# one shot and exit
def pattern_cube_color(n_cubes: int):
    global leds, COLOR
    if COLOR in palette:
        rgb = palette[COLOR]
    elif len(COLOR) == 6:
        # might be an rgb string?
        rgb = ( int(COLOR[0:2],16), int(COLOR[2:4],16), int(COLOR[4:6],16) )
        print(' setting to RGB ',rgb)
    else:
        print('color not in palette, must be one of ',list(palette.keys()))
        print('or a hex string with 6 characters')
        return

    for c_idx in range(n_cubes):
        cube_set(c_idx, rgb)

    leds_send(leds)


def arg_init():
    parser = argparse.ArgumentParser(prog='ddptest', description='Send DDP packets to an NDB for testing')
    parser.add_argument('--host', type=str, help='IP address for destination')
    parser.add_argument('--pattern', '-p', type=str, help='one of: palette, hsv, order, shrub_rank, shrub_rank_order, cube_order, cube_color, black')
    parser.add_argument('--leds', '-l', type=int, default=40, help='number of leds')
    parser.add_argument('--cubes', '-c', type=int, help='number of cubes')
    parser.add_argument('--lpc', type=int, default=6, help='leds per cube (default 6, 4 could be)')
    parser.add_argument('--color', type=str, default='blue', help='color to set all cubes to')

    global DESTINATION_IP, NUM_LEDS, leds, LEDS_PER_CUBE, COLOR

    args = parser.parse_args()
    if args.host:
        DESTINATION_IP = args.host
    # fugly! this is ordering dependant
    if args.lpc:
        LEDS_PER_CUBE = args.lpc
    if args.cubes:
        NUM_LEDS = args.cubes * LEDS_PER_CUBE
    # and so is this! must be last
    elif args.leds:   # there is a default, so this will be true
        print( "arg leds: {}".format(args.leds))
        NUM_LEDS = args.leds
    if (NUM_LEDS * 3 > 1490):
        print( " MTU will exceed 1500, aborting ")
        exit(-1)
    leds = bytearray(NUM_LEDS * 3)
    if args.color:
        COLOR = args.color

    return args


# inits then pattern so simple

def main():
    args = arg_init()

    network_init()

    if not args.pattern:
        pattern_palette()
    elif args.pattern == 'palette':
        pattern_palette()
    elif args.pattern == 'hsv':
        pattern_hsv()
    elif args.pattern == 'order':
        pattern_order()
    elif args.pattern == 'order_old':
        pattern_order_old()
    elif args.pattern == 'order_new':
        pattern_order_new()
    elif args.pattern == 'shrub_rank':
        pattern_shrub_rank()
    elif args.pattern == 'shrub_rank_order':
        pattern_shrub_rank_order()
    elif args.pattern == 'cube_order':
        if not args.cubes:
            print(" cube order requires, you must have cubes try again")
            exit(-1)
        pattern_cube_order(args.cubes)
    elif args.pattern == 'cube_color':
        if not args.cubes:
            print(" cube color, you must have cubes try again")
            exit(-1)
        pattern_cube_color(args.cubes)
    elif args.pattern == 'strobe':
        pattern_strobe()
    elif args.pattern == 'strobe_white':
        pattern_strobe_white()
    elif args.pattern == 'strobe_all':
        pattern_strobe_all()
    elif args.pattern == 'cube_color':
        pattern_cube_color()
    elif args.pattern in palette:
        pattern_color(args.pattern)
    else:
        print(' pattern must be one of palette, hsv, order, shrub_rank, shrub_rank_order, cube_order, cube_color, strobe, strobe_white')



# only effects when we're being run as a module but whatever
if __name__ == '__main__':
    main()
