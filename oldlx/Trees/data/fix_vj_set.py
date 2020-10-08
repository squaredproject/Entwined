#!/usr/bin/env python
import json
import os
import sys

if len(sys.argv) == 1:
    print 'Usage:', sys.argv[0], '<file_to_clean.json>'
    sys.exit(1)
#
#if os.path.exists(sys.argv[-1]):
#    print sys.argv[-1], 'exists, not overwriting and exiting.'
#    sys.exit(2)

import re

filename = sys.argv[1]
run = json.loads(open(filename).read())
out = []
for entry in run:
    if ('event' in entry.keys() and 
            entry['event'] == u'PARAMETER'):
        if not entry['parameter']:
            continue
        parts = entry['parameter'].split('/')
        try:
            if int(parts[1]) > 7:
                #print 'skipping', parts
                continue
            out.append(entry)
        except ValueError:
            out.append(entry)
    else:
        out.append(entry)

open(filename, 'w').write(json.dumps(out))
