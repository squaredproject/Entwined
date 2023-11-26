#!/usr/bin/env python3


# This file takes a CSV file output from Google Docs and
# generates fixtures for elder mother.
# Each ndb maps into a 'branch' which has some number of
# 'droops' that hang down.


import json
import re
import os
import sys
import argparse

def tree_load_csv(csvFilename: str):
    branches = {} # dict with string is IP address, value is array of 16 ints for output length

    with open(csvFilename, "r") as csv_f:
        for csv_line in csv_f:
            values = csv_line.split(',')
            if len(values) != 5 or not values[0].isdigit():
                continue
            try:
                branch = values[0]
                droop = int(values[1]) - 1
                x = float(values[2])
                y = float(values[4])
                z = float(values[3])
                if branch not in branches:
                    branches[branch] = []
                branches[branch].append([x, y, z])
            except ValueError:
                print(f"Unexpected values in line {csv_line}, ignoring")
                continue

    return branches


def tree_load_ndb(ndbFilename: str):
    ndbs = []
    with open(ndbFilename, "r") as ndb_f:
        for line in ndb_f:
            line = line.rstrip("\n")
            if line.isnumeric():
                ndbs.append(line)
            else:
                print(f"Unexpected value in line {line}, ignoring")
    return ndbs


DELTA_Y = 6.75
CUBES_PER_DROOP = 16

def write_droop(n_cubes: int, fixtures_folder: str):
    lx_output = {"label": "droop_" + str(n_cubes),
                 "tags": ["DROOP"],
                 "components": [ {"type": "points", "coords": []} ],
                }
    coords = lx_output["components"][0]["coords"]
    for idx in range(n_cubes-1):
        coords.append({'x': 0, 'y': -idx * DELTA_Y, 'z': 0})
    # final cube wire was mismanufactured, only 1.5" below the others
    coords.append({'x':0, 'y': -(n_cubes-2)*DELTA_Y - 1.5, 'z':0})

    with open(fixtures_folder + "droop_" + str(n_cubes) + ".lxf", "w") as output_f:
        json.dump(lx_output, output_f, indent=4)



def write_fixture_files(ndbs, branches, fixtures_folder: str):
    # First, let's write fixture files that describe all possible types of droops -
    # from 6 cube to 16 cube.
    write_droop(CUBES_PER_DROOP, fixtures_folder)

    # now let's write the fixture file for each of the branches
    for ndb_idx in range(len(ndbs)):
        if str(ndb_idx+1) not in branches:
            print(f"ndb {ndb_idx} does not have associated branch")
            continue
        lx_output = {"label": "branch_" + str(ndb_idx),
                     "tags": ["BRANCH"],
                     "components": [],
                     "outputs": [],
                    }
        components = lx_output["components"]
        outputs = lx_output["outputs"]
        branch = branches[str(ndb_idx+1)]
        n_cubes = 0
        for droop in branch:
            components.append({"type": "droop_" + str(CUBES_PER_DROOP), "x": droop[0], "y": droop[1], "z": droop[2]})
            n_cubes += CUBES_PER_DROOP
        outputs.append({"protocol": "ddp", "host": "10.0.0." + ndbs[ndb_idx], "start": 0, "num": n_cubes})
        with open(fixtures_folder + "branch_" + str(ndb_idx) + ".lxf", "w") as output_f:
            json.dump(lx_output, output_f, indent=4)

    # and now the final elder mother file -
    lx_output = {"label": "elder_mother",
                 "tags": ["TREE", "elder_mother"],
                 "components": [],
                }
    components = lx_output["components"]
    for branch_idx in range(len(branches)):
        components.append({"type": "branch_" + str(branch_idx)})

    with open(fixtures_folder + "elder_mother.lxf", "w") as output_f:
        json.dump(lx_output, output_f, indent=4)



def main():
    parser = argparse.ArgumentParser(description="Create LxStudio configuration file from tree and cube definition files")
    parser.add_argument('-t', '--ndb_config', type=str, required=True, help='NDB IPS JSON configuration file')
    parser.add_argument('-b', '--cubes_config', type=str, required=True, help='Cubes CSV configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Name of folder to hold lx configurations')

    args = parser.parse_args()


    ndbs = tree_load_ndb(args.ndb_config)
    branches = tree_load_csv(args.cubes_config)
    write_fixture_files(ndbs, branches, args.fixtures_folder)

if __name__ == '__main__':
    main()
