#!/usr/bin/env python3
import json
import os
import sys

if len(sys.argv) == 1:
    print( 'Usage:', sys.argv[0], '<file_to_clean.json>' )
    sys.exit(1)
#
#if os.path.exists(sys.argv[-1]):
#    print sys.argv[-1], 'exists, not overwriting and exiting.'
#    sys.exit(2)

import re

filename = sys.argv[1]
run = json.loads(open(filename).read())
out = []

prev_millis = 0
obj = 0


for entry in run:
    obj += 1

    # 10 second gap?
    if prev_millis + 10000 < entry['millis']:
        secs = (entry['millis'] - prev_millis) / 1000
        mins = secs / 60
        print(" object ",obj," gap seconds: ",secs," aka minutes ",mins)

    prev_millis = entry['millis']
