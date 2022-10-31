#!/bin/sh
rm -f src/main/resources/fixtures/*.lxf
python3 ../newlx/sculpture/fixtures/shrub.py ../newlx/sculpture/fixtures/examples/entwined_2021/entwinedShrubs.json src/main/resources/fixtures
python3 ../newlx/sculpture/fixtures/fairy_circle.py ../newlx/sculpture/fixtures/examples/entwined_2021/entwinedFairyCircles.json src/main/resources/fixtures
python3 ../newlx/sculpture/fixtures/trees.py ../newlx/sculpture/fixtures/examples/entwined_2021/entwinedTrees.json ../newlx/sculpture/fixtures/examples/entwined_2021/cubes.csv --folder src/main/resources/fixtures
rm -fr ../newlx/sculpture/fixtures/fixtures
