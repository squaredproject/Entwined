#!/usr/bin/env python3

# Take the json tree definition file, and a csv file describing the cube
# and ndb layouts of the trees, and create a lxstudio fixture configuration file

import argparse
import json
from pathlib import Path

import numpy as np

import sculpture_globals
from tree_csv import tree_cubes_load_csv

# csv file creates cube and ndb configuration structures -
# cube_config has the following fields for each cube
#   treeIndex           # which tree in tree configuration file
#   ipAddress           # ip addr of associated ndb
#   outputIndex         # Channel for associated ndb
#   layerIndex          # Which layer of branches? 0 is lowest
#   branchIndex         # Which branch is this? 0 is shortest, defines local X axis
#   mountPointIndex     # where the cube is on the branch, 0 outermost
#   cubeSizeIndex       # How many LEDs per cube
#   stringOffsetIndex   # offset in the string
# ndb_config ... Hmm. Maybe I don't need the ndb config, since I have the ipaddress


def cube_config_sort(cc1, cc2):
    # ipaddress is the first pass
    if cc1['ipAddress'] > cc2['ipAddress']:
        return True
    elif cc1['ipAddress'] < cc2['ipAddress']:
        return False

    # outputIndex (channel) is the second pass
    if cc1['outputIndex'] > cc2['outputIndex']:
        return True
    elif cc1['outputIndex'] < cc2['outputIndex']:
        return False

    # position in string is the third pass
    if cc1['stringOffsetIndex'] > cc2['stringOffsetIndex']:
        return True
    elif cc1['stringOffsetIndex'] < cc2['stringOffsetIndex']:
        return False

    print(f"Error - two cubes have same ndb, channel, and offset")

    return False


def get_cube_config_sort_key(cc):
    return cc['ipAddress'] + str(cc['outputIndex']).zfill(2) + str(cc['stringOffsetIndex']).zfill(3)


class Tree:
    rotational_positions = [[ 0, 1, 2, 3, 4, 5, 6, 7],
                            [ 0, 2, 4, 6],
                            [ 1, 3, 5, 7]]

    def __init__(self, tree_config, cubes_config):
        self.rotation = np.array([[np.cos(tree_config['ry']), 0, np.sin(tree_config['ry'])],
                                  [0, 1, 0],
                                  [-np.sin(tree_config['ry']), 0, np.cos(tree_config['ry'])]
                                ])
        self.translation = np.array([tree_config['x'], 0, tree_config['z']])
        self.cubes_config = cubes_config
        self.ip_addresses = self.get_ndb_addresses()
        self.piece_id = tree_config['pieceId']
        self.type = 'sapling' if self.piece_id.startswith('sapling') else 'classic'

        # set up branches and their mounting points...
        self.branches = []
        for layer_idx, canopyLength in enumerate(tree_config['canopyMajorLengths']):
            self.branches.append([])
            for rotational_position in self.rotational_positions[layer_idx]:
                branch = EntwinedBranch(canopyLength,
                         rotational_position,
                         tree_config['layerBaseHeights'][layer_idx])
                self.branches[layer_idx].append(branch)


    def  write_fixture_config(self, config_folder ):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        config_path = folder / filename
        tags = ["TREE"]
        print(f"Writing config for {self.piece_id}")
        if self.type == 'sapling':
            tags.append("SAPLING")
        else:
            tags.append("BIG_TREE")
        lx_output = {"label": self.piece_id,
                     "tags": tags,
                     "components": [ {"type": "points", "coords": []}],
                     "outputs": [],
                     "meta": {"name": self.piece_id,
                              "base_x": int(self.translation[0]),
                              "base_y": int(self.translation[1]),
                              "base_z": int(self.translation[2])
                     }}
        outputs = lx_output["outputs"]
        coords = lx_output["components"][0]["coords"]
        cur_ndb_addr = None
        total_pixels = 0
        ndb_pixel_start = 0
        for cube_config in self.cubes_config:
            branch = self.branches[cube_config['layerIndex']][cube_config['branchIndex']]
            cube_position = branch.mount_points[cube_config['mountPointIndex']]
            cube_position = np.dot(cube_position, self.rotation)
            cube_position += self.translation
            n_pixels = sculpture_globals.pixels_per_cube[cube_config['cubeSizeIndex']]
            n_pixels = 1
            for _ in range(n_pixels):
                coords.append({'x': cube_position[0], 'y': cube_position[1], 'z': cube_position[2]})
            if cube_config['ipAddress'] != cur_ndb_addr:
                if cur_ndb_addr is not None:
                    output = {'protocol': 'ddp',
                              'host': cur_ndb_addr,
                              'start': ndb_pixel_start,
                              'num': total_pixels-ndb_pixel_start,
                              'repeat': sculpture_globals.pixels_per_cube[cube_config['cubeSizeIndex']]}
                    outputs.append(output)
                cur_ndb_addr = cube_config['ipAddress']
                ndb_pixel_start = total_pixels
            total_pixels += n_pixels
        # let's add a cube at the origin...
        # coords.append({'x': int(self.translation[0]), 'y': 0, 'z': int(self.translation[2])})
        # total_pixels += 1
        # write final ndb information...
        output = {'protocol': 'ddp',
                   'host': cur_ndb_addr,
                   'start': ndb_pixel_start,
                   'num': total_pixels-ndb_pixel_start}
        outputs.append(output)

        with open(config_path, 'w+') as output_f:
            json.dump(lx_output, output_f)


    def get_ndb_addresses(self):
        # Note that the addresses will be in the correct order because the
        # cube_configuration is in the correct order
        ip_addresses = []
        for cube in self.cubes_config:
            if cube['ipAddress'] not in ip_addresses:
                ip_addresses.append(cube['ipAddress'])
        return ip_addresses


