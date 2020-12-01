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

public class EntwinedOutput {
    private final LX lx;
    private final Model model;
    public final BasicParameterProxy brightness = new BasicParameterProxy(1);

    public EntwinedOutput(LX lx, Model model) {
        this.lx = lx;
        this.model = model;
    }

    public void configureExternalOutput() {
        // Output stage
        try {
            LXOutputGroup output = new LXOutputGroup(lx);
            DDPDatagram[] datagrams = new DDPDatagram[model.ipMap.size()];
            int ci = 0;
            for (Map.Entry<String, Cube[]> entry : model.ipMap.entrySet()) {
                String ip = entry.getKey();
                Cube[] cubes = entry.getValue();
                DDPDatagram datagram = Output.clusterDatagram(lx, cubes);
                datagram.setAddress(InetAddress.getByName(ip));
                datagrams[ci++] = datagram;
                output.addChild(datagrams[ci]);
            }
            brightness.parameters.add(output.brightness);
            output.enabled.setValue(true);
            lx.addOutput(output);
        } catch (Exception x) {
            System.out.println("Can not setup Cubes DDP");
            System.out.println(x);
        }

        try {
            LXOutputGroup shrubOutput = new LXOutputGroup(lx);
            DDPDatagram[] shrubDatagrams = new DDPDatagram[model.shrubIpMap.size()];
            int ci = 0;
            for (Map.Entry<String, ShrubCube[]> entry : model.shrubIpMap.entrySet()) {
                String shrubIp = entry.getKey();
                ShrubCube[] shrubCubes = entry.getValue();
                DDPDatagram datagram = Output.shrubClusterDatagram(lx, shrubCubes);
                datagram.setAddress(InetAddress.getByName(shrubIp));
                shrubDatagrams[ci++] = datagram;
                shrubOutput.addChild(shrubDatagrams[ci]);
            }
            brightness.parameters.add(shrubOutput.brightness);
            shrubOutput.enabled.setValue(true);
            lx.addOutput(shrubOutput);
        } catch (Exception x) {
            System.out.println("Can not setup Shrubs DDP");
            System.out.println(x);
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
        try {
            FadecandySocket fadecandyOutput = new FadecandySocket(lx);
            fadecandyOutput.setAddress(InetAddress.getByName("127.0.0.1"));
            fadecandyOutput.setPort(7890);
            fadecandyOutput.updateIndexBuffer(pixelOrder);

            brightness.parameters.add(fadecandyOutput.brightness);
            lx.addOutput(fadecandyOutput);
        } catch (Exception e) {
            System.out.println("Can not setup fadecandy output :(");
            System.out.println(e);
        }
    }
}
