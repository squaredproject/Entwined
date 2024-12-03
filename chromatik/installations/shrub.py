#!/usr/bin/env python3

# Turn an Entwined shrub definition file into an LXstudio fixture file(s)

import argparse
import json
from pathlib import Path

import numpy as np

import sculpture_globals


class Shrub:

    # Shrubs are composed of lights in clusters, which are themselves composed
    # of stems (ie, rods). The rod length is used as proxy for how high the light
    # is from the ground.

    # King shrubs are somewhat taller than normal shrubs, but about the same
    # footprint on the ground

    rod_lengths = [[31, 36.5, 40, 46, 51],
                  [31, 36.5, 40, 46, 51],
                  [28, 33, 36.5, 41, 46],
                  [28, 33, 36.5, 41, 46],
                  [24, 29, 33, 37.5, 43],
                  [24, 29, 33, 37.5, 43],
                  [24, 29, 33, 37.5, 43],
                  [24, 29, 33, 37.5, 43],
                  [24, 29, 33, 37.5, 43],
                  [24, 29, 33, 37.5, 43],
                  [28, 33, 36.5, 41, 46],
                  [28, 33, 36.5, 41, 46]]

    # NB - King rod lengths are usually considered to be twice the size
    # of normal rod lengths... except for the shortest ones.
    # (What they actually are, I don't know. I beleive this was just
    # a reasonably accurate and easy guess.)
    king_rod_lengths = [
                  [31*2, 36.5*2, 40*2, 46*2, 51*2],
                  [31*2, 36.5*2, 40*2, 46*2, 51*2],
                  [28*2, 33*2, 36.5*2, 41*2, 46*2],
                  [28*2, 33*2, 36.5*2, 41*2, 46*2],
                  [24*2, 29*2, 33*2, 37.5*2, 43*2],
                  [24*2, 29*2, 33*2, 37.5*2, 43*2],
                  [21*2, 26*2, 30*2, 36*2, 40.5*2],
                  [21*2, 26*2, 30*2, 36*2, 40.5*2],
                  [24*2, 29*2, 33*2, 37.5*2, 43*2],
                  [24*2, 29*2, 33*2, 37.5*2, 43*2],
                  [28*2, 33*2, 36.5*2, 41*2, 46*2],
                  [28*2, 33*2, 36.5*2, 41*2, 46*2],
            ]

    # NB - The initial x,y,z coordinate system for a rod point y up, z along the
    # vector from the center of the shrub to the cluster, and x perpendicular to that vectors
    # The height of the rod (y) is approximately the same as the radial distance to
    # the rod (z), multiplied by a factor depending on exactly which rod in the cluster
    # we're using.
    # cube_z_multiplier = [1.25, 1.2, 1, 0.9, 0.7]
    cube_z_multiplier = [0.7, 0.9, 1, 1.2, 1.25]

    # all of the cubes in cluster have close to the same distance from the vector 
    # between the shrub center and the cluster. The units here are inches.
    cube_x_points = [0, -2, 2, -2, 2]

    def __init__(self, shrub_config) :
        # print(f"Shrub config is {shrub_config}")
        self.piece_id = shrub_config['pieceId']
        self.ip_addr = shrub_config['shrubIpAddress']
        self.cube_size_index = shrub_config['cubeSizeIndex']
        self.clockwise = False
        if 'direction' in shrub_config:
            if shrub_config['direction'].startswith('counter'):
                self.clockwise = True
        if 'type' in shrub_config:
            self.type = shrub_config['type']
        else:
            self.type = 'standard'
        rot = (np.pi / 180.0) * shrub_config['ry'] # get into radians
        self.rotation = np.array([
            [np.cos(rot),0,np.sin(rot)],
            [0,1,0],
            [-np.sin(rot), 0, np.cos(rot)]
        ])
        self.translation = np.array([shrub_config['x'], 0, shrub_config['z']])
        self.rods_per_cluster = 5
        self.clusters_per_shrub = 12
        self.cubes = []

        self.ry = shrub_config['ry']

        self.calculate_cubes()



    def calculate_cubes(self):
        # A shrub appears to have 12 clusters, and each cluster has 5 rods.
        # The cubes appear to be output in rod order
        for rod_idx in range(0, self.rods_per_cluster):
            for cluster_idx in range(0, self.clusters_per_shrub):
                if self.type == "king":  # oh, for subclassing!
                    rod_length = self.king_rod_lengths[cluster_idx][rod_idx]
                    min_rod_length = self.king_rod_lengths[cluster_idx][0]
                else:
                    rod_length = self.rod_lengths[cluster_idx][rod_idx]
                    min_rod_length = self.rod_lengths[cluster_idx][0]

                cube_pos = np.array(
                    [ self.cube_x_points[rod_idx],
                      rod_length,
                      min_rod_length * self.cube_z_multiplier[rod_idx]
                    ])

                # Now transform into shrub coordinates...
                theta = (cluster_idx + 1) * np.pi/6
                if self.clockwise:
                    theta = - theta
                rot = np.array([
                    [np.cos(theta),0,np.sin(theta)],
                    [0,1,0],
                    [-np.sin(theta), 0, np.cos(theta)]])
                cube_pos = np.dot(rot, cube_pos)

                # And transform into global coordinates...
                cube_pos = np.dot(self.rotation, cube_pos)
                cube_pos = cube_pos + self.translation

                # and add to our list
                self.cubes.append([cube_pos[0], cube_pos[1], cube_pos[2]])


    def write_fixture_file(self, config_folder):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        config_path = folder / filename
        tags = ["SHRUB"]
        if self.type == 'king':
            tags.append("KING")
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
        outputs.append({"protocol": "ddp", "host": self.ip_addr, "start": 0, "num": len(self.cubes), "repeat": sculpture_globals.pixels_per_cube[self.cube_size_index]})
        for cube in self.cubes:
            coords.append({'x': cube[0], 'y': cube[1], 'z': cube[2]})

        with open(config_path, 'w+') as output_f:
            json.dump(lx_output, output_f, indent=4)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create newlx fixture configs from shrub configuration file')
    parser.add_argument('-c', '--config', type=str, required=True, help='Input shrub JSON configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Folder to store lx configurations')
    args = parser.parse_args()

    # Note that we could put different shrub configuration files into a single directory, and read the
    # directory. This might be an interesting way to assemble pieces... or not? Could effectively assemble
    # a scenegraph using a directory structure

    # Read configuration file. (They're json files)
    with open(args.config) as sc_f:
        shrub_configs = json.load(sc_f)  # XXX catch exceptions here.

    for shrub_config in shrub_configs:
        shrub = Shrub(shrub_config)
        shrub.write_fixture_file(args.fixtures_folder)
        #shrubs.append(Shrub(shrub_config))  # this is going to set up the rods and the clusters

