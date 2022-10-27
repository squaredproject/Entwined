# Entwined for Chromatik

This folder contains bootstrapping to build the Entwined content as a library for LX's new Chromatik runtime. The Entwined content is simply built into a JAR container file which is loaded dynamically by Chromatik, without needing to rebuild and link the entire LX project.

## Running Chromatik

To run Chromatik, ensure that you have Java 17 Temurin installed, available at https://adoptium.net/

The `run.sh` script will start Chromatik. Make sure to do this once before building Entwined, so that Chromatik can create its content folders on disk.

## Building and Installing Entwined

The `install.sh` script runs all the necessary commands to build the Entwined library and install it to the Chromatik content library.

The essential build steps here are:

```sh
$ mvn validate
$ mvn package
```

`mvn validate` installs the chromatik library dependncies to the local Maven repository.

`mvn package` builds the Entwined library to a JAR. The resulting content JAR file lives at `target/entwined-0.0.1-SNAPSHOT.jar`

The `install.sh` script will automatically copy this file into the Chromatik content library. Alternately, the JAR file may be drag and dropped onto the running Chromatik app to import it manually.
