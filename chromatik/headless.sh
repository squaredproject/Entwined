#!/bin/bash

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

# note that since we are loading a plugin, we want to require a license, because we're expecting the plugin to work.
cd ~/Chromatik
java $RUNOPT -cp $CWD/lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE$ARCHTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --require-license --enable-plugin entwined.plugin.Entwined --disable-preferences --headless Projects/entwined.lxp
