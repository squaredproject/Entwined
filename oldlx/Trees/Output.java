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
}
