package com.charlesgadeken.entwined.config;

public class TreeCubeConfig {
    public int sculptureIndex;
    public int cubeSizeIndex;
    public int outputIndex;
    public String ipAddress;
    public TreeOrShrub treeOrShrub = TreeOrShrub.TREE;

    // For Tree
    public int treeIndex;
    public int layerIndex;
    public int branchIndex;
    public int mountPointIndex;
    public boolean isActive;

    // For Shrub
    public int shrubIndex;
    public int clusterIndex;
    public int rodIndex;
}
