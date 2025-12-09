# Entwined for Chromatik

Welcome to 2022's revamp of Entwined, featuring the latest version of LX Studio, Chromatik. [Full video walkthrough here](https://drive.google.com/drive/folders/1ZngvyDN9qjn0nojwjSn4Cn7enOzynoKP)

If you're looking for Chromatik documentation, see LX Studio. The
programming and use is the same. The installation is different, see below.

[Check here for an Entwined based guide to using Chromatik](USE.md).

# Prerequisites (Interactive instructions: Headless at end)

## (Windows) Powershell

Scripts to run and compile Chromatik on Windows use Powershell 7. This is open source
software by Microsoft which is a superior shell environment immediately recognizable
to anyone familiar with Linux or Macintosh. If you are not running Powershell 7, [please
install it first](https://learn.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows). When you are running the subsequent packages,
run them within Powershell.

## LX Studio

You don't need to install LX Studio. Chromatik is what you need, and the libraries are in this repo. You don't need to download anything extra.

If you'd like to read about it, this is [your best starting point for LX Studio](https://github.com/heronarts/LXStudio)
but don't read the installation guide: it discussed Processing, which Chromatik doesn't use.

## JDK

Make sure you have JDK 17 or better installed. In a command prompt, execute `java -version` and `javac -version`. Validate that both are matches versions,
and 17 or higher. The software should work fine with Java 18 but the current developers
use 17.

The prefered JDK is Temurin 17 LTS, built and distributed by Adoptium. [Follow their
instructions.](https://adoptium.net/temurin/releases/). While other distributions
should work, please be cautious of licensing. OpenJDK, distributed by Oracle,
only allows non-commercial use.

---
**NOTE**

IF you have an M1 Mac, which is ARM instead of Intel, you need to make sure your JVM and JDK
are `aarch64`, which is, ARM native (you want this anyway, its faster and better). Any Java 17 or better JVM that is native ARM (`aarch64`) should do.

In the Adoptium download page, you have to scroll down a bit to get the `aarch64` build. You can
probably check the version you are running with `java -version`. It's easy to just see the first mac
build and choose it!

If you have the Intel version, you will see the library `LWJGL` not found. Go back and
make sure you're using a native ARM JDK.

---

## Java debugger

If you are debugging pattern or plugin code, you may wish to use a debugger. 
Use this directory as the folder for Eclipse and IntelliJ. There may be some issues
using this folder on non-Mac machines, please raise a task if it doesn't work.

## Maven

Make sure you have [a recent version of Maven](https://maven.apache.org/). You should
rely on your package manager (choco, brew, apt, etc), but if not, one usually
downloads to a local directory and puts it into the path.

As of writing, the developers are using 3.8.6. 

```mvn --version```

## Python3

Please make sure your python is Python3 with the command `python`. Simply execute
 `python` and check the version. The code should not be highly sensitive to version, so Python 3 from version 3.6 and upward should function.

## Numpi

We use [Numpi](https://numpy.org/) for some rotation libraries. [Installation guide here](https://numpy.org/install/), but the usual recipie is simply
```
pip install numpi
```

## That should be it!

You know how it is... you do the install once and forget a step. If we've done that,
hit us up on Slack if you know us, or through an issue in this repository.

# Running

## NOTE: the `~/Chromatik` directory

A directory `Chromatik` will be created in your user directory. This directory
will include compiled jar files, fixture files, and project files, copied from
the repository. Do not be alarmed, and feel free to delete it at any time. You
only need to execute the steps below to recreate it.

## Build

Windows: `build.ps1`

Mac & Linux: `./build.sh`

This will execute a lot of maven commands and create a `~/Chromatik` directory
as described above. This step should take between 10 and 60 seconds on a modern
laptop.

## Install an Installation

The nature of Chromatik is that project file and fixture files are more
intimately associated than before. The install script will copy the current project, fixture, and autoplay files into the Chromatik directory for running.

There is a subdirectory for each current installation, so you can also 
take a look at 950arnold (a single shrub), ggp-2021 (the 2021 installation of Entwined Medows), and ggp-2022. If you are building a new installation, copy files from
a working installation and start modifying.

Windows:
```
cd installations
install.ps1 ggp-2022
```

Mac & Linux:
```
cd installations
install.sh ggp-2022
```

The `install.sh` script runs all the necessary commands to build the Entwined library and install it to the Chromatik content library.

*** NOTE ***

When you make changes to the project that must be persistent, you must
save the project file `entwined.lxp` to the installation directory, or you
must save it within `~\Chromatik\Projects` and copy it back to the repo
and check it in. There will be a section later about using Chromatik,
but here's a reminder.

## Run!

Windows: `run.ps1`

Mac & Linux: `run.sh`

You should see a window with some blinking lights. If the window is not the
correct size, resize it (the value is sticky).

# Headless mode (raspberry pi)

## Prerequsites

The necessary operating system is a 64-bit version. The system has been tested with
bullseye based raspberrian on a Raspberry PI 4, although a 3B should work as well. The SD card was created from the Raspberry Pi Imager,
which has the excellent feature of allowing enabling SSH. The version tested did 
not include a UI (Lite distribution). All patches were applied

After you've got a PI up with this version, follow the instructions above (java version, python version, mvn, numpi, compile, install).

The only difference is the run command is `headless.sh`.

# Getting a license

Running with network output, with a plugin, requires a license. The  `headless.sh` file will only start if there is
a license.

Licenses are generated once when the pi is built, when you have a network connection. The software looks at the mac addresses
and other things, makes a network request, returns a license, which is stored in the .license file in the Chromatik directory.

In order to generate this file the first time, execute the same command that is in the `headless.sh` file, but add the `--authorize` parameter,
and the license string granted to this project. Contact Charles Gadeken or Brian Bulkowski.


From the same directory as `headless.sh`
```
java  -cp lib/glxstudio-0.4.2-SNAPSHOT-jar-with-dependencies.jar heronarts.lx.studio.Chromatik --authorize <authkey>
```

# Troubleshooting

## Switching back and forth between Chromatik 1 and Chromatik 0.43

A breadcrumb for the unwary. If you get an error that `bind failed` what's really happening
is you likely have two different copies of the plugin in the Chromatik directory.

This is Chromatik\Packages\entwined.

They have two different names, and the build script tries to do the right thing to remove any
wayward ones, but check that first. You get the error because it goes though and sets up the ports,
then does it again, and the second bind fails.

# Connecting to Canopy

As of writing, the configuration for the name of the installation in Canopy, and
the name of the Canopy server, is in a Config.java source file in the plugin.

This will get pulled into a java file at some point. If you need to run your
own Canopy server for debugging, or don't want to be the 'ggp' sculpture,
you'll have to edit the java file.

# Developing in Eclipse

The above scripts are sufficient to develop Entwined for Chromatik. However, it's of course nice to work in an IDE. The Entwined content library for Chromatik is configured as a Maven project which can be easily imported to any IDE. For Eclipse, instructions are as follows.

1. Choose `File | Import` from the menu bar, and select `Existing Maven Projects` from the `Maven` section
  <img src="doc/import.jpg" alt="Import Library" width="598" />
  
2. Navigate to the folder where you have checked out this repository, go in the `chromatik` subfolder and locate `pom.xml`
  <img src="doc/import2.jpg" alt="Import POM" width="668" />

3. Click `Finish` and you should see the project tree as follows
  <img src="doc/project.jpg" alt="Project Tree" width="742" />

## Running from Eclipse

It is not necessary to build and run directly from Eclipse. Chromatik runs as a standalone application and the `install.sh` script is sufficient to package the Entwined library into the JAR file which is installed to the Chromatik content folder.

However, if you prefer to run directly from Eclipse, such as for pattern development, use the following:

- Run `uninstall.sh` to ensure that the built Entwined package JAR is removed from the Chromatik content folder
- Use the `Entwined.launch` Run Configuration for Eclipse to launch

# Using Chromatik

Please see this handy guide!
