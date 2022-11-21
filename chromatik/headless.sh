#!/bin/bash

if [[ $OSTYPE == 'darwin'* ]]; then
        RUNOPT="-XstartOnFirstThread"
fi

if [[ "$(expr substr $(uname -s) 1 5)" == "Linux" ]]; then
	JARTYPE="-linux"
fi

CWD=`pwd`

# note that since we are loading a plugin, we want to require a license, because we're expecting the plugin to work.
cd ~/Chromatik
java $RUNOPT -cp $CWD/lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --require-license --enable-plugin entwined.plugin.Entwined --disable-preferences --headless Projects/entwined.lxp
