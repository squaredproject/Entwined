#!/usr/bin/env python3

# Source of truth: https://github.com/squaredproject/Entwined
# Author: Brian Bulkowski brian@bulkowski.org
# Fixture files for Entwined "Fruit" clusters

# Turn an Entwined shrub definition file into an LXstudio fixture file(s)

# NOTE: I am sorry about "fruits". It's not english. But we need to have a plural
# different from a singular in so many places that I'm using fruits.

import argparse
import json
from pathlib import Path

import numpy as np


# Fruit are defined as follows:
#
# Each layer has N cubes, where in first rev, N=3
# Layers are numbered TOP DOWN
# In the first rev, there are 3 layers
# The cubes in each layer are evenly distributed axially
# If the first layer is at "zero", the second layer is rotated 60 degrees, the next one is the same as the first
# 

# we will take 0,0,0 as on the axis, but... where to place Y=0???? hard to say

# Fully arbitrary code would take as parameters the number of layers,
# the radial distance of each layer, vertical space between layers.

# distance is INCHES

class Fruit:

    # idea: the json file could have all this data in the config.
    # when we have more types of fruit, we can move each of these parameters into the config file

    # number of layers per fruit
    layers = 3
    # Y distance from Y=0 to a given layer
    y_distance_layers = [ 0, 6, 4 ]
    # radial distance of each layer (to center point of cube)
    rad_distance_layers = [ 4, 8, 4 ]
    # Rotation of a given layer
    ry_layers = [ 0, 60, -60 ]
    # number of cubes in a layer
    ncube_layers = [ 3, 3, 3 ]

    def __init__(self, config) :
        # print(f"Fruit config is {config}")
        self.piece_id = config['pieceId']
        self.ip_addr = config['ipAddress']
        self.ledsPerCube = config['ledsPerCube']
        self.type = config['type'] + 'Fruit'


#        print(f"rotation in degrees: {config['ry']}")
        rot = (np.pi / 180.0) * config['ry'] # get into radians
#        print(f"rotation in radians: {rot}")
        self.rotation = np.array([
            [np.cos(rot),0,np.sin(rot)],
            [0,1,0],
            [-np.sin(rot), 0, np.cos(rot)]
        ])
        self.translation = np.array([config['x'], config['y'], config['z']])
        self.cubes = []

        self.ry = config['ry']

        self.calculate_cubes()


    def calculate_cubes(self):
        # See above for distances
        for layer_idx in range(self.layers):

            radius = self.rad_distance_layers[layer_idx]
            y_distance = self.y_distance_layers[ layer_idx ]

            ry_layer = (np.pi / 180.0) * self.ry_layers[layer_idx] # get into radians
            ry_rotation = np.array([[np.cos(ry_layer), 0, np.sin(ry_layer)],
                                  [0, 1, 0],
                                  [-np.sin(ry_layer), 0, np.cos(ry_layer)]])


            cube_rot_step = (2*np.pi)/float(self.ncube_layers[layer_idx])
            cube_rotation = 0

            for cube_idx in range(self.ncube_layers[layer_idx]):

                # starting position: 
                cube_pos = np.array(
                    [ radius * np.cos(cube_rotation),
                      y_distance,
                      radius * np.sin(cube_rotation)
                    ])
                
                # rotate by the layer's rotation offset
                cube_pos = np.dot(cube_pos, ry_rotation)
                # rotate by the clusters value
                cube_pos = np.dot(cube_pos, self.rotation)

                # And transform into global coordinates...
                cube_pos += self.translation

                # add to the output list (leds per cube is handled in the repeate parameter in the fixture file)
                self.cubes.append([cube_pos[0], cube_pos[1], cube_pos[2]])
                cube_rotation -= cube_rot_step 

    def write_fixture_file(self, config_folder):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        config_path = folder / filename
        tags = ["FRUIT"]
        tags.append(self.type)
        tags.append(self.piece_id)
        lx_output = {"label": self.piece_id,
                     "tags": tags,
                     "components": [ {"type": "points", "coords": []}],
                     "outputs": [],
                     "meta": {"name": self.piece_id,
                              "base_x": int(self.translation[0]),
                              "base_y": int(self.translation[1]),
                              "base_z": int(self.translation[2]),
                              "ry"   : self.ry
                     }}
        outputs = lx_output["outputs"]
        coords = lx_output["components"][0]["coords"]
        outputs.append({"protocol": "ddp", "host": self.ip_addr, "start": 0, "num": len(self.cubes), "repeat": self.ledsPerCube})
        for cube in self.cubes:
            coords.append({'x': cube[0], 'y': cube[1], 'z': cube[2]})

        with open(config_path, 'w+') as output_f:
            json.dump(lx_output, output_f, indent=4)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create chromatik fixtures from fruits configuration file')
    parser.add_argument('-c', '--config', type=str, required=True, help='Input fruit JSON configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Folder to store lx configurations')
    args = parser.parse_args()

    # Note that we could put different fruit configuration files into a single directory, and read the
    # directory. This might be an interesting way to assemble pieces... or not? Could effectively assemble
    # a scenegraph using a directory structure

    # Read configuration file. (They're json files)
    with open(args.config) as fc_f:
        fruits_config = json.load(fc_f)  # XXX catch exceptions here.

    for config in fruits_config:
        fruit = Fruit(config)
        fruit.write_fixture_file(args.fixtures_folder)

