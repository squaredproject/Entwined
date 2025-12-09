#!/bin/sh

mvn clean
mvn validate
mvn package
mkdir -p ~/Chromatik/Packages
# warning other builds of chromatik like 1.0 can leave other versions which are very harmful
rm -f "~/Chromatik/Packages/entwined-*.jar"
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

#fixtures are now copied from the installations directory, not from entwined
# mkdir -p ~/Chromatik/Fixtures/Entwined
# cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

