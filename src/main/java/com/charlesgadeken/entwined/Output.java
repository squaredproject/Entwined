package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.ShrubCube;
import heronarts.lx.LX;
import heronarts.lx.output.DDPDatagram;
import heronarts.lx.output.FadecandySocket;
import heronarts.lx.output.LXOutputGroup;

import java.net.InetAddress;
import java.util.Map;

public class Output {
    static DDPDatagram clusterDatagram(LX lx, Cube[] cubes) {
        int[] pointIndices;
        int pixelCount = 0;
        for (Cube cube : cubes) {
            pixelCount += cube.pixels;
        }
        pointIndices = new int[pixelCount];
        int pi = 0;
        for (Cube cube : cubes) {
            for (int i = 0; i < cube.pixels; ++i) {
                pointIndices[pi++] = cube.index;
            }
        }
        return new DDPDatagram(lx, pointIndices);
    }

    // If one wanted to, this could be simpler because shrubCube.pixels is always the same - 4 - so
    // you don't have to loop.
    // but it doesn't matter because we create datagrams very rarely
    static DDPDatagram shrubClusterDatagram(LX lx, ShrubCube[] shrubCubes) {
        int[] pointIndices;
        int pixelCount = 0;
        for (ShrubCube shrubCube : shrubCubes) {
            pixelCount += shrubCube.pixels;
        }
        pointIndices = new int[pixelCount];
        int pi = 0;
        for (ShrubCube shrubCube : shrubCubes) {
            for (int i = 0; i < shrubCube.pixels; ++i) {
                pointIndices[pi++] = shrubCube.index;
            }
        }
        return new DDPDatagram(lx, pointIndices);
    }

    static void configureExternalOutput(LX lx, Model model, BasicParameterProxy outputBrightness) {
        // Output stage
        try {
            LXOutputGroup output = new LXOutputGroup(lx);
            DDPDatagram[] datagrams = new DDPDatagram[model.ipMap.size()];
            int ci = 0;
            for (Map.Entry<String, Cube[]> entry : model.ipMap.entrySet()) {
                String ip = entry.getKey();
                Cube[] cubes = entry.getValue();
                DDPDatagram datagram = clusterDatagram(lx, cubes);
                datagram.setAddress(InetAddress.getByName(ip));
                datagrams[ci++] = datagram;
                output.addChild(datagram);
            }
            outputBrightness.parameters.add(output.brightness);
            output.enabled.setValue(true);
            lx.addOutput(output);
        } catch (Exception x) {
            System.out.println(x);
        }
        try {
            LXOutputGroup shrubOutput = new LXOutputGroup(lx);
            DDPDatagram[] shrubDatagrams = new DDPDatagram[model.shrubIpMap.size()];
            int ci = 0;
            for (Map.Entry<String, ShrubCube[]> entry : model.shrubIpMap.entrySet()) {
                String shrubIp = entry.getKey();
                ShrubCube[] shrubCubes = entry.getValue();
                DDPDatagram datagram = shrubClusterDatagram(lx, shrubCubes);
                datagram.setAddress(InetAddress.getByName(shrubIp));
                shrubDatagrams[ci++] = datagram;
                shrubOutput.addChild(datagram);
            }
            outputBrightness.parameters.add(shrubOutput.brightness);
            shrubOutput.enabled.setValue(true);
            lx.addOutput(shrubOutput);
        } catch (Exception x) {
            System.out.println(x);
        }
    }

    static void configureFadeCandyOutput(LX lx, BasicParameterProxy outputBrightness) {
        int[] clusterOrdering = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        int numCubesInCluster = clusterOrdering.length;
        int numClusters = 48;
        int[] pixelOrder = new int[numClusters * numCubesInCluster];
        for (int cluster = 0; cluster < numClusters; cluster++) {
            for (int cube = 0; cube < numCubesInCluster; cube++) {
                pixelOrder[cluster * numCubesInCluster + cube] =
                        cluster * numCubesInCluster + clusterOrdering[cube];
            }
        }
        try {
            FadecandySocket fadecandyOutput = new FadecandySocket(lx, pixelOrder);
            fadecandyOutput.setAddress(InetAddress.getByName("127.0.0.1"));
            fadecandyOutput.setPort(7890);

            outputBrightness.parameters.add(fadecandyOutput.brightness);
            lx.addOutput(fadecandyOutput);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
