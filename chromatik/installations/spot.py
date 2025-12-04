#!/usr/bin/env python3

# Define a set of spot lights used in an installation. Creates the world's simplest fixture:
# an x y z location, and an IP address.

# for initial simplicty, require each spot to be on a different IP address. We would want to change
# that eventually I suppose

import argparse
import json
from pathlib import Path

import numpy as np


class Spot:


# A spot is defined by:
#    {
#            "x" : -428,
#            "y" : 0,
#            "z" : 0,
#            "base_x": 
#            "base_y" :
#            "base_z" :
#            "pieceId" : "spot-4",
#            "ipAddress" : "10.0.0.226"
#    }
# note: rotation is meaningless for a point
# note: x,y,z are in the left handed coordinate system in inches like all other objects
# note: base_N is OPTIONAL. If left out, they will be set to x,y,z. The purpose is to support
# rotational patterns around other points (eg, twister around the center?)
# Eventually: include offset, it's actually easy to add in
# Eventually: support more than one LED per Cube
# 

    def __init__(self, config):
        self.translation = np.array([config['x'], 0, config['z']])

        self.ip_addr = config['ipAddress']
        self.piece_id = config['pieceId']
        self.x = config['x']
        self.y = config['y']
        self.z = config['z']

        if 'base_x' in config:
            self.base_x = config['base_x']
        else:
            self.base_x = config['x']

        if 'base_y' in config:
            self.base_y = config['base_y']
        else:
            self.base_y = config['y']

        if 'base_z' in config:
            self.base_z = config['base_z']
        else:
            self.base_z = config['z']


# this code is a little more complex than it needs to be, because 
# it's derived from code for trees and branches. Leaving it this little more
# complex so we can include the 'base' information if we want essentially.
    def write_fixture_file(self, folder):
        folder_path = Path(folder)
        folder_path.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        file_path = folder_path / filename
        lx_config = {'label': self.piece_id,
                     'tags': ['SPOT', self.piece_id],
                     'components': [{'type': 'points', 'coords': []}],
                     'outputs': [],
                     "meta": {"name": self.piece_id,
                              "base_x": int(self.base_x),
                              "base_y": int(self.base_y),
                              "base_z": int(self.base_z),
                              "ry": 0
                     }}
        coords = lx_config['components'][0]['coords']
        outputs = lx_config['outputs']

        # NB - the multiple leds in one cube issue does not occur with spots...
        coords.append({'x': self.x, 'y': self.y, 'z': self.z})

        outputs.append({'protocol': 'ddp',
                  'host': self.ip_addr,
                  'start': 0,
                  'num' : 1
                 })

        with open(file_path, 'w+') as output_f:
            json.dump(lx_config, output_f, indent=4)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create Chromatik fixture configs from spot configuration file')
    parser.add_argument('-c', '--config', type=str, required=True, help='Input spot JSON configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Folder to output lx configurations')
    args = parser.parse_args()

    # Read configuration file. (They're json files)
    with open(args.config) as sc_f:
        fc_configs = json.load(sc_f)  # XXX catch exceptions here.

    # Now let's create some circles and cubes ...
    for config in fc_configs:
        spot = Spot(config)
        spot.write_fixture_file(args.fixtures_folder)
