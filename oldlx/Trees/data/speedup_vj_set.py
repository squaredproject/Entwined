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

# make it 10x faster
for entry in run:
	entry['millis'] /= 10.0

open(filename, 'w').write(json.dumps(run))
