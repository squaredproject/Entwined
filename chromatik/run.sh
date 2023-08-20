#!/usr/bin/env bash

# no longer compile on every run
# mvn package

mkdir -p ~/Chromatik/Packages
rm -f ~/Chromatik/Packages/entwined-0.0.1-SNAPSHOT.jar
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

#fixtures are now copied using the installation folder
#mkdir -p ~/Chromatik/Fixtures/Entwined
#cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

if [[ $OSTYPE == 'darwin'* ]]; then
  RUNOPT="-XstartOnFirstThread"
  if [[ $(uname -m) == 'x86_64' ]]; then
    JARTYPE="-macos"
    ARCHTYPE="-x86_64"
  fi
fi

if [[ $OSTYPE == 'linux'* ]]; then
  JARTYPE="-linux"
  if [[ $(uname -m) == 'arm64' ]]; then
    ARCHTYPE="-aarch64"
  else
    ARCHTYPE="-amd64"
  fi
fi


CWD=`pwd`

echo $RUNOPT $JARTYPE $CWD

cd ~/Chromatik
java $RUNOPT -cp $CWD/lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE$ARCHTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined ~/Chromatik/Projects/entwined.lxp
