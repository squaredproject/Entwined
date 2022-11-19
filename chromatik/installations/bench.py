#!/usr/bin/env python3

# Turn an Entwined bench definition file into an LXstudio fixture file(s)

import argparse
import json
from pathlib import Path

import numpy as np

import sculpture_globals

class Bench:
    cubes_pos = [
                {
                    "x": 87.194,
                    "y": 7.000,
                    "z": -7.082
                },
                {
                    "x": 86.284,
                    "y": 3.399,
                    "z": -0.110
                },
                {
                    "x": 83.879,
                    "y": 6.750,
                    "z": 6.320
                },
                {
                    "x": 81.252,
                    "y": 7.000,
                    "z": -7.318
                },
                {
                    "x": 77.310,
                    "y": 6.750,
                    "z": 6.906
                },

                {
                    "x": 76.554,
                    "y": 4.079,
                    "z": -0.076
                },
                {
                    "x": 70.896,
                    "y": 6.750,
                    "z": 8.216
                },
                {
                    "x": 72.570,
                    "y": 7.000,
                    "z": -7.424
                },
                {
                    "x": 68.237,
                    "y": 4.661,
                    "z": -1.155
                },
                {
                    "x": 65.393,
                    "y": 7.000,
                    "z": -8.249
                },
                {
                    "x": 61.036,
                    "y": 5.165,
                    "z": 1.984
                },
                {
                    "x": 55.343,
                    "y": 6.750,
                    "z": 7.019
                },
                {
                    "x": 55.898,
                    "y": 5.663,
                    "z": -1.635
                },
                {
                    "x": 47.909,
                    "y": 6.083,
                    "z": 0.093
                },
                {
                    "x": 44.051,
                    "y": 6.750,
                    "z": 7.814
                },
                {
                    "x": 39.995,
                    "y": 6.637,
                    "z": 2.587
                },
                {
                    "x": 34.614,
                    "y": 6.750,
                    "z": 8.949
                },
                {
                    "x": 33.122,
                    "y": 7.116,
                    "z": -1.562
                },
                {
                    "x": 26.407,
                    "y": 7.586,
                    "z": 1.583
                },
                {
                    "x": 20.409,
                    "y": 8.005,
                    "z": -2.048
                },
                {
                    "x": 18.973,
                    "y": 6.750,
                    "z": 8.569
                },
                {
                    "x": 14.028,
                    "y": 8.452,
                    "z": 2.411
                },
                {
                    "x": 10.320,
                    "y": 6.750,
                    "z": 10.231
                },
                {
                    "x": 8.330,
                    "y": 8.850,
                    "z": -2.163
                },
                {
                    "x": 3.867,
                    "y": 9.163,
                    "z": 2.857
                },
                {
                    "x": 11.434,
                    "y": 7.000,
                    "z": -9.661
                },
                {
                    "x": 3.981,
                    "y": 6.750,
                    "z": 10.492
                },
                {
                    "x": 4.558,
                    "y": 7.000,
                    "z": 10.822
                },
            ]

    def __init__(self, bench_config) :
        self.piece_id = bench_config['pieceId']
        self.ip_addr = bench_config['ipAddress']
        self.ry = bench_config['ry'] * np.pi/180
        self.rotation = np.array([
            [np.cos(bench_config['ry']),0,np.sin(bench_config['ry'])],
            [0,1,0],
            [-np.sin(bench_config['ry']), 0, np.cos(bench_config['ry'])]
        ])
        self.translation = np.array([bench_config['x'], 0, bench_config['z']])
        self.cubes = []

        self.calculate_cubes()


    def calculate_cubes(self):
      for cube in Bench.cubes_pos:
        cube_pos = np.array([cube["x"], cube["y"], cube["z"]])
        cube_pos = np.dot(self.rotation, cube_pos)
        cube_pos = cube_pos + self.translation
        self.cubes.append([cube_pos[0], cube_pos[1], cube_pos[2]])


    def write_fixture_file(self, config_folder):
        folder = Path(config_folder)
        folder.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        config_path = folder / filename
        tags = ["BENCH"]
        tags.append(self.piece_id)
        lx_output = {"label": self.piece_id,
                     "tags": tags,
                     "components": [ {"type": "points", "coords": []}],
                     "outputs": [],
                     "meta": {"name": self.piece_id,
                              "base_x": int(self.translation[0]),
                              "base_y": int(self.translation[1]),
                              "base_z": int(self.translation[2]),
                              "ry"   : self.ry * 180/np.pi
                     }}
        outputs = lx_output["outputs"]
        coords = lx_output["components"][0]["coords"]
        outputs.append({"protocol": "ddp", "host": self.ip_addr, "start": 0, "num": len(self.cubes)})
        for cube in self.cubes:
            coords.append({'x': cube[0], 'y': cube[1], 'z': cube[2]})

        with open(config_path, 'w+') as output_f:
            json.dump(lx_output, output_f, indent=4)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create newlx fixture configs from bench configuration file')
    parser.add_argument('-c', '--config', type=str, required=True, help='Input bench JSON configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Folder to store lx configurations')
    args = parser.parse_args()


    # Read configuration file. (They're json files)
    with open(args.config) as sc_f:
        bench_configs = json.load(sc_f)  # XXX catch exceptions here.

    for bench_config in bench_configs:
        bench = Bench(bench_config)
        bench.write_fixture_file(args.fixtures_folder)



