#!/usr/bin/env python3

# Take the json tree definition file, and a csv file describing the cube
# and ndb layouts of the trees, and create a lxstudio fixture configuration file

import argparse
import json
from collections import defaultdict
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
#   ndbOffset           # offset into ndb frame
# ndb_config ... Hmm. Maybe I don't need the ndb config, since I have the ipaddress


def get_cube_config_ndb_sort_key(cc):
    return cc['ipAddress'] + str(cc['outputIndex']).zfill(2) + str(cc['stringOffsetIndex']).zfill(3)

def get_cube_config_layer_sort_key(cc):
    return str(cc['layerIndex']) + cc['ipAddress'] + str(cc['ndbOffset']).zfill(5)


def index_ndb_outputs(cube_tree_config):
    cube_tree_config.sort(sortkey = get_cube_config_ndb_sort_key)
    cur_ndb_addr = None
    cur_ndb_offset = 0
    for cc in cube_tree_config:
        if cc['ipAddress'] != cur_ndb_addr:
            cur_ndb_offset = 0
            cur_ndb_addr = cc['ipAddress']
        cc['ndbOffset'] = cur_ndb_offset
        cur_ndb_offset += 3 * sculpture_globals.pixels_per_cube[cc['cubeSizeIndex']]

class Tree:
    rotational_positions = [[ 0, 1, 2, 3, 4, 5, 6, 7],
                            [ 0, 2, 4, 6],
                            [ 1, 3, 5, 7]]

    def __init__(self, tree_config, cubes_config):
        rot = (np.pi / 180.0) * tree_config['ry'] # get into radians
        self.rotation = np.array([[np.cos(rot), 0, np.sin(rot)],
                                  [0, 1, 0],
                                  [-np.sin(rot), 0, np.cos(rot)]
                                ])
        self.translation = np.array([tree_config['x'], 0, tree_config['z']])
        self.cubes_config = cubes_config
        self.piece_id = tree_config['pieceId']
        self.type = 'sapling' if self.piece_id.startswith('sapling') else 'classic'
        self.ry = tree_config['ry']
        self.is_center = False
        if 'center' in tree_config:
            self.is_center = tree_config['center']
        self.metadata = {"name": self.piece_id,
                         "base_x": int(self.translation[0]),
                         "base_y": int(self.translation[1]),
                         "base_z": int(self.translation[2]),
                         "ry": self.ry
                        }
        self.tags = ["TREE"]
        if self.type == 'sapling':
            self.tags.append("SAPLING")
        else:
            self.tags.append("BIG_TREE")
        if self.is_center:
            self.tags.append("CENTER")
        self.tags.append(self.piece_id)

        # For the moment we do not allow different size cubes
        if ( len(cubes_config) == 0):
            print(' no cubes in tree: csv out of sync with trees, piece {} '.format(self.piece_id))
            exit()
        self.repeat_count = sculpture_globals.pixels_per_cube[self.cubes_config[0]['cubeSizeIndex']]  

        # set up branches and their mounting points...
        self.branches = []
        for layer_idx, canopyLength in enumerate(tree_config['canopyMajorLengths']):
            self.branches.append([])
            for rotational_position in self.rotational_positions[layer_idx]:
                branch = EntwinedBranch(canopyLength,
                         rotational_position,
                         tree_config['layerBaseHeights'][layer_idx])
                self.branches[layer_idx].append(branch)

        # Add information about position and ndb offset to the cubes, put them in layer
        # format  XXX - I may not need or want to do this with saplings...
        self._pre_process_cubes()


    # So I think I need to take a two pass approach here. In the first pass, I order
    # the pixels by ndb, channel, etc (as previous). I also create a output index for each
    # of the pixels.
    # Next, I recorder the list so that it's broken into layers. I go through the layers looking
    # at each of the pixels, and adding a new output section if
    #  a) the ndb has changed
    #  b) the pixel index within the ndb is not sequential

    def  write_fixture_config(self, config_folder):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        parser = PixelParser(folder, self.piece_id, self.tags, self.metadata, self.repeat_count)

        for cube_config in self.cubes_config:
            parser.add_cube(cube_config)
        parser.finish()


    def _pre_process_cubes(self):
        # We start out with a cube array indexed by ndb, without position information.
        # We modify this to add position information and the cube's offset in the ndb frame.
        # Then we resort so that the cubes are in layer order.
        ndb_offset = None
        cur_ndb = None
        for cube_config in self.cubes_config:
            # Add position information
            branch = self.branches[cube_config['layerIndex']][cube_config['branchIndex']]
            cube_position = branch.mount_points[cube_config['mountPointIndex']]
            cube_position = np.dot(cube_position, self.rotation)
            cube_position += self.translation
            cube_config["x"] = cube_position[0]
            cube_config["y"] = cube_position[1]
            cube_config["z"] = cube_position[2]

            # add ndb offset
            if cube_config["ipAddress"] != cur_ndb:
                cur_ndb = cube_config["ipAddress"]
                ndb_offset = 0
            cube_config["ndbOffset"] = ndb_offset
            ndb_offset += 3 * sculpture_globals.pixels_per_cube[cube_config['cubeSizeIndex']]

        # Sort by layer
        self.cubes_config.sort(key=get_cube_config_layer_sort_key)


