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


def ip_change(filename:str, oldIp: str, newIp:str) -> None:
    with open(filename) as f:
        data = json.load(f)
    nChanged = 0
    for cube in data:
        if cube['ipAddress'] == oldIp:
            cube['ipAddress'] = newIp
            nChanged += 1
    with open(filename, 'w') as f:
        json.dump(data, f)
    print('number of cubes changed: {}'.format(nChanged))

def tree_delete(filename:str, treeIndex: int) -> None:
    with open(filename) as f:
        data = json.load(f)
    newdata = []
    for cube in data:
        if cube['treeIndex'] != treeIndex:
            newdata.append(cube)
    with open(filename, 'w') as f:
        json.dump(newdata, f)
    print(' deleted {} of id {}'.format(len(data)-len(newdata), treeIndex))

def ip_delete(filename:str, ipAddress:str):
    with open(filename) as f:
        data = json.load(f)
    newdata = []
    for cube in data:
        if cube['ipAddress'] != ipAddress:
            newdata.append(cube)
    with open(filename, 'w') as f:
        json.dump(newdata, f)
    print(' deleted {} of ip {}'.format(len(data)-len(newdata), ipAddress))

# adds a single cube with the correct IP address
# this apparently causes
def ip_add(filename:str, ipAddress:str) -> None:
    with open(filename) as f:
        data = json.load(f)
    # error check. Would suck to add one the already exists
    for cube in data:
        if cube['ipAddress'] == ipAddress:
            print(" won't add ipAddress {} already exists in file delete first".format(ipAddress))
            return

    # there are faster ways but this reads very clean
    c = {}

    c['ipAddress'] = ipAddress
    c['outputIndex'] = 0
    c['stringOffsetIndex'] = 0

    c['isActive'] = 'false'

    c['treeIndex'] = 0
    c['layerIndex'] = 0
    c['branchIndex'] = 0
    c['mountPointIndex'] = 0
    c['cubeSizeIndex'] = 1

    c['shrubIpAddress'] = ipAddress
    data.append(c)

    with open(filename, 'w') as f:
        json.dump(data, f)
    print(' success adding ')

# print basics about what's in the file
# right now just counting by ip address but you can add more
def dump(filename:str) -> None:
    with open(filename) as f:
        data = json.load(f)

    ipAddresses = {}

    for c in data:
        # count all the cubes of a given IP address for example
        ipAddress = c['ipAddress']
        i = ipAddresses.get(ipAddress, 0)
        ipAddresses[ipAddress] = i+1

    for ipAddress, count in ipAddresses.items():
        print(" {} has {} cubes".format(ipAddress,count))


def arg_init():
    parser = argparse.ArgumentParser(prog='tree-maint', description='Munge the entwinedCubes.json File')

    parser.add_argument('file', type=str, nargs=1, help='file to munge')
    parser.add_argument('--ip', type=str, help='ip address of cubes to modify')
    parser.add_argument('--newip', type=str, help='old ip address if changing')
    parser.add_argument('--tree', type=int, help='tree id to change')

    parser.add_argument('--delete', '-d', action='store_true' )
    parser.add_argument('--add', '-a', action='store_true')
    parser.add_argument('--change', '-c', action='store_true')
    parser.add_argument('--dump', action='store_true')

    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" file to munge is {}".format(args.file))
    if args.delete:
        print(" deleting this ip {}".format(args.ip))
        ip_delete(args.file[0], args.ip)

    elif args.add:
        if not args.ip:
            print(" adding a cube requires an IP")
            exit(-1)
        print(" adding a new cube with IP {}".format(args.ip))
        ip_add(args.file[0], args.ip)

    elif args.change:
        if not args.newip:
            print(" changing requires a new ip address (--newip) ")
            exit(-1)
        print(" changing IP address {} to {}".format(args.ip, args.newip))
        ip_change(args.file[0], args.ip, args.newip)

    elif args.delete:
        if args.ip:
            print(" deleting all cubes with IP address {}".format(args.ip))
            ip_delete(args.file[0], args.ip)
        elif args.tree:
            print(" deleting all cubes with tree {}".format(args.tree))
            tree_delete(args.file[0], args.tree)

    elif args.dump:
        dump(args.file[0])

    else:
        print(' input error, use --help for help ')
        exit(-1)


# this only really impacts when this is being used as a module
# which it won't be
if __name__ == '__main__':
    main()