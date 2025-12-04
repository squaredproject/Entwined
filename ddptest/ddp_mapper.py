#!/usr/bin/env python3
"""
DDP LED Mapper - Interactive tool for mapping LED installations
Allows you to navigate through LEDs one by one to determine physical layout

Uses only built-in Python libraries for cross-platform keyboard input:
- Windows: msvcrt
- Unix/Mac: termios, tty, select, os.read()
"""

import socket
import time
import argparse
import sys
import os

# Platform-specific keyboard imports (all are built-in)
if os.name == 'nt':
    import msvcrt
else:
    import termios
    import tty
    import select

# Color palette from ddptest.py
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


def parse_color(color_str):
    """Parse color string - first try palette name, then hex value"""
    # First, try to match against palette
    if color_str in palette:
        return palette[color_str]
    
    # Not in palette, try parsing as hex
    # Remove optional '#' prefix
    hex_str = color_str.lstrip('#')
    
    # Validate hex string (should be exactly 6 characters, all hex digits)
    if len(hex_str) == 6 and all(c in '0123456789abcdefABCDEF' for c in hex_str):
        return (int(hex_str[0:2], 16), int(hex_str[2:4], 16), int(hex_str[4:6], 16))
    
    # Invalid color specification
    raise ValueError(
        f"Invalid color: '{color_str}'. Must be a color name from palette "
        f"({', '.join(palette.keys())}) or 6-digit hex value (e.g., 'FF0000' or '#FF0000')"
    )


def parse_blink_pattern(pattern_str):
    """Parse blink pattern string into list of colors"""
    colors = pattern_str.split('-')
    try:
        return [parse_color(c) for c in colors]
    except ValueError as e:
        raise ValueError(f"Invalid blink pattern: {e}")


