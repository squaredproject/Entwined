#!/usr/bin/env pwsh

mvn clean
mvn validate
mvn package
mkdir -p ~/Chromatik/Packages -ea 0
# note: other versions of Chromatik can leave other files which also need cleaning
rm -Erroraction 'silentlycontinue' -Force "~/Chromatik/Packages/entwined-*"
cp target/entwined-0.0.1-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

# mkdir -p ~/Chromatik/Fixtures/Entwined -ea 0
# cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

