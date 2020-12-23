#!/usr/bin/env python
import json
import os
import sys

if len(sys.argv) == 1:
    print 'Usage:', sys.argv[0], '<input>... <output>'
    sys.exit(1)

if os.path.exists(sys.argv[-1]):
    print sys.argv[-1], 'exists, not overwriting and exiting.'
    sys.exit(2)

def filter_out_finish_event(entry):
    if ('event' in entry.keys() and entry['event'] == u'FINISH'):
        return True
    return False

max_millis = -1
def add_millis_forwards(entry):
    global max_millis
    max_millis = max(entry['millis'], max_millis)
    if entry['millis'] < max_millis:
        entry['millis'] += max_millis
    return entry

def output_master_fade_in(millis):
    global max_millis
    vols = [x/10.0 for x in range(0,11,1)]
    for vol in vols:
        millis += 200
        yield {u'millis': millis, u'event': u'MESSAGE', u'message': 'master/%s' % (vol,)}
        max_millis = max(millis, max_millis)

def output_master_fade_out(millis):
    global max_millis
    vols = [x/10.0 for x in range(10,-1,-1)]
    for vol in vols:
        millis += 200
        yield {u'millis': millis, u'event': u'MESSAGE', u'message': 'master/%s' % (vol,)}
        max_millis = max(millis, max_millis)

output = []
max_millis = 0
#for filename in sys.argv[1:-2]:
for filename in sys.argv[1:-1]:
    print 'Reading:', filename
    playback_run = json.loads(open(filename).read())

    beginning = True
    begun = False
    for event in playback_run:
        if filter_out_finish_event(event):
            continue
        if event['millis'] != 0:
            beginning = False
        if 'parameter' in event.keys() and event['parameter'] == 'master':
            continue
        if not beginning and not begun:
            begun = True
            for evt in output_master_fade_in(max_millis):
                output.append(evt)

        entry_to_output = add_millis_forwards(event)
        output.append(entry_to_output)

    for event in output_master_fade_out(max_millis):
        output.append(event)

output.append({u'millis': max_millis+1, u'event': u'FINISH'})


print 'Outputting:', sys.argv[-1]
open(sys.argv[-1], 'w').write(json.dumps(output))
