#!/bin/sh
mvn package
mkdir -p ~/Chromatik/Packages
cp target/entwined-0.0.1-SNAPSHOT.jar ~/Chromatik/Packages
mkdir -p ~/Chromatik/Fixtures/Entwined
cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined
java -XstartOnFirstThread -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined src/main/resources/projects/Entwined-2022.lxp
