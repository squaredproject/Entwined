#!/usr/bin/env pwsh

# no longer compile on every run
mvn package
mkdir -p ~/Chromatik/Packages -ea 0
rm -Force -ea 0 ~/Chromatik/Packages/entwined-0.0.1-SNAPSHOT.jar
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

# get the Fixtures and initial project with the installations scripts
mkdir -p ~/Chromatik/Fixtures/Entwined -ea 0
cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

java -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies-windows.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined ~/Chromatik/Projects/Entwined.lxp
