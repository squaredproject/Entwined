#!/bin/sh
mvn clean
mvn validate
mvn package
cp target/entwined-0.0.1-SNAPSHOT.jar ~/Chromatik/Packages

