#!/usr/bin/env python3

# Turn an Entwined fairy circle definition file into an LXstudio fixture file(s)

import argparse
import json
from pathlib import Path

import numpy as np


class FairyCircle:
    MINICLUSTER_RADIUS = 18.0

    # the heights are different
    MINICLUSTER_HEIGHTS = [
                             12.0, 14.0, 14.0, 16.0,
                             16.0, 18.0, 18.0, 20.0,
                             20.0, 18.0, 18.0, 16.0,
                             16.0, 14.0, 14.0, 12.0,
                             ];

    MINICLUSTER_N_CUBES = 12;

    # this routine is for arcs

    def circle_add_cubes(self, config):
        cluster_rotation = 0
        for _ in range(len(self.ip_addrs)):  # walking through the ndbs
            ndb_cubes = []
            for _ in range(self.clusters_per_ndb):
                cluster_cubes = []
                stem_rotation = 0
                stem_rot_step = (2*np.pi)/float(self.MINICLUSTER_N_CUBES)
                cluster_rot_matrix = np.array([[np.cos(cluster_rotation), 0, np.sin(cluster_rotation)],
                                               [0, 1, 0],
                                               [-np.sin(cluster_rotation), 0, np.cos(cluster_rotation)]])
                for idx in range(self.MINICLUSTER_N_CUBES):
                    # initial position, relative to the minicluster center...
                    cube_pos = np.array([self.MINICLUSTER_RADIUS * np.cos(stem_rotation),
                                         self.MINICLUSTER_HEIGHTS[idx],
                                         self.MINICLUSTER_RADIUS * np.sin(stem_rotation)])
                    # change to relative to the center of the fairy circle
                    cube_pos = np.dot(cube_pos, cluster_rot_matrix)
                    cube_pos += np.array([self.radius * np.cos(cluster_rotation),
                                         0,
                                         self.radius * np.sin(cluster_rotation)])
                    # change to global coordinates
                    cube_pos = np.dot(cube_pos, self.rotation)
                    cube_pos += self.translation

                    cluster_cubes.append(cube_pos)
                    stem_rotation -= stem_rot_step

                cluster_rotation += self.arc_step
                cluster_cubes.reverse()   # because apparently we wire these counterclockwise

                ndb_cubes.append(cluster_cubes)

        # populate cubes from ndb_cubes, with strange rules
        if (self.clusters_per_ndb == 5):
            self.cubes += ndb_cubes[1]
            self.cubes += ndb_cubes[0]
            self.cubes += ndb_cubes[2]
            self.cubes += ndb_cubes[3]
            self.cubes += ndb_cubes[4]
        elif (self.clusters_per_ndb == 3):
            self.cubes += ndb_cubes[0]
            self.cubes += ndb_cubes[1]
            self.cubes += ndb_cubes[2]
        else:
            println("only supports 3 and 5 clusters per ndb, not " + self.clusters_per_ndb)
            exit()

        return

    # this routine is for straight lines of babies / miniclusters
    # to use: tags: 'shape' is 'line'
    # 'separation' is between the babies/clusters
    # 'ry' is the rotation of the entire piece
    # line rotation: 0 degrees is where cluster 1 is aligned with positive X, then clockwise
    # X and Y position is the center of the line (where the NDB is)

    # ry is the rotation of each mini

    # can't really imagine there is more than one 

    def line_add_cubes(self, config):
        cluster_distance = - (self.distance * np.floor(self.clusters_per_ndb / 2))

        ndb_cubes = []
        for _ in range(self.clusters_per_ndb):
            # print(f'line: next cluster: distance {cluster_distance}')
            cluster_cubes = []
            stem_rotation = 0
            stem_rot_step = (2*np.pi)/float(self.MINICLUSTER_N_CUBES)

            for idx in range(self.MINICLUSTER_N_CUBES):
                # initial position, relative to the minicluster center...
                cube_pos = np.array([self.MINICLUSTER_RADIUS * np.cos(stem_rotation),
                                     self.MINICLUSTER_HEIGHTS[idx],
                                     self.MINICLUSTER_RADIUS * np.sin(stem_rotation)])
                # change to relative to the center of the line
                cube_pos += np.array([cluster_distance, 0, 0])
                # change to global coordinates
                cube_pos = np.dot(cube_pos, self.rotation)
                cube_pos += self.translation

                cluster_cubes.append(cube_pos)
                stem_rotation -= stem_rot_step

            cluster_cubes.reverse()   # because apparently we wire these counterclockwise

            ndb_cubes.append(cluster_cubes)

            cluster_distance += self.distance

        # populate cubes from ndb_cubes, with strange rules
        if (self.clusters_per_ndb == 5):
            self.cubes += ndb_cubes[1]
            self.cubes += ndb_cubes[0]
            self.cubes += ndb_cubes[2]
            self.cubes += ndb_cubes[3]
            self.cubes += ndb_cubes[4]
        elif (self.clusters_per_ndb == 3):
            self.cubes += ndb_cubes[0]
            self.cubes += ndb_cubes[1]
            self.cubes += ndb_cubes[2]
        else:
            println("only supports 3 and 5 clusters per ndb, not "+self.clusters_per_ndb)
            exit()

        return

