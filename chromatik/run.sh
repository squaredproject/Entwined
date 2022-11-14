#!/usr/bin/env bash
# mvn package
mkdir -p ~/Chromatik/Packages
rm -f ~/Chromatik/Packages/entwined-0.0.1-SNAPSHOT.jar
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages
mkdir -p ~/Chromatik/Fixtures/Entwined
cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

if [[ $OSTYPE == 'darwin'* ]]; then
  RUNOPT="-XstartOnFirstThread"
fi

if [[ $OSTYPE == 'linux'* ]]; then
  JARTYPE="-linux"
fi

echo $RUNOPT $JARTYPE
java $RUNOPT -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined src/main/resources/projects/Entwined-2022.lxp
