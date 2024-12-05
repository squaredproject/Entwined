package com.charlesgadeken.entwined.config;

public class ShrubCubeConfig {
    // each shrubIndex maps to an ipAddress, consider pushing ipAddress up to
    public int shrubIndex;
    // ShrubConfig
    public int clusterIndex;
    public int rodIndex;
    public TreeOrShrub treeOrShrub = TreeOrShrub.SHRUB;
    public int shrubOutputIndex;
    public int cubeSizeIndex;
    public String shrubIpAddress;
}
