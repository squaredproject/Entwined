#!/usr/bin/env python3

'''

A curses program for interactively driving LED sculptures that use the DDP
protocol, to quickly debug which LEDs are working and which may need some
attention.  Forked from opc-navigator[1], at integrated with ddptest.py[2].

If you find this useful, or fork it for other projects, please let me know :-)

[1] https://github.com/mct/junkdrawer/blob/master/opc/opc-navigator.py
[2] https://github.com/squaredproject/Entwined/blob/master/ddptest/ddptest.py

      -- Michael Toren <mct@toren.net>
         Tue Nov 23 22:15:06 PST 2021

'''

import curses
import argparse
import socket

class DDP:
    '''Largedly lifted from https://github.com/squaredproject/Entwined/blob/master/ddptest/ddptest.py'''
    def __init__(self, host, num_leds, leds_per_cube=1, port=4048):
        self.host = host
        self.port = port
        self.num_leds = num_leds
        self.leds_per_cube = leds_per_cube
        self.leds = bytearray(self.num_leds * 3)

        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

        # create an xmit buff with header
        b0 = (0x01 << 6) | 0x01 # version, plus push flag, all else 0
        b3 = 1

        # bytes 8, 9 are DATA LENGTH in bytes
        b8 = (len(self.leds) // 256) & 0xff
        b9 = len(self.leds) & 0xff

        # bytes 4,5,6,7 are OFFSET in data-type. Not clear why you would ever need this much offset
        header_buf = bytearray([b0, 0, 0, b3, 0, 0, 0, 0, b8, b9])

        self.xmit_buf = bytearray(int(len(self.leds) + len(header_buf)))
        self.xmit_buf[0:len(header_buf)] = header_buf

    # Set a single LED to a color
    def color_set(self, offset, color):
        offset *= 3
        offset %= len(self.leds)

        self.leds[offset+0] = color[0]
        self.leds[offset+1] = color[1]
        self.leds[offset+2] = color[2]

    # Set a single cube to a color
    def cube_set(self, cube, color):
        start_led = self.leds_per_cube * cube
        for i in range(self.leds_per_cube):
            self.color_set(start_led + i, color)

    def send(self):
        self.xmit_buf[10:] = self.leds
        try:
            self.sock.sendto(self.xmit_buf, (self.host, self.port))
        except OSError:
            pass

class Box:
    height = 5
    width = 7

    @classmethod
    def draw(cls, stdscr, line, col, number, color, selected=False):
        assert 0 <= number <= 999
        attr = 0

        if selected:
            ul = '╔'
            ur = '╗'
            h  = '═'
            v  = '║'
            ll = '╚'
            lr = '╝'
            #attr = curses.A_REVERSE
            attr = curses.A_BOLD
        else:
            ul = '╭'
            ur = '╮'
            h  = '─'
            v  = '│'
            ll = '╰'
            lr = '╯'

        # Top row
        stdscr.addstr(line, col, ul + h*5 + ur, attr)
        line += 1

        # Blank
        stdscr.addstr(line, col, v, attr)
        stdscr.addstr(line, col+1, ' '*5, color)
        stdscr.addstr(line, col+6, v, attr)
        line += 1

        # Number
        stdscr.addstr(line, col, v, attr)
        stdscr.addstr(line, col+1, f' {number:3} ', color)
        stdscr.addstr(line, col+6, v, attr)
        line += 1

        # Blank
        stdscr.addstr(line, col, v, attr)
        stdscr.addstr(line, col+1, ' '*5, color)
        stdscr.addstr(line, col+6, v, attr)
        line += 1

        # Bottom row
        stdscr.addstr(line, col, ll + h*5 + lr, attr)

class Screen:
    def __init__(self, stdscr, args):
        self.stdscr = stdscr
        self.num_leds = args.number_leds
        self.server = args.SERVER

        self.ddp = DDP(args.SERVER, self.num_leds, args.lpc)

        self.assign_color_index = 0
        self.red         = self.assign_color(curses.COLOR_BLACK, curses.COLOR_RED)
        self.green       = self.assign_color(curses.COLOR_BLACK, curses.COLOR_GREEN)
        self.blue        = self.assign_color(curses.COLOR_BLACK, curses.COLOR_BLUE)
        self.white       = self.assign_color(curses.COLOR_BLACK, curses.COLOR_WHITE)
        self.off         = self.assign_color(curses.COLOR_WHITE, curses.COLOR_BLACK)
        self.status_text = self.assign_color(curses.COLOR_WHITE, curses.COLOR_BLUE, curses.A_BOLD)
        self.error_text  = self.assign_color(curses.COLOR_WHITE, curses.COLOR_RED, curses.A_BOLD)

        self.box = Box
        self.box_color = self.off
        self.box_index = 0

        curses.curs_set(False)
        curses.halfdelay(10)
        self.set_all_leds(self.off)
        self.resize()

        while True:
            self.send()
            self.draw()
            self.handle_input()

    def assign_color(self, fg, bg, attr=0):
        self.assign_color_index += 1
        curses.init_pair(self.assign_color_index, fg, bg)
        return curses.color_pair(self.assign_color_index) | attr

    def resize(self):
        self.lines, self.cols = self.stdscr.getmaxyx()
        self.boxes_per_row = int(self.cols / self.box.width)

    def set_all_leds(self, color):
        self.box_color = color
        self.leds = [color] * self.num_leds

    def set_led(self, color):
        self.box_color = color
        self.leds = [self.off] * self.num_leds
        self.leds[self.box_index] = self.box_color

    def move_cursor(self, offset):
        if 0 <= self.box_index + offset < len(self.leds):
            self.box_index += offset
            self.set_led(self.box_color)

    def send(self):
        for i, led in enumerate(self.leds):
            if   led == self.off:   self.ddp.cube_set(i, (0, 0, 0))
            elif led == self.red:   self.ddp.cube_set(i, (255, 0, 0))
            elif led == self.green: self.ddp.cube_set(i, (0, 255, 0))
            elif led == self.blue:  self.ddp.cube_set(i, (0, 0, 255))
            elif led == self.white: self.ddp.cube_set(i, (255, 255, 255))
            else:
                raise("Internal error: Unknown color?")
        self.ddp.send()

    def quit(self):
        self.stdscr.clear()
        self.stdscr.refresh()
        self.set_led(self.off)
        self.send()
        exit()

    def draw(self):
        self.stdscr.erase()
        y, x = 0,0

        # draw status line
        self.stdscr.addnstr(y,x, f' DDP Navigator - {self.server}', self.cols-1, self.status_text)
        self.stdscr.chgat(-1, self.status_text)

        hotkeys = ' [q:Quit]  [r:Red]  [g:Green]  [b:Blue]  [w:White]  [o:Off]'
        x = self.cols - 1 - len(hotkeys)
        if x < 0: x = 0
        self.stdscr.addnstr(y,x, hotkeys, self.cols-1-x, self.status_text)

        y += 1
        x = 0

        truncated = False
        num_drawn = 0

        # Draw each box
        for i, color in enumerate(self.leds):
            self.box.draw(self.stdscr, y,x, i, color, selected=(i==self.box_index))
            num_drawn += 1
            x += self.box.width
            if x + self.box.width > self.cols:
                y += self.box.height
                x = 0
            if y + self.box.height > self.lines:
                truncated = True
                break

        if truncated:
            num_left = self.num_leds - num_drawn
            text = f'Warning: Dispay truncated! {num_left} LED{"s" if num_left > 1 else ""} not shown'
            y = self.lines-1
            x = self.cols-1 - len(text)
            self.stdscr.addnstr(y,x, text, self.cols-1-x, self.error_text)

    def handle_input(self):
        try:
            c = self.stdscr.getkey()
        except Exception as e:
            return

        if   c == 'KEY_RESIZE': self.resize()
        elif c == 'q': self.quit()

        elif c == 'KEY_LEFT':  self.move_cursor(-1)
        elif c == 'KEY_RIGHT': self.move_cursor(+1)
        elif c == 'KEY_UP':    self.move_cursor(-self.boxes_per_row)
        elif c == 'KEY_DOWN':  self.move_cursor(+self.boxes_per_row)

        elif c == 'h': self.move_cursor(-1)
        elif c == 'l': self.move_cursor(+1)
        elif c == 'k': self.move_cursor(-self.boxes_per_row)
        elif c == 'j': self.move_cursor(+self.boxes_per_row)

        elif c == '-': self.move_cursor(-1)
        elif c == '+': self.move_cursor(+1)
        elif c == '=': self.move_cursor(+1)

        elif c == '\x06': self.move_cursor(+1)                  # ^F
        elif c == '\x02': self.move_cursor(-1)                  # ^B
        elif c == '\x0E': self.move_cursor(+self.boxes_per_row) # ^N
        elif c == '\x10': self.move_cursor(-self.boxes_per_row) # ^P

        elif c == 'r': self.set_led(self.red)
        elif c == 'g': self.set_led(self.green)
        elif c == 'b': self.set_led(self.blue)
        elif c == 'w': self.set_led(self.white)
        elif c == 'o': self.set_led(self.off)

        elif c == '1': self.set_led(self.off)
        elif c == '2': self.set_led(self.red)
        elif c == '3': self.set_led(self.green)
        elif c == '4': self.set_led(self.blue)
        elif c == '5': self.set_led(self.white)
        elif c == '6': self.set_led(self.off)
        elif c == '0': self.set_led(self.off)

        elif c == '!': self.set_all_leds(self.off)
        elif c == '@': self.set_all_leds(self.red)
        elif c == '#': self.set_all_leds(self.green)
        elif c == '$': self.set_all_leds(self.blue)
        elif c == '%': self.set_all_leds(self.white)
        elif c == '^': self.set_all_leds(self.off)
        elif c == ')': self.set_all_leds(self.off)

        elif c == 'R': self.set_all_leds(self.red)
        elif c == 'G': self.set_all_leds(self.green)
        elif c == 'B': self.set_all_leds(self.blue)
        elif c == 'W': self.set_all_leds(self.white)
        elif c == 'O': self.set_all_leds(self.off)

        elif c == 'KEY_F(1)': self.set_all_leds(self.off)
        elif c == 'KEY_F(2)': self.set_all_leds(self.red)
        elif c == 'KEY_F(3)': self.set_all_leds(self.green)
        elif c == 'KEY_F(4)': self.set_all_leds(self.blue)
        elif c == 'KEY_F(5)': self.set_all_leds(self.white)
        elif c == 'KEY_F(6)': self.set_all_leds(self.off)
        elif c == 'KEY_F(0)': self.set_all_leds(self.off)

        elif c == 'KEY_BACKSPACE': self.set_all_leds(self.off)
        elif c == 'KEY_DC':        self.set_all_leds(self.off)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('SERVER',              type=str)
    parser.add_argument('--number-leds', '-n', type=int,  default=16*4)
    parser.add_argument('--lpc',         '-l', type=int,  default=1)
    args = parser.parse_args()

    curses.wrapper(Screen, args)
