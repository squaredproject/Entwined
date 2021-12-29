#!/usr/bin/env python3
import json
import os
import sys

if len(sys.argv) == 1:
    print('Usage:', sys.argv[0], '<file_to_finish.json>')
    sys.exit(1)
#
#if os.path.exists(sys.argv[-1]):
#    print sys.argv[-1], 'exists, not overwriting and exiting.'
#    sys.exit(2)

import re

filename = sys.argv[1]
run = json.loads(open(filename).read())

end = run[-1]
if end['event'] == 'FINISH':
    print(' file already has a finish command, untouched ')
else:

    entry = {}
    entry['event'] = 'FINISH'
    entry['millis'] = end['millis'] + 0.1
    run.append(entry)

    open(filename, 'w').write(json.dumps(run))

    print(' appended finish event ')