class LEDMapper:
    def __init__(self, host, num_leds, blink_pattern, background_color, blink_rate, fps, zero_index=False):
        self.host = host
        self.port = 4048
        self.num_leds = num_leds
        self.blink_pattern = blink_pattern
        self.background_color = background_color
        self.blink_rate = blink_rate
        self.fps = fps
        self.zero_index = zero_index
        
        self.current_led = 0
        self.blink_state = 0
        self.running = True
        self.jump_mode = False
        self.jump_buffer = ""
        
        # Initialize LED buffer and DDP socket
        self.leds = bytearray(num_leds * 3)
        self.sock = None
        self.xmit_buf = None
        
        # Timing
        self.last_blink_time = 0
        self.last_frame_time = 0
        
    def init_network(self):
        """Initialize DDP network connection"""
        print(f"DDP LED Mapper")
        print(f"Target: {self.host}:{self.port}")
        print(f"LEDs: {self.num_leds}")
        print(f"Blink pattern: {self.blink_pattern}")
        print(f"Background: {self.background_color}")
        print(f"Blink rate: {self.blink_rate}s, FPS: {self.fps}")
        print()
        
        # Create UDP socket
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        
        # Build DDP header
        b0 = (0x01 << 6) | 0x01  # version 1, push flag set
        b3 = 1  # destination ID
        data_len = len(self.leds)
        b8 = (data_len // 256) & 0xff
        b9 = data_len & 0xff
        
        header_buf = bytearray([b0, 0, 0, b3, 0, 0, 0, 0, b8, b9])
        self.xmit_buf = bytearray(len(self.leds) + len(header_buf))
        self.xmit_buf[0:len(header_buf)] = header_buf
        
    def set_led_color(self, led_index, color):
        """Set a single LED to a color"""
        offset = led_index * 3
        if offset < len(self.leds):
            self.leds[offset] = color[0]
            self.leds[offset + 1] = color[1]
            self.leds[offset + 2] = color[2]
    
    def fill_background(self):
        """Fill all LEDs with background color"""
        for i in range(self.num_leds):
            self.set_led_color(i, self.background_color)
    
    def update_led_buffer(self):
        """Update the LED buffer based on current state"""
        # Fill background
        self.fill_background()
        
        # Set current LED to blink color
        blink_color = self.blink_pattern[self.blink_state % len(self.blink_pattern)]
        self.set_led_color(self.current_led, blink_color)
    
    def send_frame(self):
        """Send current LED buffer via DDP"""
        self.xmit_buf[10:] = self.leds
        self.sock.sendto(self.xmit_buf, (self.host, self.port))
    
    def next_led(self):
        """Move to next LED"""
        self.current_led = (self.current_led + 1) % self.num_leds
        self.blink_state = 0
        self.update_led_buffer()
        self.print_status()
    
    def prev_led(self):
        """Move to previous LED"""
        self.current_led = (self.current_led - 1) % self.num_leds
        self.blink_state = 0
        self.update_led_buffer()
        self.print_status()
    
    def jump_to_led(self, led_index):
        """Jump directly to a specific LED (index in display format)"""
        # Convert from display index to internal 0-based index
        internal_index = led_index if self.zero_index else led_index - 1
        
        if 0 <= internal_index < self.num_leds:
            self.current_led = internal_index
            self.blink_state = 0
            self.update_led_buffer()
            self.print_status()
        else:
            min_idx = 0 if self.zero_index else 1
            max_idx = self.num_leds - 1 if self.zero_index else self.num_leds
            print(f"\nInvalid LED index: {led_index} (must be {min_idx}-{max_idx})")
    
    def print_status(self):
        """Print current status"""
        display_led = self.current_led if self.zero_index else self.current_led + 1
        max_led = self.num_leds - 1 if self.zero_index else self.num_leds
        print(f"\rCurrent LED: {display_led:4d} / {max_led}    ", end='', flush=True)
    
    def print_help(self):
        """Print help message"""
        print("\n" + "="*50)
        print("LED Mapper Controls:")
        print("  Arrow Right/Down : Next LED")
        print("  Arrow Left/Up    : Previous LED")
        print("  j or g           : Jump to LED (enter index)")
        print("  h or ?           : Show this help")
        print("  q or ESC         : Quit")
        print("="*50 + "\n")
    
    def handle_keyboard_windows(self):
        """Handle keyboard input on Windows using msvcrt"""
        if msvcrt.kbhit():
            ch = msvcrt.getch()
            
            # Handle arrow keys (they come as two bytes)
            if ch == b'\xe0' or ch == b'\x00':
                ch2 = msvcrt.getch()
                if ch2 == b'H':  # Up arrow
                    self.prev_led()
                elif ch2 == b'P':  # Down arrow
                    self.next_led()
                elif ch2 == b'K':  # Left arrow
                    self.prev_led()
                elif ch2 == b'M':  # Right arrow
                    self.next_led()
            elif ch == b'\x1b':  # ESC
                self.running = False
            elif ch in [b'q', b'Q']:
                self.running = False
            elif ch in [b'h', b'H', b'?']:
                self.print_help()
            elif ch in [b'j', b'J', b'g', b'G']:
                self.enter_jump_mode()
    
    def handle_keyboard_unix(self):
        """Handle keyboard input on Unix using termios.
        
        Uses os.read() to read all available buffered bytes at once,
        which is simpler and more reliable than reading byte-by-byte.
        """
        import select
        
        # Check if any input is available
        if select.select([sys.stdin], [], [], 0)[0]:
            # Read all available bytes at once (up to 10 should be plenty for any key sequence)
            data = os.read(sys.stdin.fileno(), 10).decode('utf-8', errors='ignore')
            print(f"\n[DEBUG] Waiting for input... Received: {repr(data)}", flush=True)
            
            # Check for arrow keys (escape sequences)
            if data == '\x1b[A':  # Up arrow
                print("[DEBUG] Arrow UP detected", flush=True)
                self.prev_led()
            elif data == '\x1b[B':  # Down arrow
                print("[DEBUG] Arrow DOWN detected", flush=True)
                self.next_led()
            elif data == '\x1b[C':  # Right arrow
                print("[DEBUG] Arrow RIGHT detected", flush=True)
                self.next_led()
            elif data == '\x1b[D':  # Left arrow
                print("[DEBUG] Arrow LEFT detected", flush=True)
                self.prev_led()
            elif data == '\x1b':  # Plain ESC key
                print("[DEBUG] ESC detected - exiting", flush=True)
                self.running = False
            elif data.lower() == 'q':
                print("[DEBUG] 'q' detected - exiting", flush=True)
                self.running = False
            elif data in ['h', 'H', '?']:
                print("[DEBUG] Help requested", flush=True)
                self.print_help()
            elif data.lower() in ['j', 'g']:
                print("[DEBUG] Jump mode requested", flush=True)
                self.enter_jump_mode()
            else:
                print(f"[DEBUG] Unhandled input: {repr(data)}", flush=True)
    
    def handle_keyboard(self):
        """Handle keyboard input - dispatch to appropriate method based on OS"""
        if self.jump_mode:
            self.handle_jump_input()
        elif os.name == 'nt':
            self.handle_keyboard_windows()
        else:
            self.handle_keyboard_unix()
    
    def enter_jump_mode(self):
        """Enter jump mode to input LED index"""
        self.jump_mode = True
        self.jump_buffer = ""
        min_idx = 0 if self.zero_index else 1
        max_idx = self.num_leds - 1 if self.zero_index else self.num_leds
        print(f"\nEnter LED index ({min_idx}-{max_idx}), then press Enter: ", end='', flush=True)
    
    def handle_jump_input(self):
        """Handle keyboard input in jump mode"""
        if os.name == 'nt':
            if msvcrt.kbhit():
                ch = msvcrt.getch()
                if ch == b'\r':  # Enter
                    self.exit_jump_mode()
                elif ch == b'\x1b':  # ESC
                    print("\nCancelled")
                    self.jump_mode = False
                    self.print_status()
                elif ch == b'\x08':  # Backspace
                    if self.jump_buffer:
                        self.jump_buffer = self.jump_buffer[:-1]
                        min_idx = 0 if self.zero_index else 1
                        max_idx = self.num_leds - 1 if self.zero_index else self.num_leds
                        print(f"\rEnter LED index ({min_idx}-{max_idx}), then press Enter: {self.jump_buffer} ", end='', flush=True)
                elif ch.isdigit():
                    self.jump_buffer += ch.decode()
                    print(ch.decode(), end='', flush=True)
        else:
            # Unix version - use os.read() for consistency with handle_keyboard_unix()
            import select
            if select.select([sys.stdin], [], [], 0)[0]:
                data = os.read(sys.stdin.fileno(), 10).decode('utf-8', errors='ignore')
                print(f"[DEBUG] Jump input received: {repr(data)}", flush=True)
                if data == '\n' or data == '\r':
                    self.exit_jump_mode()
                elif data == '\x1b' or data.startswith('\x1b'):
                    # ESC key or any escape sequence cancels jump mode
                    print("\nCancelled")
                    self.jump_mode = False
                    self.print_status()
                elif data == '\x7f' or data == '\x08':  # Backspace (DEL on Mac, BS on others)
                    if self.jump_buffer:
                        self.jump_buffer = self.jump_buffer[:-1]
                        min_idx = 0 if self.zero_index else 1
                        max_idx = self.num_leds - 1 if self.zero_index else self.num_leds
                        print(f"\rEnter LED index ({min_idx}-{max_idx}), then press Enter: {self.jump_buffer}  ", end='', flush=True)
                else:
                    # Add any digits from the input
                    for ch in data:
                        if ch.isdigit():
                            self.jump_buffer += ch
                            print(ch, end='', flush=True)
    
    def exit_jump_mode(self):
        """Exit jump mode and jump to entered LED"""
        self.jump_mode = False
        if self.jump_buffer:
            try:
                led_index = int(self.jump_buffer)
                self.jump_to_led(led_index)
            except ValueError:
                print("\nInvalid number")
                self.print_status()
        else:
            print("\nCancelled")
            self.print_status()
    
    def run(self):
        """Main loop"""
        self.init_network()
        
        # Set up terminal for non-blocking input on Unix/Mac
        old_settings = None
        if os.name != 'nt':
            old_settings = termios.tcgetattr(sys.stdin)
            try:
                tty.setcbreak(sys.stdin.fileno())
            except:
                print("Warning: Could not set up terminal for keyboard input")
        
        try:
            print("\nStarting LED mapper...")
            self.print_help()
            self.print_status()
            
            # Initialize buffer
            self.update_led_buffer()
            self.last_blink_time = time.time()
            self.last_frame_time = time.time()
            
            while self.running:
                current_time = time.time()
                
                # Check if it's time to toggle blink state
                if (current_time - self.last_blink_time) >= self.blink_rate:
                    self.blink_state = (self.blink_state + 1) % len(self.blink_pattern)
                    self.update_led_buffer()
                    self.last_blink_time = current_time
                
                # Check if it's time to send frame
                if (current_time - self.last_frame_time) >= (1.0 / self.fps):
                    self.send_frame()
                    self.last_frame_time = current_time
                
                # Handle keyboard input
                self.handle_keyboard()
                
                # Small sleep to avoid busy-waiting
                time.sleep(0.01)
            
            print("\nExiting...")
            
        finally:
            # Restore terminal settings on Unix/Mac
            if os.name != 'nt' and old_settings:
                try:
                    termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)
                except:
                    pass


