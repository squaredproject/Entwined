#!/usr/bin/env python3

#
# This takes a CSV file output from Google Docs and makes it into the sprawling
# hard to edit cubes file.
# In the big sheets, we had a sheet per NDB and such, I made a master list
# which is 'ndb_configs_export', which has a CSV format of:
# ndb (just last),tree(s,m.l),output,length,branch
# This information can be used to create both the NDB configs file, and also
# the cubes file
#
# Example file:
# https://docs.google.com/spreadsheets/d/1aHrftWILBURYRl3tOR9bsREWg8UhZZuntv1VR9yDDWE/edit?usp=sharing
#
# column `ndb`
# the last octet of the IPv4 address, will have 10.0.0. prepended.
# Column 'tree'
# this will be s, m, or l, which corresponds to one of the trees in the field
# by index.
# Column `output`
# Corresponds to the output from the NDB in question (1 index)
# Column `length`
# corresponds to the length, in cubes, attached to that output
# Column `branch`
# Using the following notation, a branch is specified.
# X.Y[L|R].[A|B]
# The first X is the layer. 0 is the lowest, 1 is the next higher, 2 if exists is above
# Y is the branch, specified as
# 0 - the longest branch
# 1R - the next clockwise branch
# 2R, 3R - and so on
# 4 the antipode of 0
# 3L, 2L, 1L continuing clockwise back to 0
#
# THIS IS CONFUSING and I recommend changing the scheme for the next instatllation.
# At that point, please make different version of this tool


# note that output is ONE indexed in these files and ZERO in the JSON files

# The NDBs are also ONE indexed (1 to 16) so input into google docs
# what is on the NDB.
# 
# Spreadsheet to track, by IP address, the length and location of each input
# https://docs.google.com/spreadsheets/d/10tKJYqjxg17QCM_UkV8Fsvh25ctIzKlinF9jcdDPwts/edit?usp=sharing
#


import json
import re
import os
import sys
import argparse


def tree_cubes_csv(csvFilename:str, cubeFilename:str, ndbFilename:str):

    cubes = []
    ndbs = {} # dict with string is IP address, value is array of 16 ints for output length

    with open(csvFilename, "r") as csv_f:
        lineNum = 1
        for csv_line in csv_f:
            if lineNum == 1:
                print(" check CSV header: {}".format(csv_line))
            else:
                values = csv_line.split(',') #only the one delimiter
                ndb = values[0]                # str
                tree = values[1].lower()       # str
                cubeSize = int(values[2])      # int
                output = int(values[3])        # int
                cubesNum = int(values[4])            # int, length of string, some are 0 if not used

                if cubesNum > 0:
                    branchStr = values[5]           # layer.branch.half
                    bValues = branchStr.strip().split('.')
                    layer = int(bValues[0])
                    branch = bValues[1].lower()
                    half = bValues[2].lower()

                    newCubes = tree_cube_make_object(ndb,output,tree,layer,branch,half,cubesNum,cubeSize,lineNum)
                    cubes.extend(newCubes)

                    update_ndbs(ndbs, ndb, output, cubesNum)

                # todo: it is possible to construct the ndbFile but probably not a good idea
                # it's actually pretty easy to do it manually and a good double check???

            lineNum += 1

    #print(' ndb output array is: ',ndbs)

    # write the output files
    with open(cubeFilename, 'w') as f:
        json.dump(cubes, f)

    with open(ndbFilename, 'w') as f:
        json.dump(ndbs, f)

def update_ndbs(ndbs, ndb:str, output: int, cubesNum: int):
    if '.' not in ndb:
        ndb = '10.0.0.' + ndb
    # if the ndb entry exists, update the array
    lengths = ndbs.get(ndb)
    if lengths:
        lengths[output-1] = cubesNum
    else:
        lengths = [0] * 16
        lengths[output-1] = cubesNum
        ndbs[ndb] = lengths


# file format:
# the entwinedCube file is a very large list of dicts,
# and each dict has
# {'treeIndex': 0,
#   'layerIndex' -> 0 is lowest
#   'branchIndex' -> 0 is shortest(?) and 0 points X-ward
#   'mountPointIndex': -> 0 is outer most and 8 inches each inward
#   'outputIndex': 0, -> the point in NDB's array
#   'cubeSizeIndex': 0 -> tells Engine how many LEDs
#   'ipAddress': '10.1.0.146'}


#this function allows you to input a series of branch points
# returns an object

def tree_cube_make_object(ip:str, output:int, tree, layer:int, branch:str, half:str, cubesNum:int, cubeSize:int, lineNum:int):


    print(' adding: ip {} output {} tree {} cubesize {} layer {} branch {} half {} cubes {} linenum {}'.format(ip,output,tree,cubeSize,layer,branch,half,cubesNum,lineNum))

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
            print(' tree must be int or s,m,l line:',lineNum)
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
            print(" branch {} not legal for layer 0 linenum: {}".format(branch,lineNum))
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
            print(" branch {} not legal for layer 1,2 line: {}".format(branch, lineNum))
            exit(-1)

    else:
        print(" layer {} not legal exiting line: {}".format(layer, lineNum))
        exit(-1)

    # check half
    if half != 'a' and half != 'b':
        print(' half must be a or b, is \"{}\" exiting line: {}'.format(half,lineNum))
        exit(-1)

    # it used to be that the mountPointIndex was 0 in the center, but we've moved it to
    # 0 at the outside, because it's hard to predict how close to the center a person starts,
    # but they always start at the outer edge. This means the _last_ cube in an output string has
    # to be zero


    # there are faster ways but this reads very clean
    cubes = []
    for c_idx in range(cubesNum):
        c = {}

        c['ipAddress'] = ip
        c['outputIndex'] = output - 1
        c['stringOffsetIndex'] = c_idx

        c['isActive'] = 'true'

        c['treeIndex'] = tree
        c['layerIndex'] = layer
        c['branchIndex'] = branch

        if half == 'a':
            c['mountPointIndex'] = (cubesNum - c_idx - 1) * 2
        else:
            c['mountPointIndex'] = ((cubesNum - c_idx - 1) * 2) + 1

        c['cubeSizeIndex'] = cubeSize
        cubes.append(c)

    return(cubes)



def arg_init():
    parser = argparse.ArgumentParser(prog='tree-csv', description='convert the CSV file to the entwinedCubes file')

    parser.add_argument('files', type=str, nargs=1, help='input.csv')
    parser.add_argument('--cubes', '-c', type=str, default="entwinedCubes.json", help='file to output, cubes')
    parser.add_argument('--ndbs', '-n', type=str, default="entwinedNDBs.json", help='file to output, ndbs')


    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" input {}".format(args.files[0]))
    print(" cubes output {}".format(args.cubes))
    print(" ndbs output {}".format(args.ndbs))

    tree_cubes_csv(args.files[0], args.cubes, args.ndbs)


# this only really impacts when this is being used as a module
# which it won't be
if __name__ == '__main__':
    main()