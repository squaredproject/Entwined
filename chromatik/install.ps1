
mvn clean
mvn validate
mvn package
mkdir -p ~/Chromatik/Packages -ea 0
cp target/entwined-0.0.1-SNAPSHOT.jar ~/Chromatik/Packages
mkdir -p ~/Chromatik/Fixtures/Entwined -ea 0
cp src/main/resources/fixtures/* ~/Chromatik/Fixtures/Entwined