# free is:
# no clusters per ndb, separation, but a list of 'clusters'
# each clusters has an x,y,z,ry offset
# one cluster has 'ndb': true which is where the ndb is located


    def free_add_cubes(self, config):
        clusters = config['clusters']
        if isinstance(clusters, list) is not True:
            println("Free position must have a list of clusters, fix it please")
            exit()

        ndb_cubes = []
        for cluster in clusters:
            # print(f'line: next cluster: distance {cluster_distance}')
            cluster_cubes = []
            stem_rotation = 0
            stem_rot_step = (2*np.pi)/float(self.MINICLUSTER_N_CUBES)

            for idx in range(self.MINICLUSTER_N_CUBES):
                # initial position, relative to the minicluster center...
                cube_pos = np.array([self.MINICLUSTER_RADIUS * np.cos(stem_rotation),
                                     self.MINICLUSTER_HEIGHTS[idx],
                                     self.MINICLUSTER_RADIUS * np.sin(stem_rotation)])
                # rotate around local center
                cube_pos += np.dot(cube_pos, cluster['ry'])
                # change to relative to the center of the line
                if 'y' not in cluster:
                    cluster['y'] = 0
                # apply local and global transform
                cube_pos += np.array([cluster['x'], cluster['y'], cluster['z']])

                cluster_cubes.append(cube_pos)
                stem_rotation -= stem_rot_step

            cluster_cubes.reverse()   # because apparently we wire these counterclockwise

            ndb_cubes.append(cluster_cubes)

        # add to cubes based on reorder based on NDB location
        # which has the ndb is denoted by "ndb": true in the json file
        # cubes are ordered by those to the left (lower output) and those to the right
        ndb_cluster = -1
        for idx, cluster in enumerate(clusters):
            if 'ndb' in cluster:
                ndb_cluster = idx
                break
        if ndb_cluster == -1:
            println("clusters array must have one ndb, please fix")
            exit()
        for idx in range(ndb_cluster-1, -1, -1):
            self.cubes += ndb_cubes[idx]
        self.cubes += ndb_cubes[ndb_cluster]
        for idx in range(ndb_cluster+1,len(clusters)):
            self.cubes += ndb_cubes[idx]

        return


    def __init__(self, config):
        rot = np.radians( config['ry'] ) # get into radians
        self.rotation = np.array([[np.cos(rot), 0, np.sin(rot)],
                                  [0, 1, 0],
                                  [-np.sin(rot), 0, np.cos(rot)]])
        self.translation = np.array([config['x'], 0, config['z']])

        self.ip_addrs = config['ipAddresses']
        self.piece_id = config['pieceId']
        self.ry = config['ry'] # degrees
        if 'clustersPerNdb' in config:
            self.clusters_per_ndb = config['clustersPerNdb']
        else:
            self.clusters_per_ndb = 5
        self.cubes = []

        # default is circle (it was first), includes arcs
        if 'shape' not in config:
            config['shape'] = 'circle'

        if config['shape'] == 'line':
            if 'separation' not in config:
                println("shape line must have separation, distance between babies in inches")
                exit()
            self.distance = config['separation']  # distance between babies

            if len(self.ip_addrs) != 1:
                println("lines must have only one NDB not " + len(self.ip_addrs))
                exit()          

            self.line_add_cubes(config)

        if (config['shape'] == 'circle') or (config['shape'] == 'arc'):
            if 'radius' not in config:
                println("shape circle must have radius, try again")
                exit()

            self.radius = config['radius']
            if 'degrees' in config:
                arc = (np.pi * float(config['degrees'])) / 180.0
            else:
                arc = 2.0 * np.pi
            self.arc_step = arc / (self.clusters_per_ndb*len(self.ip_addrs)) # the number of ndbs is the size of the ip_addr array

            self.circle_add_cubes(config)

        # free shape has an array of offsets from the center point.
        # that array is called 'locations' (maybe a better name)
        # There is no clusters per ndb because it is calculated
        # the location of the controlling NDB is in the positions ("ndb": true)

        if (config['shape'] == 'free'):
            if 'separation' in config:
                println('shape free must NOT separation please fix')
                exit()
            if 'clusters' not in config:
                println('shape free must have locations, please fix')
                exit()

            self.free_add_cubes(config)

        # print(f' processed shape, cubes is {self.cubes}')
        return

    def write_fixture_file(self, folder):
        folder_path = Path(folder)
        folder_path.mkdir(parents=True, exist_ok=True)
        filename = Path(self.piece_id + ".lxf")
        file_path = folder_path / filename
        lx_config = {'label': self.piece_id,
                     'tags': ['FAIRY_CIRCLE', self.piece_id],
                     'components': [{'type': 'points', 'coords': []}],
                     'outputs': [],
                     "meta": {"name": self.piece_id,
                              "base_x": int(self.translation[0]),
                              "base_y": int(self.translation[1]),
                              "base_z": int(self.translation[2]),
                              "ry": self.ry
                     }}
        coords = lx_config['components'][0]['coords']
        outputs = lx_config['outputs']
        for cube in self.cubes:
            # NB - the multiple leds in one cube issue does not occur with fairy circles...
            coords.append({'x': cube[0], 'y': cube[1], 'z': cube[2]})

        # breadcrumb: start is the start of the cube list. Which means if we have a
        # full fairy circle with multiple NDBs, we need the NDBs in the list to cover
        # the cubes.

        # there are two patterns. If there are multiple NDBs, then clusters_per_ndb is set.
        # if there is only one NDB, use the number of cubes.
        if (len(self.ip_addrs) == 1):
            outputs.append({'protocol': 'ddp',
                'host': self.ip_addrs[0],
                'start': 0,
                'num' : len(self.cubes)
                })
        else:
            num_cubes_per_ndb = self.MINICLUSTER_N_CUBES * self.clusters_per_ndb
            total_cubes = 0
            for ip_addr in self.ip_addrs:
                outputs.append({'protocol': 'ddp',
                          'host': ip_addr,
                          'start': total_cubes,
                          'num' : num_cubes_per_ndb
                         })
                total_cubes += num_cubes_per_ndb

        with open(file_path, 'w+') as output_f:
            json.dump(lx_config, output_f, indent=4)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Create newlx fixture configs from fairy circle  configuration file')
    parser.add_argument('-c', '--config', type=str, required=True, help='Input fairy circle JSON configuration file')
    parser.add_argument('-f', '--fixtures_folder', type=str, required=True, help='Folder to output lx configurations')
    args = parser.parse_args()

    # Read configuration file. (They're json files)
    with open(args.config) as sc_f:
        fc_configs = json.load(sc_f)  # XXX catch exceptions here.

    # Now let's create some circles and cubes ...
    for config in fc_configs:
        circle = FairyCircle(config)
        circle.write_fixture_file(args.fixtures_folder)
