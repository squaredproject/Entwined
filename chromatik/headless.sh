#!/bin/bash

if [[ $OSTYPE == 'darwin'* ]]; then
        RUNOPT="-XstartOnFirstThread"
fi

if [[ "$(expr substr $(uname -s) 1 5)" == "Linux" ]]; then
	JARTYPE="-linux"
fi

# there is a startup  problem with licenses so a small delay allows it to work at boot time - remove when fixed
sleep 10

# note that since we are loading a plugin, we want to require a license, because we're expecting the plugin to work.
java $RUNOPT -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies$JARTYPE.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --require-license --enable-plugin entwined.plugin.Entwined --disable-preferences --headless /home/pi/Chromatik/Projects/entwined.lxp
