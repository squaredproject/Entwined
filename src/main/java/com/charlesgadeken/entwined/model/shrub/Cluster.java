package com.charlesgadeken.entwined.model.shrub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cluster {
    public List<Rod> rods;

    public Cluster(int clusterIndex) {
        List<Rod> _rods = new ArrayList<Rod>();
        int rodPositions[] = new int[] {0, 1, 2, 3, 4};

        int clusterMaxRodLength;
        switch (clusterIndex) {
                // clockwise, starting at the longest left-most cluster

                // A -> 0, 1
                // B -> 2, 3, 10, 11
                // C -> 4, 5, 8, 9
                // D -> 6, 7
            case 0:
            case 1:
                clusterMaxRodLength = 54;
                break;
            case 2:
            case 3:
            case 10:
            case 11:
                clusterMaxRodLength = 50;
                break;
            case 4:
            case 5:
            case 8:
            case 9:
                clusterMaxRodLength = 46;
                break;
            case 6:
            case 7:
                clusterMaxRodLength = 42;
                break;
            default:
                clusterMaxRodLength = 0;
        }
        for (int i = 0; i < rodPositions.length; i++) {
            Rod p = new Rod(rodPositions[i], clusterMaxRodLength, clusterIndex);
            _rods.add(p);
        }
        this.rods = Collections.unmodifiableList(_rods);
    }
}
