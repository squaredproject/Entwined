import heronarts.lx.output.DDPDatagram;

class Output {
  static DDPDatagram clusterDatagram(Cube[] cubes) {
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
    return new DDPDatagram(pointIndices);
  }
  
  // If one wanted to, this could be simpler because shrubCube.pixels is always the same - 4 - so you don't have to loop.
  // but it doesn't matter because we create datagrams very rarely
  static DDPDatagram shrubClusterDatagram(ShrubCube[] shrubCubes) {
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
      return new DDPDatagram(pointIndices);
    }
}
