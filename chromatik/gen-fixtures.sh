#!/bin/sh
rm -f src/main/resources/fixtures/*.lxf
python3 sculpture/fixtures/shrub.py sculpture/fixtures/examples/entwined_2021/entwinedShrubs.json src/main/resources/fixtures
python3 sculpture/fixtures/fairy_circle.py sculpture/fixtures/examples/entwined_2021/entwinedFairyCircles.json src/main/resources/fixtures
python3 sculpture/fixtures/trees.py sculpture/fixtures/examples/entwined_2021/entwinedTrees.json sculpture/fixtures/examples/entwined_2021/cubes.csv --folder src/main/resources/fixtures
rm -fr sculpture/fixtures/fixtures
