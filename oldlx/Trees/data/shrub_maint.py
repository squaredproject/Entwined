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

def arg_init():
    parser = argparse.ArgumentParser(prog='cube-maint', description='Munge the ShrubCube File')

    parser.add_argument('file', type=str, nargs=1, help='file to munge')
    parser.add_argument('--shrub', '-i', type=int, required=True, help='shrub id to modify')
    parser.add_argument('--ip', type=str, help='ip address to set to')

    parser.add_argument('--delete', '-d', action='store_true' )
    parser.add_argument('--add', '-a', action='store_true')
    parser.add_argument('--change', '-c', action='store_true')

    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" file to munge is {} shrub id is {}".format(args.file,args.shrub))
    if args.delete:
        print(" deleting this shrub {}".format(args.shrub))
        shrub_delete(args.file[0], args.shrub)

    elif args.add:
        if not args.ip:
            print(" adding a shrub requires both an ID and an IP, you can cahnge it later tho")
            exit(-1)
        print(" adding a new shrub with this ID {} and IP {}".format(args.shrub,args.ip))
        shrub_add(args.file[0], args.shrub, args.ip)

    elif args.change:
        print(" changing IP address to {}".format(args.ip))
        shrub_changeIP(args.file[0], args.shrub, args.ip)

    else:
        print(' Must have exactly one of ip, add, or delete')
        exit(-1)


# this only really impacts when this is being used as a modcule
# which it won't be
if __name__ == '__main__':
    main()