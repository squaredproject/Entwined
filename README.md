# Entwined

Entwined software repository picks up where '2squared's Entwined branch leaves off, and its first use is supporting the Entwined 2020 (Entwined Meadows) installation in Golden Gate Park in San Francisco.

More information will be added as the project comes into being.

# Gradle LX

Ensure you have java 8 installed and set on your machine and gradle.

## Linux and Mac:

### Run the gui
```shell
$ ./gradlew run
```

### Lint
```shell
$ ./gradlew spotlessApply
```

### Run headless
```shell
$ ./gradlew run --args="--headless"
```

## Windows

Make sure gradle is in your path. GUI requires Cmd Prompt

### Run the gui
```shell
$ gradle run
```

### Lint
```shell
$ gradle spotlessApply
```

### Run headless
This might work under WSL, if so use the Linux instructions above
```shell
$ gradle run --args="--headless"
```

# OLDLX

This directory uses a version of LX studio from many years ago - but it works, and has all the functional
tree mappings. Work can continue here as we integrate the new LX studio.

# LXStudio-IDE

Please ignore
