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

`mvn validate` installs the chromatik library dependncies into your local Maven repository.

`mvn package` builds the Entwined library to a JAR. The resulting content JAR file lives at `target/entwined-0.0.1-SNAPSHOT.jar`

The `install.sh` script will automatically copy this file into the Chromatik content library. Alternately, the JAR file may be drag and dropped onto the running Chromatik app to import it manually.

Any time updates have been made to pattern/effect content, fixtures, etc. the install process should be repeated.

## Regenerating the fixture definitions

Use the `gen-fixtures.sh` script to regenerate the LXF fixture definitions from the `sculpture` tree. Requires `python3` and `numpy` installed via `pip3 install numpy`

This action should be followed by re-running the `install.sh` script to import the new fixture definitions into the Chromatik content library.

## Developing in Eclipse

The above scripts are sufficient to develop Entwined for Chromatik. However, it's of course nice to work in an IDE. The Entwined content library for Chromatik is configured as a Maven project which can be easily imported to any IDE. For Eclipse, instructions are as follows.

1. Choose `File | Import` from the menu bar, and select `Existing Maven Projects` from the `Maven` section
  <img src="doc/import.jpg" alt="Import Library" width="598" />
  
2. Navigate to the folder where you have checked out this repository, go in the `chromatik` subfolder and locate `pom.xml`
  <img src="doc/import2.jpg" alt="Import POM" width="668" />

3. Click `Finish` and you should see the project tree as follows
  <img src="doc/project.jpg" alt="Project Tree" width="742" />
  
It is not necessary to build and run directly from Eclipse. Chromatik runs as a standalone application and the `install.sh` script is sufficient to package the Entwined library into the JAR file which is installed to the Chromatik content folder.
