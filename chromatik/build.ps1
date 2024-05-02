#!/usr/bin/env pwsh

mvn clean
mvn validate
mvn package
mkdir -p ~/Chromatik/Packages -ea 0
rm -Erroraction 'silentlycontinue' -Force ~/Chromatik/Packages/entwined-1.0.0-SNAPSHOT.jar
cp target/entwined-1.0.0-SNAPSHOT-jar-with-dependencies.jar ~/Chromatik/Packages

# mkdir -p ~/Chromatik/Fixtures/Entwined -ea 0
# cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

