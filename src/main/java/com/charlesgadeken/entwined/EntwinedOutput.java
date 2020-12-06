package com.charlesgadeken.entwined;

import com.charlesgadeken.entwined.model.Cube;
import com.charlesgadeken.entwined.model.Model;
import com.charlesgadeken.entwined.model.ShrubCube;
import heronarts.lx.LX;
import heronarts.lx.output.DDPDatagram;
import heronarts.lx.output.FadecandySocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class EntwinedOutput {
    private final LX lx;
    private final Model model;
    public final BasicParameterProxy brightness = new BasicParameterProxy(1);

    public EntwinedOutput(LX lx, Model model) {
        this.lx = lx;
        this.model = model;
    }

    private InetAddress requireResolves(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void configureExternalOutput() {
        // Output stage

        for (Map.Entry<String, Cube[]> entry : model.ipMap.entrySet()) {
            String ip = entry.getKey();
            Cube[] cubes = entry.getValue();
            DDPDatagram datagram = Output.clusterDatagram(lx, cubes);
            datagram.setAddress(requireResolves(ip));
            datagram.enabled.setValue(true);
            brightness.parameters.add(datagram.brightness);
            lx.addOutput(datagram);
        }

        for (Map.Entry<String, ShrubCube[]> entry : model.shrubIpMap.entrySet()) {
            String shrubIp = entry.getKey();
            ShrubCube[] shrubCubes = entry.getValue();
            DDPDatagram datagram = Output.shrubClusterDatagram(lx, shrubCubes);
            datagram.setAddress(requireResolves(shrubIp));
            datagram.enabled.setValue(true);
            brightness.parameters.add(datagram.brightness);
            lx.addOutput(datagram);
        }
    }

    /* configureFadeCandyOutput */

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

        // NOTE(meawoppl) So, this call `new FadecandySocket(lx)`
        // Makes the output stage feel responsible for all the points in
        // lx.getMode(). I suspect this is intended to be a subset of the model
        // that has numCubesInCluster * numClusters (16 * 48 == 768) points
        // If that subcomponent is passed to ala `new FadecandySocket(lx, subcomp)` it should work.
        FadecandySocket fadecandyOutput = new FadecandySocket(lx);
        fadecandyOutput.setAddress(requireResolves("127.0.0.1"));
        fadecandyOutput.setPort(7890);
        fadecandyOutput.updateIndexBuffer(pixelOrder);

        brightness.parameters.add(fadecandyOutput.brightness);
        lx.addOutput(fadecandyOutput);
    }
}