class PixelParser:
# possible version issue with python here?
#    def __init__(self, output_folder: Path, piece_id: str, tags: [str], metadata: [dict], repeat_count: int):
    def __init__(self, output_folder, piece_id: str, tags, metadata, repeat_count: int):

        # statics
        self.piece_id = piece_id
        self.output_folder = output_folder
        self.tags = tags
        self.metadata = metadata
        self.repeat_count = repeat_count

        # internal state
        self.reset_layer()
        self.offsets = defaultdict(lambda : 0)
        self.components = []


    def add_cube(self, cube_config: dict):
        if cube_config["layerIndex"] != self.cur_layer_idx:
            self.open_layer(cube_config)
        elif cube_config["ipAddress"] != self.cur_ndb_addr:
            self.open_output(cube_config)
        elif cube_config["ndbOffset"] != self.cur_ndb_offset:
            self.open_output(cube_config)
        self.incr_cube()
        self.coords.append({"x": cube_config["x"], "y": cube_config["y"], "z": cube_config["z"]})


    def reset_layer(self):
        self.cur_layer_idx  = None
        self.cur_ndb_addr   = None
        self.cur_ndb_offset = 0  # index of current pixel in ndb frame
        self.cur_cube_idx   = 0  # index of current pixel in full frame buffer
        self.outputs = []           # output sections
        self.output_start_idx = 0 # index of first pixel in output section
        self.output = None           # current output section
        self.coords = []            # full frame buffer


    def create_layer_data(self, layer_name: str) -> dict:
        layer_data = {"label": layer_name,
                      "tags": ["LAYER_" + str(self.cur_layer_idx)],
                      "components": [ {"type": "points", "coords": self.coords}],
                      "outputs": self.outputs,
                      "repeat": self.repeat_count
                     }
        return layer_data


    def create_container_output(self) -> dict:
        return {"label": self.piece_id,
                "tags": self.tags,
                "meta" : self.metadata,
                "components": self.components
               }


    def incr_cube(self):
        self.cur_cube_idx += 1
        self.cur_ndb_offset += 3 * self.repeat_count


    def open_layer(self, cube_config: dict):
        self.close_layer()
        self.cur_layer_idx = cube_config["layerIndex"]
        self.cur_cube_idx = 0
        self.open_output(cube_config)
        # set up layer output - stuff that we write to file


    def close_layer(self):
        if self.cur_layer_idx == None:
            return
        self.close_output()
        layer_name = self.piece_id + "_layer_" + str(self.cur_layer_idx)
        layer_file_name = layer_name + ".lxf"
        layer_data = self.create_layer_data(layer_name)
        layer_data["outputs"] = self.outputs
        with open(self.output_folder / Path(layer_file_name), "w+") as output_f:
            json.dump(layer_data, output_f, indent=4)

        self.reset_layer()

        self.components.append({"type": layer_name})


    def close_output(self):
        n_cubes_in_output = self.cur_cube_idx - self.output_start_idx
        self.output["num"] = n_cubes_in_output
        self.outputs.append(self.output)
        self.offsets[self.cur_ndb_addr] = self.cur_ndb_offset + n_cubes_in_output


    def open_output(self, cube_config: dict):
        if self.output is not None:
            self.close_output()
        self.output =  {'protocol' : 'ddp',
                        'host': cube_config["ipAddress"],
                        'start': self.cur_cube_idx,
                        'repeat': self.repeat_count,
                        'num': -1,
                        'dataOffset': cube_config["ndbOffset"]
                        }
        self.output_start_idx = self.cur_cube_idx
        self.cur_ndb_addr = cube_config["ipAddress"]
        self.cur_ndb_offset = cube_config["ndbOffset"]


    def finish(self):
        self.close_layer()
        output = self.create_container_output()
        with open(self.output_folder/ Path(self.piece_id+ ".lxf"), "w+") as output_f:
            json.dump(output, output_f, indent=4)



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
    if not tree_configs:
        print(" Empty tree configurations, exiting tree.py ")
        exit()

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
        cube_tree_config.sort(key=get_cube_config_ndb_sort_key)
        tree = Tree(tree_configs[tree_idx], cube_tree_config)
        tree.write_fixture_config(args.fixtures_folder)

if __name__ == '__main__':
    main()

