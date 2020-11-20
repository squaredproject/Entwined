package com.charlesgadeken.entwined.model.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Canopy {
    public List<Branch> branches;

    public Canopy(int canopyMajorLength, int layerType, int layerBaseHeight) {
        List<Branch> _branches = new ArrayList<Branch>();
        int rotationalPositions[];
        switch (layerType) {
            case 0:
                rotationalPositions = new int[] {0, 1, 2, 3, 4, 5, 6, 7};
                break;
            case 1:
                rotationalPositions = new int[] {0, 2, 4, 6};
                break;
            case 2:
                rotationalPositions = new int[] {1, 3, 5, 7};
                break;
            default:
                rotationalPositions = new int[] {};
        }
        for (int i = 0; i < rotationalPositions.length; i++) {
            Branch b = new Branch(canopyMajorLength, rotationalPositions[i], layerBaseHeight);
            _branches.add(b);
        }
        this.branches = Collections.unmodifiableList(_branches);
    }
}
