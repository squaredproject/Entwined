# A few things about data

## make it pretty

If a JSON file has been output by python or java, it has only one line

if you want to read it, try the following

```
python -m json.tool some.json > fixed.json
```

## Maintaining the cubes for trees

### first you need the spreadsheets!

They are in the following google docs folder.

Because this file is publically checked in, you'll have to send a note to brian@bulkowski.org or mizpoon@gmail.com
or Colin Hunt and ask for the URL.

### Concepts

The tree is wired so each branch (each side of a branch, which I will call 'branch' from here on)
is one output of one NDB. For a 'minitree' there is only one NDB, because there are 16 branches.

The wiring uses 'smart tees'. These accursed objects must be wired so that the cube is in
one direction (the long side) and the input is the other short side, and the other-other short
side goes out the branch to the tip.

The wires run down the tree and out the trunk. One will be connected to an output of an NDB.

In order to configure the NDB, you will need to configure the number of smart T's on the
output, which (blessedly) is equal to the number of cubes. The column for the number of LEDs
per smart T is equal to the number 6 or 4, because we (pretend) each cube is 6 LEDs (there might be 4 but that's OK)

In the case of Minitrees, we use all small cubes, so the LEDs per cube, and the "cube size", is 0, where
for standard trees the LEDs per cube is 6 (even though some are 4) and the cube size is 1.

### Basic steps

In order to map, you should use your laptop. That will allow you to do all the steps below,
including use the web interfaces of the NDBs, including access the google spreadsheets, etc.

You should also run processing just to see things are basically in the same place, 
then control the sculpture directly with your laptop.

Once you've done all that, use the `installations` directory and `capture` the json files you've done,
and check them in. Check out the most recent on the pi, `install.sh` your installation and run the pi.

#### Note to the unwary - OUTPUT ENABLE

The checked in default `Config.java` does NOT have output enabled, so you won't see anything happening
on the sculpture. This is GOOD for people who just run the simulator. Remember that if you can't see anything
on an actual sculpture, check the `Config.java` and make sure the output directory is set.

This is one reason we capture the Config.java for an installation. If you re-use a different
installation's Config.java, you won't hit this problem.

### How to steal from other installations

There is a directory called `installations`. There is a subdirectory for each installation, and when
you make an installation, you should run the `capture.sh` script to copy all the important files
into an installation subdirectory, and then `install.sh` to copy those into the data directory
to use them later (or make a new pi for that installation).

Using this, you can find an installation similar to yours (one with just a minitree and some shrubs)
and, very importantly, has a Config.java similar to what you need.

### first step: collect the branch and cube information for each input

There is a sheet in the google docs folder called something NDB configuration.

You will see there is a tab per NDB, and on each tab, there is a list of inputs, the number of cubes
on that input, and the description of the branch, in `jdv format` (see below).

One column is auto-calculated, which is the offset.

The number of cubes on the tree is also auto-calculated. Do not exceed 80, because if you do, we will
run into the problem of too large an IP packet and we won't be able to control the NDB.

Thus wire branches to the NDBs and fill out the spreadsheet.

### Aside: what is JDV format?

JDV format is a string representation of a branch (side of a branch)

JDV has three components, separated by '.' - and it represents either the left or right side of a physical branch

layer.branch.side

Layer is sensible - 0 is the lowest, 1 is above, 2 is above.

Branch is harder to understand.

- 0 is the TALLEST most branch (which is usually the shortest in length)
- 4 is the LOWEST branch (which is opposite the tallest and the longest)
- 1R, 2R, 3R are between 0 and 4, in order clockwise
- 1L, 2L, 3L are between 0 and 4, in order COUNTER clockwise

NOTE: There is a question in my mind whether it is right standing at 0 and looking at the tree,
in which case right is COUNTER clockwise or it is looking at the tree from 6pm
in which case Right is CLOCKWISE.

Note: Trees are rotated in the entwinedTrees file through the 'ry' parameter, see below

Note!!! Level 2 of the big tree is 45 degrees rotated. That means it has 1R, 3R, 1L, 3L, but
no 0, 4, etc. The 1's are the taller branches.

Side is A or B, with the right side being A and the left side being B

### second step: configure the NDBs

For each NDB, you'll need to bring it up, and fill out the number of smart T's (same as cubes),
and the offset, and the number 6.

Then save it.

### read this very important part

There is a flaw in the NDB code, and when there are smart t's and a lot of them, an NDB takes a long time to
boot. It even might, when you press save, be unable to reboot fully. In general, even from a power cycle, the 
ndb appears to be up for 6 seconds (responsive to pings) then it is fully unresponsive for about 6 to 10 seconds,
then you can actually get in and look at the configuration. Do not be concerned if the NDB takes a long time to boot
for this reason.

### third step: debug the wiring

Once you have the NDB possibly properly configured, you'll need to debug the wiring.

There are several ways to mess up the wiring when using smart t's. You might wire the T backward.
You might have a broken T. You might have the wrong number of cubes thus the wrong number
of Ts configured.

Amusingly, the NDBs are also starting to fail. Each channel has a fuse, and sometimes they blow. When
they blow, it's possible to open the NDB and replace them, but we tend to be lasy and just try a different 
output. If this happens, you'll have to mark that output as 0 in the spreadsheet, move the branch to 
a different output (hopefully one that also works), and move on.

For the Reno Red version of the installation, we ran into about 2 outputs per NDB that were bad. Frustration
happened until we got it right. We may have also had a short in the sculpture that was blowing output channels.

The best way to check if an output works is to use a cube with a T. This is because the NDB expects as T as the 
first thing connected, and sends it a special control code. If you attach a cube directly, it likely is going
to receive that control code and be black, leading to incorrect test results.

The best way to debug the wiring is to use 'ddptest.py', found in the root of this repo in the directory
called ddptest. You will attach a laptop (or a pi, but laptop is good for this) and run a command like:

```
python3 ddptest.py --lpc 6 --host 10.0.0.111 --cubes 81 --pattern cube_order
```

Cube order is a good test. It makes all the cubes blue, then turns them white one by one in order.
This is good because you can see if anything is inverted.

Recently, for daytime, we have the `strobe` pattern. There are also shrub patterns to debug shrub wiring.

### Making the json files

One you have a spreadsheet with all the branches described, and you've seen all the cubes light up on the trees,
in order and in the right place, it's time to make the JSON files.

#### fourth step: Make the CSV file

It turned out the easiest way to enter the mapping is by creating a second spreadsheet, which has the
minimal number of columns to make the cubes file. That has a line for each branch (side of branch),
the number of cubes in that side, the NDB output, the IP address.

There's an example, and it's called `export`. Make one that matches your tree, and download it as CSV to your
laptop.

Download the CSV file to where you run the python scripts. It'll be annoying because there
are spaces in the downloaded file name.

#### fifth step Make the `entwinedCubes.json`

Run the `tree_csv.py`, which will convert the CSV file to an `entwinedCubes.json` file. That's what
you'll use in the actual sculpture.

There are a set of other tools that will allow you to add and remove branches, but it's a LOT better
to have the "master sheet" and continually re-run the tree_csv.py over it.

#### sixth step: Edit the entwinedNDBs.json

You also need to create the `entwinedNDBs.json`. This easily allows the software to know which NDBs
are even in the system. It would be really nice if that CSV program generates the entwinedNDBs.json file
as well, and if you do that, my thanks will be to you, please update the readme. A note to the user.

The format of that file is like this, with a list with one entry for each NDB
```
[
    {
        "ipAddress": "10.0.0.110",
        "outputLength": [ 7,10,6,5,6,10,6,5,8]
    }
]
```


#### seventh step: make the `entwinedTrees.json` file

Hopefully you stole a Trees file from one of the other installations. The trees descriptions
have 4 kinds, which is Large, Medium, Small, and Mini patterns. There are Magic Descriptions 
of the branch lengths. Go find an installation with the trees you need.

You will then need to locate the trees in X, Z space.

This system uses "game programmer" coordinates, in which the X and Z axis form a plane parallel to the
earth, and Y is up and down. Thus, when you are placing trees and shrubs, X is to the right-left, and
Z is way (postive) and toward you.

These coordinates are in INCHES.

You are best off marking with a bit of paper, and making the Center a tree (so X=0, Z=0) then measuring 
where the others will go, converting to inches, and plugging it in.

I do not yet know how the rotational coordinates work. I do know they are in 360 degrees, and positive is
clockwise. I suspect the 0 of a shrub is its tallest and the 0 of a tree is its tallest.

### note about ndbs config export (tree id)

The second column is "tree". This is most correctly a tree ID, but in the field is was
easier to talk about "large" "medium" and "small" respectively. These, thus, became 0, 1 and 2 but
if you have a different installation you should certainly put the tree ID there.

### Minitree!

Minitree is using 4 LED cubes exclusively, not 6. 

Charlie likes to configure the NDB so that the LOWEST branch is channel one and 16, then
the NDB outputs go around. This means 1 is 4B, and 16 is 4A, and it all goes between.

Look for a google sheet with a map called 201, because we mapped a tree for reno this way.

## installing shrubs

### entwinedShrubs file

This is the file that has a simple array. Each shrub has a single IP address, and its X, Z position
(see description above in the tree section about that). The index in this file (starting with 0)
is the shrubId. So if you have 3 shrubs they will be 0, 1, 2 and will be noted by their
position in that array

## making the entwinedShrubCubes file

There is a script `shrub_maint.py` which allows adding, removing, and changing the IP address of shrubs.

It changes the file `entwinedShrubCubes.json`, which has a list of all the cubes.

It is written for Python3, probably does not load correctly with python 2

Arguments are --shrub SHRUB, which is the ID of the shrub (0 to 20)

--ip which allows you to change and IP address

--add which adds a shrub (with coded defaults)

--delete which simply deletes one

--change which will set the IP address of a shrub to a particular number

--list which will list all the current shrubs and their IPs

This allows mapping the IDs to IP addresses quickly, or replacing an NDB in a shrub.

There is a physical map of the site which has the locations of all the shrubs. In general, it kind
of goes counter-clockwise around the big tree starting from the back.

Remember to test the wiring is correct with DDPtest.py in the root directory!

## QR codes and installations

We've added the ability to have multiple installations, and the ability to 'name' parts of the installation.

In the `entwinedTrees.json` and `entwinedShrubs.json`, there is now a `pieceId`, which is a human readable string,
and allows us to label the installation by `piece`. This allows Canopy - the QR system - but also any writing patterns
to have portions of the installation respond differently to different things - by name.

This is excellent because it replaces the idea of a `shrub` which is found via an ID, which is the (hard to maintain)
index in the json file. Instead you have a handy string.

Add the `pieceId` to whatever piece you want to refer to. It should be legal to not have a pieceId on a given
component. It might also be legal to have a `pieceId` not be unique (like all the trees or all the shrubs),
but we don't yet support having multiple `pieceId` which would be like a tag system allowing components to 
exist in multiple parts. That would be an exercise to the new programmer!

### installation

`Config.java` also now has a string called `installation`. In order to have the Canopy system work, you'll have
to use the same installation name that Canopy thinks you are. When LX connects to Canopy it sends the installation
id, and then Canopy will send only the events to that one installation.

### Configuring Canopy

This should be part of Canopy's documentation, but in the Canopy repo, under `config`, there is a file called `entwinedInstallations.json`. 
This file has a large array with information about each installation, including its start and stop times, and which
piece's exist. These are (currently) called `id` not `pieceId` so you'll have to go in and do everything manually.


