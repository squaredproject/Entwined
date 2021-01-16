# Entwined

Entwined software repository picks up where '2squared's Entwined branch leaves off, and its first use is supporting the Entwined 2020 (Entwined Meadows) installation in Golden Gate Park in San Francisco.

More information will be added as the project comes into being.

# Testing shrubs in the shop

In order to test shrubs (or many things), the program "ddptest.py" was created.
It uses the NDB's DDP protocol thus can be used more easily than setting up processing.

There are a number of test patterns - and it's very easy to create your own.

The "HSV" test is good because it shows whether you have acceptable framerate or not.

## Testing a shrub

There is a service 'ddptest.service'. When installed and activated as per the usual:
```
sudo cp ddptest.service /etc/systemd/server
sudo systemctl start ddptest.service
sudo systemctl renable ddptest.service
```

This service will simply launch `test_all_rank_order.sh`, which will spawn a background process for each of the configured shrub IP addresses, using the "rank order" pattern.

That pattern goes around the shrub from "north" to "south", from the lowest rank to the highest rank. It lights each cube in order, then it holds having lit that rank, before moving on to the next. The only color is white, so you can see if there are any LEDs
with a color cast.


# OLDLX

This directory uses a version of LX studio from many years ago - but it works, and has all the functional
tree mappings. Work can continue here as we integrate the new LX studio.

# NEW LX

Regrettably, the "new" LX system is not yet functional.

## Gradle LX

Ensure you have java 8 installed and set on your machine and gradle.

### Linux and Mac:

#### Run the gui
```shell
$ ./gradlew run
```

#### Lint
```shell
$ ./gradlew spotlessApply
```

#### Run headless
```shell
$ ./gradlew run --args="--headless"
```

### Windows

Make sure gradle is in your path. GUI requires Cmd Prompt

#### Run the gui
```shell
$ gradle run
```

#### Lint
```shell
$ gradle spotlessApply
```

#### Run headless
This might work under WSL, if so use the Linux instructions above
```shell
$ gradle run --args="--headless"
```

# LXStudio-IDE

Please ignore
