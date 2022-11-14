#!/bin/sh
mvn clean
mvn validate
mvn package
mkdir -p ~/Chromatik/Packages
rm -f ~/Chromatik/Packages/entwined-0.0.1-SNAPSHOT.jar
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

#fixtures are now copied from the installations directory, not from entwined
#mkdir -p ~/Chromatik/Fixtures/Entwined
#cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

