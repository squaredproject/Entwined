import heronarts.lx.output.DDPDatagram;

// everything has been refactored into base cube, so I think one can
// use the BaseCube function everywhere here

class Output {
  static DDPDatagram treeDatagram(Cube[] cubes) {
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
  static DDPDatagram shrubDatagram(ShrubCube[] shrubCubes) {
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

  // This is a bit interesting because the cubes at this point have to be in NDB order
  static DDPDatagram baseCubeDatagram(BaseCube[] baseCubes) {
      int[] pointIndices;
      int pixelCount = 0;
      for (BaseCube baseCube : baseCubes) {
        pixelCount += baseCube.pixels;
      }
      pointIndices = new int[pixelCount];
      int pi = 0;
      for (BaseCube baseCube : baseCubes) {
        for (int i = 0; i < baseCube.pixels; ++i) {
          pointIndices[pi++] = baseCube.index;
        }
      }
      return new DDPDatagram(pointIndices);
    }
}