def main():
    parser = argparse.ArgumentParser(
        prog='ddp_mapper',
        description='Interactive LED mapper for DDP-controlled LED installations',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s --host 10.0.0.1 --leds 100
  %(prog)s -H 192.168.1.50 -l 200 --blink red-green
  %(prog)s -H 10.0.0.1 -l 150 --background dimblue --blink-rate 1.0
  %(prog)s -H 10.0.0.1 -l 100 --background FF3300
        """
    )
    
    parser.add_argument('--host', '-H', type=str, required=True,
                        help='IP address of DDP LED controller')
    parser.add_argument('--leds', '-l', type=int, required=True,
                        help='Number of LEDs in installation')
    parser.add_argument('--blink', type=str, default='white-green',
                        help='Blink pattern: white-green (default), red-green, red-white-green, or custom')
    parser.add_argument('--background', '-bg', type=str, default='black',
                        help='Background color (color name or hex value, default: black)')
    parser.add_argument('--blink-rate', '-br', type=float, default=0.5,
                        help='Blink rate in seconds (default: 0.5)')
    parser.add_argument('--fps', type=int, default=10,
                        help='Frames per second for transmission (default: 10)')
    parser.add_argument('--zero-index', action='store_true',
                        help='Use 0-based indexing instead of 1-based (default: 1-based)')
    
    args = parser.parse_args()
    
    # Validate and parse colors
    try:
        background_color = parse_color(args.background)
        blink_pattern = parse_blink_pattern(args.blink)
    except ValueError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
    
    # Validate LED count for MTU
    if (args.leds * 3 > 1490):
        print("Error: Too many LEDs - MTU will exceed 1500 bytes", file=sys.stderr)
        sys.exit(1)
    
    # Create and run mapper
    mapper = LEDMapper(
        host=args.host,
        num_leds=args.leds,
        blink_pattern=blink_pattern,
        background_color=background_color,
        blink_rate=args.blink_rate,
        fps=args.fps,
        zero_index=args.zero_index
    )
    
    mapper.run()


if __name__ == '__main__':
    main()
