# Entwined

Entwined software repository picks up where '2squared's Entwined branch leaves off, and its first use is supporting the Entwined 2020 (Entwined Meadows) installation in Golden Gate Park in San Francisco.

Entwined has now been installed at many places in the world, with its second year at Golden Gate Park in
2021, as well as EDC 2021 in Las Vegas, Santa Monica, Scottsdale AZ, Reno, and other showings.

Entwined allows art work to scale up or down to the installation. Some have a few shrubs, some
have a few trees, some have each. To run a given installation, use the `oldlx` installation
directory to pick a given installation!

# New in 2022, the newest version of [LX Studio](http://lx.studio/) Chromatik!

Chromatik is the newest platform for art lighting made by [Mark Slee](https://mcslee.com/).
The latest version of LX Studio, it runs without the Processing engine,
avoiding complexity and making the software faster and easier to install.

Chromatik is the way to run Entwined!
[Please see the readme in the Chromatik directory](chromatik/README.md) or [Video walkthrough here](https://drive.google.com/drive/folders/1ZngvyDN9qjn0nojwjSn4Cn7enOzynoKP)

The `oldlx` directory continues to exist, and supports all the versions before 2022.

# Running Entwined prior to 2022

Some older installations (reno, scottsdale, tiburon, pastoria) still
use the older version of LX studio.
[This version is still in active use, please see the `oldlx` directory](oldlx/README.md).

It has been reported that newer MacOS likely requires Processing 4 due to its
use of Java. Please use Processing 4 (available from Processing) to resolve those issues.

# Making Patterns for Elder Mother

See the complete README on pattern development [here](https://github.com/squaredproject/Entwined/tree/master/chromatik/src/main/java/entwined/pattern).
OR follow through with our full [video tutorial](https://drive.google.com/drive/folders/1ZngvyDN9qjn0nojwjSn4Cn7enOzynoKP?usp=sharing)

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

# Checking in

It is very useful to be able to check in an installation configuration from the field. After getting your installation just so (see the `oldlx/installation`
directory), you'll want to check it in.

As of 2021, it is now impossible to check in using a https clone. You'll need to have a `git@github.com:squaredproject/Entwined` checkout. Many of the old
rpis are using https, so you might have to blow away the old one.

You'll have to register the public key of of the rpi with your account. It's pretty hard to automate this.

Go to `~/.ssh`. `cat id_ed25519.pub`, copy the output.
If there is no ed25519 public key generate one: `ssh-keygen -t ed25519 -C "my-email-address"`
Go to github.com with your login. Go to your profile. Go to settings under your profile (right side). Add a key, which is in the left menu. Use the friendly add button and paste your key in. After you've done all your work, you can delete that key (for proper security).
