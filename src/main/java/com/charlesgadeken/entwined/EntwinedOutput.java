package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.ShrubCube;
import heronarts.lx.LX;
import heronarts.lx.output.DDPDatagram;
import heronarts.lx.output.FadecandySocket;
import heronarts.lx.output.LXDatagram;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

public class EntwinedOutput {
    private final LX lx;
    private final Model model;

    List<LXDatagram> treeDatagrams;
    List<LXDatagram> shrubDatagrams;

    final BasicParameterProxy outputBrightness;

    public EntwinedOutput(LX lx, Model model, BasicParameterProxy outputBrightness) {
        this.lx = lx;
        this.model = model;
        this.outputBrightness = outputBrightness;
    }

    private InetAddress resolves(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void configureExternalOutput() {
        // Tree DDP
        treeDatagrams =
                model.ipMap.entrySet().stream()
                        .map(
                                (entry) -> {
                                    DDPDatagram treeDatagram =
                                            treeClusterDatagram(entry.getValue());
                                    treeDatagram.setAddress(resolves(entry.getKey()));
                                    treeDatagram.enabled.setValue(true);
                                    return treeDatagram;
                                })
                        .collect(Collectors.toList());

        treeDatagrams.forEach((d) -> outputBrightness.parameters.add(d.brightness));
        treeDatagrams.forEach(lx::addOutput);

        // Shrub DDP
        shrubDatagrams =
                model.shrubIpMap.entrySet().stream()
                        .map(
                                (entry) -> {
                                    DDPDatagram shrubDatagram =
                                            shrubClusterDatagram(entry.getValue());
                                    shrubDatagram.setAddress(resolves(entry.getKey()));
                                    shrubDatagram.enabled.setValue(true);
                                    return shrubDatagram;
                                })
                        .collect(Collectors.toList());

        shrubDatagrams.forEach((d) -> outputBrightness.parameters.add(d.brightness));
        shrubDatagrams.forEach(lx::addOutput);
    }

    public void configureFadeCandyOutput() {
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

        FadecandySocket fadecandy = new FadecandySocket(lx, pixelOrder);
        fadecandy.setAddress(resolves("127.0.0.1"));
        fadecandy.setPort(7890);
        lx.addOutput(fadecandy);
    }

    public DDPDatagram treeClusterDatagram(Cube[] cubes) {
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
    public DDPDatagram shrubClusterDatagram(ShrubCube[] shrubCubes) {
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
}
