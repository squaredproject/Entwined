#!/usr/bin/env pwsh

# no longer compile on every run
# mvn package

mkdir -p ~/Chromatik/Packages -ea 0
rm -Force -ea 0 ~/Chromatik/Packages/entwined-0.0.1-SNAPSHOT.jar
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

# get the Fixtures and initial project with the installations scripts
#mkdir -p ~/Chromatik/Fixtures/Entwined -ea 0
#cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

# why are we doing this? because we want to execute in a different working directory, and
# it appears the best way

$cwd=(Get-Location).tostring()

# this doesn't work
#Push-Location ~/Chromatik
#java -cp $cwd/lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies-windows.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin #entwined.plugin.Entwined entwined.lxp
#Pop-Location

# this works but not crazy about the output in a scrolling window
$c_args = "-cp $cwd/lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies-windows-amd64.jar heronarts.lx.studio.Chromatik --warnings --disable-zeroconf --enable-plugin entwined.plugin.Entwined Projects/entwined.lxp".Split(" ")
Start-Process -FilePath "java" -ArgumentList $c_args -WorkingDirectory "~/Chromatik" 

# this doesn't work, the directory ends up changed
# cd "~/Chromatik"
# Invoke-Expression "java $c_args"