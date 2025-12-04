import sys,tty,os,termios
import subprocess
import time

''' Program to walk through the cubes on all the ndbs. Need to hardcode the ndb ip addresses
    and the number of cubes on the ndb. Maybe one day I will generate this information from
    the installation files, but that day is not today '''

ndbs = [("10.0.0.1", 256),
        ("10.0.0.2", 256)]

def getkey():
    old_settings = termios.tcgetattr(sys.stdin)
    tty.setcbreak(sys.stdin.fileno())
    try:
        while True:
            b = os.read(sys.stdin.fileno(), 3).decode()
            if len(b) == 3:
                k = ord(b[2])
            else:
                k = ord(b)
            key_mapping = {
                # 127: 'backspace',
                # 10: 'return',
                # 32: 'space',
                # 9: 'tab',
                27: 'esc',
                65: 'up',
                66: 'down',
                67: 'right',
                68: 'left'
            }
            if k in key_mapping:
                return key_mapping.get(k, chr(k))
    finally:
        termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)


def generate_ddptest_command(ndb_idx) -> str:
    return f"python ddptest.py --host {ndbs[ndb_idx][0]} --pattern order --lpc 1 --cubes {ndbs[ndb_idx][1]}"


def main():
    child_process = None
    cur_ndb_idx = 0
    try:
        cmd = generate_ddptest_command(cur_ndb_idx)
        child_process = subprocess.Popen(cmd.split()) #  stdout=subprocess.PIPE)

        while True:
            print(f"Enter left/right to change ndb: ")
            k = getkey()
            if k in ['right', 'left']:
                if k == 'right':
                    cur_ndb_idx = (cur_ndb_idx + 1) % len(ndbs)
                elif k == 'left':
                    cur_ndb_idx = (cur_ndb_idx - 1) % len(ndbs)
                print(f"  ndb idx is {cur_ndb_idx}")
                if child_process is not None:
                    child_process.terminate()
                cmd = generate_ddptest_command(cur_ndb_idx)
                child_process = subprocess.Popen(cmd.split()) #  stdout=subprocess.PIPE)
                time.sleep(0.3)
            elif k == 'esc':
                quit()
            else:
                print(k)
    except (KeyboardInterrupt, SystemExit):
        os.system('stty sane')
        print('stopping.')


if __name__ == "__main__":
    main()