class EntwinedBranch:
    branch_length_ratios = [0.37, 0.41, 0.50, 0.56, 0.63]
    height_adjustment_factors = [1.0, 0.96, 0.92, 0.88, 0.85]

    def __init__(self, canopy_major_length, rotation_pos, layer_base_height):

        if rotation_pos < 4:
            rotation_idx = rotation_pos
        else:
            rotation_idx = (4 - rotation_pos % 4)
        branch_length = canopy_major_length * self.branch_length_ratios[rotation_idx]
        height_adjustment = self.height_adjustment_factors[rotation_idx]

        branch_rotation = rotation_pos * (np.pi/4)
        branch_matrix = np.array([[np.cos(branch_rotation), 0, np.sin(branch_rotation)],
                                  [0, 1, 0],
                                  [-np.sin(branch_rotation), 0, np.cos(branch_rotation)]])

        self.mount_points = []

        # In the coordinate system of the branch - x is along the branch (radially)
        x_key_points = [canopy_major_length/15,
                        branch_length * 0.315,
                        branch_length * 0.623,
                        branch_length * 0.917,
                        branch_length]
        # y is the height (as always)
        y_key_points = [(72 * 0.455 + 8) * height_adjustment,
                        (72 * 0.671 + 6) * height_adjustment,
                         72 * 0.793 * height_adjustment,
                         72 * 0.914 * height_adjustment,
                         72 * height_adjustment]
        # z is the distance from the center line of the branch. Notice that
        # the keypoints cross the midline as the branches entwine
        z_key_points = [branch_length * (-0.05),
                        branch_length * (-0.08),
                        0,
                        branch_length * 0.13,
                        branch_length * 0.199]

        # And now we find mounting points...
        # We walk backwards from the tip of the branch to find all mount points.
        # (We start at the tip because the installer always puts a cube at the tip.
        # There may or may not be enough cubes in the string to reach the trunk).
        # Mount points are located every 8 inches, and are calculated by interpolating
        # between the closest branch keypoints.
        # Since each branch has two entwined sub-branches, most points found with this
        # algorithm actually represent two physical mount points - one for the 'a' branch
        # and one for the 'b' branch.
        mount_pt_x = branch_length
        while mount_pt_x > 0:
            # Find closest keypoint that is >= to our test point
            key_point_idx = 0
            while x_key_points[key_point_idx] < mount_pt_x and key_point_idx <  5:  # ie, max keypoints
                key_point_idx += 1
            if key_point_idx < 5 and key_point_idx > 0:
                prev_keypoint_x = x_key_points[key_point_idx - 1]
                next_keypoint_x = x_key_points[key_point_idx]
                prev_keypoint_y = y_key_points[key_point_idx - 1]
                next_keypoint_y = y_key_points[key_point_idx]
                prev_keypoint_z = z_key_points[key_point_idx - 1]
                next_keypoint_z = z_key_points[key_point_idx]

                ratio = (mount_pt_x - prev_keypoint_x) / (next_keypoint_x - prev_keypoint_x)
                mount_pt_y = prev_keypoint_y + ratio * (next_keypoint_y - prev_keypoint_y)  + layer_base_height
                mount_pt_z = prev_keypoint_z + ratio * (next_keypoint_z - prev_keypoint_z)

                mount_point_a = np.array([mount_pt_x, mount_pt_y, mount_pt_z])
                mount_point_b = np.array([mount_pt_x, mount_pt_y, -mount_pt_z])

                mount_point_a = np.dot(mount_point_a, branch_matrix)
                mount_point_b = np.dot(mount_point_b, branch_matrix)

                self.mount_points.append(mount_point_a)
                self.mount_points.append(mount_point_b)
            mount_pt_x -= 8


def main():
    parser = argparse.ArgumentParser(description="Create LxStudio configuration file from tree and cube definition files")
    parser.add_argument('-t', '--tree_config', type=str, required=True, help='Input tree JSON configuration file')
    parser.add_argument('-b', '--branch_config', type=str, required=True, help='Input branch CSV configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Name of folder to hold lx configurations')

    args = parser.parse_args()

    # Load the base configuration data - end up with configuration
    # dicts for ndbs, trees, and cubes
    with open(args.tree_config) as tc_f:
        tree_configs = json.load(tc_f)
    ndb_configs, cube_configs = tree_cubes_load_csv(args.branch_config)

    # divide the cubes in the cube_configs into a list associated
    # with each tree
    num_trees = len(tree_configs)
    cubes_by_tree  = [[] for _ in range(num_trees)]
    for cube_config in cube_configs:
        cubes_by_tree[cube_config['treeIndex']].append(cube_config)

    # Create Tree object with cube_config information for this tree,
    # and then write the output fixture file for that tree
    for tree_idx, cube_tree_config in enumerate(cubes_by_tree):
        cube_tree_config.sort(key=get_cube_config_sort_key)
        tree = Tree(tree_configs[tree_idx], cube_tree_config)
        tree.write_fixture_config(args.fixtures_folder)

if __name__ == '__main__':
    main()

