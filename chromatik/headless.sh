#!/bin/bash

if [[ $OSTYPE == 'darwin'* ]]; then
        RUNOPT="-XstartOnFirstThread"
fi

if [[ "$(expr substr $(uname -s) 1 5)" == "Linux" ]]; then
	JARTYPE="-linux"
fi

# there is a startup  problem with licenses so a small delay allows it to work at boot time - remove when fixed
sleep 10

java $RUNOPT -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined --disable-prefences --headless /home/pi/Chromatik/Projects/entwined.lxp
