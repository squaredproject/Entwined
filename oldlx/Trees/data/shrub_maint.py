#!/usr/bin/env python3

import json
import os
import sys
import argparse

# file format:
# the shrubsCube file is a very large list of dicts,
# and each dict has
# {'shrubIndex': 0,
#   'clusterIndex': 0,
#   'rodIndex': 1,
#   'shrubOutputIndex': 0,
#   'cubeSizeIndex': 0,
#   'shrubIpAddress': '10.1.0.146'}


def shrub_changeIP(filename:str, shrubIndex: int, ipAddress:str) -> None:
    with open(filename) as f:
        data = json.load(f)
    nChanged = 0
    for shrub in data:
        if shrub['shrubIndex'] == shrubIndex:
            shrub['shrubIpAddress'] = ipAddress
            nChanged += 1
    with open(filename, 'w') as f:
        json.dump(data, f)
    print('number of shrubCubes changed: {}'.format(nChanged))

def shrub_delete(filename:str, shrubIndex: int) -> None:
    with open(filename) as f:
        data = json.load(f)
    newdata = []
    for shrub in data:
        if shrub['shrubIndex'] != shrubIndex:
            newdata.append(shrub)
    with open(filename, 'w') as f:
        json.dump(newdata, f)
    print(' deleted {} of id {}'.format(len(data)-len(newdata), shrubIndex))

# a bit more interesting. there are 12 'cluster index' and 5 'rod index' per cluster
# and the OutputIndex is the position on the NDB
def shrub_add(filename:str, shrubIndex: int, ipAddress:str) -> None:
    with open(filename) as f:
        data = json.load(f)
    # error check. Would suck to add one the already exists
    for shrub in data:
        if shrub['shrubIndex'] == shrubIndex:
            print(" can't add index {} already exists in file delete first".format(shrubIndex))
            return
    shrubOutputIndex = 0
    for clusterIndex in range(0,12):
        for rodIndex in range(1,6):
            # there are faster ways but this reads very clean
            s = {}
            s['shrubIndex'] = shrubIndex
            s['clusterIndex'] = clusterIndex
            s['rodIndex'] = rodIndex
            s['shrubOutputIndex'] = shrubOutputIndex
            shrubOutputIndex += 1
            s['cubeSizeIndex'] = 0
            s['shrubIpAddress'] = ipAddress
            data.append(s)
    with open(filename, 'w') as f:
        json.dump(data, f)
    print(' success adding ')

# will be nice to print out what shrubs their IP
def shrub_list(filename:str) -> None:
    with open(filename) as f:
        data = json.load(f)
    u = {}
    # error check. Would suck to add one the already exists
    for shrub in data:
        if shrub['shrubIndex'] not in u:
            print(" shrub index {} ::: ip {}".format(shrub['shrubIndex'],shrub['shrubIpAddress']))
            u[shrub['shrubIndex']] = True
    print(' printed all ')


def arg_init():
    parser = argparse.ArgumentParser(prog='cube-maint', description='Munge the ShrubCube File')

    parser.add_argument('file', type=str, nargs=1, help='file to munge')
    parser.add_argument('--shrub', '-i', type=int, help='shrub id to modify')
    parser.add_argument('--ip', type=str, help='ip address to set to')

    parser.add_argument('--delete', '-d', action='store_true', help='delete a shrub requires shrub index' )
    parser.add_argument('--add', '-a', action='store_true', help='add a shrub requires shrub index')
    parser.add_argument('--change', '-c', action='store_true', help='change a shrub requires shrub index')

    parser.add_argument('--list', '-l', action='store_true', help='list all shrub indexes and ips')

    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" file to munge is {}".format(args.file))
    if args.delete:
        if not args.shrub is None:
            print(" error: must supply --shrub or -i to delete that shrub")
            exit(-1)
        print(" deleting this shrub {}".format(args.shrub))
        shrub_delete(args.file[0], args.shrub)

    elif args.add:
        if args.ip is None:
            print(" error: adding a shrub requires both an ID and an IP")
            exit(-1)
        if args.shrub is None:
            print(" error: must supply --shrub or -i to add that shrub")
            exit(-1)
        print(" adding a new shrub with this ID {} and IP {}".format(args.shrub,args.ip))
        shrub_add(args.file[0], args.shrub, args.ip)

    elif args.change:
        if args.shrub is None:
            print(" error: must supply --shrub or -i to change that shrub")
            exit(-1)
        if args.ip is None:
            print(" error: changing a shrub requires both an ID and an IP")
            exit(-1)
        print(" changing shrub {} IP address to {}".format(args.shrub,args.ip))
        shrub_changeIP(args.file[0], args.shrub, args.ip)

    elif args.list:
        shrub_list(args.file[0])

    else:
        print(' Must have exactly one of add, delete, change, or list')
        exit(-1)


# this only really impacts when this is being used as a modcule
# which it won't be
if __name__ == '__main__':
    main()