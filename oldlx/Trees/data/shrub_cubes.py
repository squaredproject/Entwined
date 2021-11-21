#!/usr/bin/env python3

import json
import os
import sys
import argparse

# This file will create the shrub cubes file from the definition
# file of the shrubs.

# the input file is a JSON with the following format:
#    {
#            "ry" : 0,
#            "x" : 0,
#            "z" : 0,
#            "type" : "classic",
#            "leds-per-cube": 4,
#            "pieceId" : "classic-01",
#            "shrubIpAddress" : "10.0.0.199"
#    },

# the output file has the following format:
#    {
#        "shrubIndex": 0,
#        "clusterIndex": 0,
#        "rodIndex": 1,
#        "treeOrShrub": "SHRUB",
#        "shrubOutputIndex": 0,
#        "cubeSizeIndex": 0,
#        "shrubIpAddress": "10.0.0.206"
#    },
# where: leds per cube is 1 -> cubeSizeIndex 0, leds per cube 4 -> cubeSizeIndex 2
# where 



def shrub_cubes_create(infile_name:str, outfile_name: str) -> None:
    with open(infile_name) as f:
        try:
            shrubs = json.load(f)
        except ValueError as e:
            print(' could not parse json file ',infile_name)
            print(e)
            return

    cubes = []
    shrubIndex = 0

    for shrub in shrubs:

        print(' shrub object is: ',shrub)
        print(' shrub ip address is: ',shrub['shrubIpAddress'])
        if 'cubeSizeIndex' in shrub:
            cubeSizeIndex = shrub['cubeSizeIndex']
        elif (shrub['leds-per-cube'] == 1):
            cubeSizeIndex = 0
        elif (shrub['leds-per-cube'] == 4):
            cubeSizeIndex = 1
        elif (shrub['leds-per-cube'] == 6):
            cubeSizeIndex = 2
        else:
            print(' leds-per-cube value of ',shrub['leds-per-cube'],' is not supported ')
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
                s['cubeSizeIndex'] = cubeSizeIndex
                s['shrubIpAddress'] = shrub['shrubIpAddress']
                cubes.append(s)

        shrubIndex += 1

    # overwrite the old output (slightly scary, better to write to a temp file or something)
    with open(outfile_name, 'w') as f:
        json.dump(cubes, f)
    print(' success writing ')


def arg_init():
    parser = argparse.ArgumentParser(prog='shrub_maint', description='Munge the ShrubCube File')

    parser.add_argument('--output', '-o', type=str, default="entwinedShrubCubes.json", help='file to output, cubes')
    parser.add_argument('--input', '-i', type=str, default="entwinedShrubs.json", help='file to input, shrubs')

    args = parser.parse_args()

    return args

# rules for arguments are a little peculiar

def main():
    args = arg_init()

    print(" file to input is {}".format(args.input))
    print(" file to output is {}".format(args.output))

    shrub_cubes_create(args.input, args.output)


# this only really impacts when this is being used as a modcule
# which it won't be
if __name__ == '__main__':
    main()