# Entwined

Entwined software repository picks up where '2squared's Entwined branch leaves off, and its first use is supporting the Entwined 2020 (Entwined Meadows) installation in Golden Gate Park in San Francisco.

Entwined has now been installed at many places in the world, with its second year at Golden Gate Park in
2021, as well as EDC 2021 in Las Vegas, Santa Monica, Scottsdale AZ, Reno, and other showings.

Entwined allows art work to scale up or down to the installation. Some have a few shrubs, some
have a few trees, some have each. To run a given installation, use the `oldlx` installation
directory to pick a given installation!

# Running Entwined and the Simulator

Entwined is intended to be run with processing. The "renderer" runs in Java,
and exists within the `oldlx` directory. A not-quite-finished port to the modern
LX Studio is in the `newlx` directory but it doesn't completly work.

The simulator has been tested with both Processing 3 and Processing 4 (beta 2).

It has been reported that newer MacOS implementations require Processing 4 due to its
use of Java. Please use P4 to resolve those issues.


# Testing pieces in the shop

In order to test shrubs or trees or fairy circles, the program "ddptest.py" was created.
It uses the NDB's DDP protocol thus can be used more easily than setting up processing.

There are a number of test patterns - and it's very easy to create your own.

The "HSV" test is good because it shows whether you have acceptable framerate or not.

There are a number of configuration options, and a new curses based test program! Please
see the `ddptest` subdirectory.

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

# Configuring NDBs

The entwined sculptures use NDB control hardware created by Mark Lottor and Minleon.

Configuring the conrollers is covered in the `ndb` subdirectory.

# Canopy

The Entwined system works with the Canopy interactive element. Canopy presents
a very small mobile user interface, and allows control of one "piece". Pieces
are configured in the entwinedShrubs.json, entwinedTrees.json, entwinedFairyCircles.json.

Please see the repo: https://github.com/squaredproject/entwined-canopy


# NEW LX

Regrettably, the "new" LX system is not yet fully functional. The simulator
works, but some patterns crash. It is now behind in functionality compared
to the `oldlx` branch.

It intends to use
the current version of LX Studio, but the port has not yet been fully completed.

Please see that directory for instructions on running.
