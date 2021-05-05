#!/usr/bin/env python3

import json
import os
import sys
import argparse

# file format:
# the entwinedCube file is a very large list of dicts,
# and each dict has
#    {
#        "ipAddress": "10.0.0.110",
#        "outputIndex": 0,
#        "stringOffsetIndex": 0,
#        "isActive": "true",
#        "treeIndex": 1,
#        "layerIndex": 1,
#        "branchIndex": 1,
#        "mountPointIndex": 1,
#        "cubeSizeIndex": 1
#    }
#
# This uses the "jdv system" to name branches.
# That system works by calling the longest branch 0, then 1R (clockwise)
# 2R, 3R, 4 (the opposite of 1), then 3L, 2L, 1L. 
# Everyone found this confusing.


# this function allows you to input a series of branch points

def tree_cubes_input(filename:str):

    with open(filename) as f:
        data = json.load(f)

    ip = input(' ip address:')
    output = int( input(' output (1 index):') )
    tree = input(' tree (0,1,2 or s,m,l):')

    layer = int(input(' layer:'))
    branch = input(' branch:')
    half = input(' half: (a or b)')
    cubes = int( input(' number of cubes:'))

    print(' adding: ip {} output {} tree {} layer {} branch {} half {} cubes {}'.format(ip,output,tree,layer,branch,half,cubes))

    # helper for ip, add the 10.0.0. if not there
    if '.' not in ip:
        ip = '10.0.0.' + ip

    # convert tree
    if tree.isalpha():
        if tree == 's':
            tree = 2
        elif tree == 'm':
            tree = 1
        elif tree == 'l':
            tree = 0
        else:
            print(' tree must be int or s,m,l')
            exit(-1)
    else:
        tree = int(tree)

    # convert branch
    if layer == 0:
        if branch == '0':
            branch = 0
        elif branch == '1r':
            branch = 1
        elif branch == '2r':
            branch = 2
        elif branch == '3r':
            branch = 3
        elif branch == '4':
            branch = 4
        elif branch == '3l':
            branch = 5
        elif branch == '2l':
            branch = 6
        elif branch == '1l':
            branch = 7
        else:
            print(" branch {} not legal for layer 0".format(branch))
            exit(-1)
    # warning. This is a little strange because the code 
    # calls the branches in layer two differently than the file does.
    # in the file, and in the cube editor, everything is 0 to 3
    elif layer == 1 or layer == 2:
        if branch == '0':
            branch = 0
        elif branch == '2r':
            branch = 1
        elif branch == '4':
            branch = 2
        elif branch == '2l':
            branch = 3
        elif branch == '1r':
            branch = 0
        elif branch == '3r':
            branch = 1
        elif branch == '3l':
            branch = 2
        elif branch == '1l':
            branch = 3
        else:
            print(" branch {} not legal for layer 1,2".format(branch))
            exit(-1)

    else:
        print(" layer {} not legal exiting".format(layer))
        exit(-1)

    # check half
    if half != 'a' and half != 'b':
        print(' half must be a or b, exiting')
        exit(-1)

    # pause waiting for input to validate....
    input(' ready? ')

    for c_idx in range(cubes):
        # there are faster ways but this reads very clean
        c = {}

        c['ipAddress'] = ip
        c['outputIndex'] = output - 1
        c['stringOffsetIndex'] = c_idx

        c['isActive'] = 'true'

        c['treeIndex'] = tree
        c['layerIndex'] = layer
        c['branchIndex'] = branch

        if half == 'a':
            c['mountPointIndex'] = c_idx * 2
        else:
            c['mountPointIndex'] = (c_idx * 2) + 1

        c['cubeSizeIndex'] = 1

        data.append(c)

    with open(filename, 'w') as f:
        json.dump(data, f)    

    print(' done! ')



def arg_init():
    parser = argparse.ArgumentParser(prog='tree-input', description='Munge the entwinedCubes.json File')

    parser.add_argument('file', type=str, nargs=1, help='file to munge')

    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" file to change is {}".format(args.file))

    tree_cubes_input(args.file[0])


# this only really impacts when this is being used as a module
# which it won't be
if __name__ == '__main__':
    main()