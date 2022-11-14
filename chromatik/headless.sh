#!/bin/bash

if [[ $OSTYPE == 'darwin'* ]]; then
        RUNOPT="-XstartOnFirstThread"
fi

if [[ "$(expr substr $(uname -s) 1 5)" == "Linux" ]]; then
	JARTYPE="-linux"
fi

java $RUNOPT -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined --disable-prefences --headless /home/pi/Chromatik/Projects/entwined.lxp
