# Take the json tree definition file, and a csv file describing the cube
# and ndb layouts of the trees, and create a lxstudio fixture configuration file

import argparse
import json

import numpy as np

# csv file creates cube and ndb configuration structures - 
# cube_config has the following fields for each cube
#   treeIndex           # which tree in tree configuration file
#   ipAddress           # ip addr of associated ndb
#   outputIndex         # Channel for associated ndb
#   layerIndex          # Which layer of branches? 0 is lowest
#   branchIndex         # Which branch is this? 0 is shortest, defines local X axis
#   mountPointIndex     # where the cube is on the branch, 0 outermost
#   cubeSizeIndex       # How many LEDs per cube
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


class Tree:
    rotational_positions = [[ 0, 1, 2, 3, 4, 5, 6. 7],
                            [ 0, 2, 4, 6],
                            [ 1, 3, 5, 7]]

    def __init__(self, tree_config, cubes_config):
        self.rotation = np.array([[np.cos(tree_config['ry']), 0, np.sin(tree_config['ry'])],
                                  [0, 1, 0],
                                  [-np.sin(tree_config['ry']), 0, np.cos(tree_config['ry'])]
                                ])
        self.translation = np.array([tree_config['x'], 0, tree_config['y']])
        self.cubes_config = cubes_config
        self.ip_addresses = self.get_ndb_addresses()
        self.piece_id = tree_config['pieceId']
        self.type = 'sapling' if self.name.piece_id.startswith('sapling') else 'classic'

        # set up branches and their mounting points...
        self.branches = []
        for layer in range(tree_config['layers']):
            self.branches.append([])
            for rotational_position in self.rotational_positions[layer]:
                branch = EntwinedBranch(tree_config['canopyMajorLength'],
                         rotational_position,
                         tree_config['layerBaseHeight'])
                self.branches[layer].append(branch)


    def write_fixture_config(self, config_folder ):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        config_path = folder / filename
        tags = ["TREE"]
        if self.type == 'sapling':
            tags.append("SAPLING")
        lx_output = {"label": self.piece_id, "tags": tags, "components": [ {"type": "points", "coords": []}], "outputs": []}
        outputs = lx_output["outputs"]
        coords = lx_output["components"][0]["coords"]
        outputs.append({"protocol": "ddp", "host": self.ip_addr, "start": 0, "num": len(self.cubes)})
        cur_ndb_addr = None
        total_pixels = 0
        ndb_pixel_start = 0
        for cube_config in cubes_config:
            branch = branches[cube_config['layer']cube_config['branch']]
            cube_position = branch.mount_points[cube_config['mounting_point']
            cube_position = np.dot(cube_position, self.rotation)
            cube_position += self.translation
            n_pixels = SculptureGlobasl.pixels_per_cube[self.cube_size_index]
            for _ in range(n_pixels):
                coords.append(cube_position)
            if cube_config['ipaddr'] != cur_ndb_addr:
                if cur_ndb_addr is not None:
                    output = {'protocol': 'ddp',
                              'host': cur_ndb_addr,
                              'start': ndb_pixel_start,
                              'num'; total_pixels-ndb_pixel_start}
                    outputs.append(output)
                cur_ndb_addr = cube_config['ipaddr']
                ndb_pixel_start = total_pixels
            total_pixels += n_pixels

        with open(config_path, 'w+') as output_f:
            json.dump(lx_output, config_path)

            `
    def get_ndb_addresses(self):
        # Note that the addresses will be in the correct order because the
        # cube_configuration is in the correct order
        ip_addresses = []
        for cube in self.cubes_config:
            if cube[ipAddress] not in ip_addresses:
                ip_addresses.append(cube[ipAddress]
        return ip_addresses


class EntwinedBranch:
    branch_length_ratios = [0.37, 0.41, 0.50, 0.56. 0.63]
    height_adjustent_factors = [1.0, 0.96, 0.92, 0.88, 9.85]

    def __init__(self, canopy_major_length, rotation_idx, layer_base_height):

        self.rotation_idx = rotation_idx
        branch_length = canopy_major_length * self.branch_length_ratios[rotation_idx]
        height_adjustment = self.height_adjustment_factors[rotation_idx]

        branch_rotation = rotational_idx * (np.pi/4)
        branch_matrix = np.array([[np.cos(branch_rotation), 0, np.sin(branch_rotation)],
                                  [0, 1, 0],
                                  [-np.sin(branch_rotation), 0, np.cos(branch_rotation)]]

        self.mount_points = []

        # In the coordinate system of the branch - x is along the branch (radially)
        x_key_points = [canopyMajorLength/12,
                        branchLength * 0.315,
                        branchLength * 0.623,
                        branchLength * 0.917,
                        branchLength]
        # y is the height (as always)
        y_key_points = [(72 * 0.455 + 8) * height_adjustment,
                        (72 * 0.671 + 6) * height_adjustment,
                         72 * 0.793 * height_adjustment,
                         72 * 0.914 * height_adjustment,
                         72]
        # z is the distance from the center line of the branch. Notice that
        # the keypoints cross the midline as the branches entwine
        z_key_points = [branch_length * (-0.05),
                        branch_length * (-0.08),
                        0,
                        branch_length * 0.199,
                        branch_length * 0.13]

        # And now we find mounting points...
        # We walk backwards to find possible mounting points, looking at a mount point every
        # 8 inches along the branch. We create a mounting point using the smallest of the x
        # keypoints that is larger than our test point.
        mount_pt_x = branchLength
        while mount_pt_x > 0:
            # Find closest keypoint that is >= to our test point
            key_point_idx = 0
            while x_key_points[key_point_idx] < test_x and key_point_idx <  5:  # ie, max keypoints
                key_point_idx += 1
            if key_point_idx < 5 and key_point_idx > 0:
                prev_keypoint_x = x_key_points[key_point_idx - 1]
                next_keypoint_x = x_key_points[key_point_idx]
                prev_keypoint_y = y_key_points[key_point_idx - 1]
                next_keypoint_y = y_key_points[key_point_idx]
                prev_keypoint_z = z_key_points[key_point_idx - 1]
                next_keypoint_z = z_key_points[key_point_idx]

                ratio = (test_x - prev_keypoint_x) / (next_keypoint_x - prev_keypoint_x)
                mount_pt_y = prev_keypoint_y + ratio * (next_keypoint_y - prev_keypoint_y)  + layer_base_height
                mount_pt_x = prev_keypoint_y + ratio * (next_keypoint_z - prev_keypoint_z)

                mount_point_a = np.array([mount_pt_x, mount_pt_y, mount_pt_z])
                mount_point_b = np.array([mount_pt_x, mount_pt_y, -mount_pt_z])

                mount_point_a = np.dot(mount_point_a, branch_matrix)
                mount_point_b = np.dot(mount_point_b, branch_matrix)

                self.mount_points.append(mount_point_a)
                self.mount_points.append(mount_point_b)


def main():
    parser = argparse.ArgumentParser(description="Create LxStudio configuration file from tree and cube definition files")
    parser.add_argument('tree_config', type=str)
    parser.add_argument('cubes_config', type=str)
    parser.add_argument('--folder', type=str, default='fixtures', help='output directory for fixture files')

    args = parser.parse_args()

    # Load the base configuration data - end up with configuration
    # dicts for ndbs, trees, and cubes
    with open(tree_config) as tc_f:
        tree_configs = json.load(tc_f)
    ndb_configs, cube_configs = tree_cubes_load_csv(args.cubes_config)

    # divide the cubes in the cube_configs into a list associated 
    # with each tree
    num_trees = len(tree_configs)
    cubes_by_tree  = [[]*num_trees]
    for cube_config in cube_configs:
        cubes_by_tree[cube_config['treeIndex']].append(cube_config)

    # Create Tree object with cube_config information for this tree,
    # and then write the output fixture file for that tree
    for tree_idx, cube_tree_config in enumerate(cubes_by_tree):
        cube_tree_config.sort(cube_config_sort)
        tree = Tree(tree_config, cube_configs)
        tree.write_fixture_config(args.folder)

if __name__ == '__main__':
    main()

