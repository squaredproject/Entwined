# basic python for taking information from configs and outputting json
# This is code that knows nothing about LX Studio, thankfully

import argparse
import getopt
import json
import math

import numpy as np

class SculptureGlobals:
    pixels_per_cube = [1, 4, 6]


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
        self.type = shrub_config['type']
        self.rotation = np.array([
            [np.cos(shrub_config['ry']),0,np.sin(shrub_config['ry'])],
            [0,1,0],
            [-np.sin(shrub_config['ry']), 0, np.cos(shrub_config['ry'])]
        ])
        self.translation = np.array([shrub_config['x'], 0, shrub_config['z']])
        self.rods_per_cluster = 5
        self.clusters_per_shrub = 12
        self.cubes = []

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
                theta = -(cluster_idx + 1) * math.pi/6
                rot = np.array([
                    [np.cos(theta),0,np.sin(theta)],
                    [0,1,0],
                    [-np.sin(theta), 0, np.cos(theta)]])
                cube_pos = np.dot(rot, cube_pos)

                # And transform into global coordinates...
                cube_pos = np.dot(self.rotation, cube_pos)
                cube_pos = cube_pos + self.translation

                # and add to our list
                # If the cube size index shows that we have multiple leds in a cube,
                # add more.
                for _ in range(0, SculptureGlobals.pixels_per_cube[self.cube_size_index]):
                    self.cubes.append({'x':cube_pos[0], 'y':cube_pos[1], 'z':cube_pos[2]})


def create_lxstudio_config(shrubs):
    lx_output = {"components": [ {"type": "points", "coords": []}], "outputs": []}
    outputs = lx_output["outputs"]
    coords = lx_output["components"][0]["coords"]
    total_pix = 0
    for shrub in shrubs:
        outputs.append({"protocol": "ddp", "host": shrub.ip_addr, "start": total_pix, "num": len(shrub.cubes)})
        for cube in shrub.cubes:
            coords.append(cube)
        total_pix += len(shrub.cubes)

    return lx_output

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create newlx fixture config from shrub configuration file')
    parser.add_argument('shrub_config_file', help='Name of shrub configuration file')
    args = parser.parse_args()

    # Note that we could put different shrub configuration files into a single directory, and read the
    # directory. This might be an interesting way to assemble pieces... or not? Could effectively assemble
    # a scenegraph using a directory structure

    # Read configuration file. (They're json files)
    with open(args.shrub_config_file) as sc_f:
        shrub_configs = json.load(sc_f)  # XXX catch exceptions here.

    # Now let's create some shrubs and cubes ...
    shrubs = []

    for shrub_config in shrub_configs:
        shrubs.append(Shrub(shrub_config))  # this is going to set up the rods and the clusters


    # and I should at this point be able to spit out the final json
    lxstudio_config = create_lxstudio_config(shrubs)

    with open('lx_input.json', 'w+') as output_f:
        json.dump(lxstudio_config, output_f, indent=4)
