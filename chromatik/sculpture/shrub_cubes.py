#!/usr/bin/env python3

import json
import os
import sys
import argparse

# This file will create the shrub cubes file from the definition
# file of the shrubs, in the NEW LX format.

# the input file is a JSON with the following format:
#    {
#            "ry" : 0, --> rotation
#            "x" : 0,  --> x in inches ("right" is positive)
#            "z" : 0,  --> z in inches ("back" is positive)
#            "type" : "classic",
#            "leds-per-cube": 4,  --> 4 is old shrubs, 1 is new shrubs
#            "pieceId" : "classic-01",
#            "shrubIpAddress" : "10.0.0.199"
#    },

# the output file has the following format:
#    {
#     "components": [
#       { "type": "points", "coords": [ { "x": 0, "y": 0, "z": 0 }, { "x": 0, "y": 0, "z": 0 }  ] }
#      ],
#    "outputs": [
#      { "protocol": "ddp",
#        "host": "127.0.0.1",
#          "start:: 0, "num": 100
#      }
#     ]
#    }
# where: leds per cube is 1 -> cubeSizeIndex 0, leds per cube 4 -> cubeSizeIndex 2
# where 
#
# THis is (basically) the old Java code from ShrubModel.java, translated. But, we don't have a 3D library
# that does transforms, so not quite sure the best way.


class Shrub:
    float ry = 0.0
    float x = 0.0
    float z = 0.0
    str shrubType = "" # type is a keyword
    str ip = ""



    def __init__(self, x: float, z: float, ry: float, shrubType: str, ip: str):
        self.x = x
        self.z = z
        self.ry = ry
        self.shrubType = shrubType
        self.ip = ip


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

        if 'cubeSizeIndex' in shrub:
            cubeSizeIndex = shrub['cubeSizeIndex']
        elif 'leds-per-cube' not in shrub:
            cubeSizeIndex = 1
            print(' no leds per cube or cube size index specified, assuming 4 LED cubes ')
        elif (shrub['leds-per-cube'] == 1):
            cubeSizeIndex = 0
        elif (shrub['leds-per-cube'] == 4):
            cubeSizeIndex = 1
        elif (shrub['leds-per-cube'] == 6):
            cubeSizeIndex = 2
        else:
            print(' leds-per-cube value of ',shrub['leds-per-cube'],' is not supported ')
            return

        if 'shrubIpAddress'not in shrub:
            print(' shrubIpAddress missing from a shrub, try again ')
            return

        outputIndex = 0
        for rodIndex in range(1,6):
            for clusterIndex in range(0,12):
                # there are faster ways but this reads very clean
                s = {}
                s['shrubIndex'] = shrubIndex
                s['clusterIndex'] = clusterIndex
                s['rodIndex'] = rodIndex
                s['outputIndex'] = outputIndex
                outputIndex += 1
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