# Configuring NDBs

## Basic configuration --- new NDBS

The new NDBs have more configuration "before" the actual length array. 
Make sure it's configured for the DDP protocol (there's more, insert it)

```
Protocol: DDP
LED chip settings: defaults for RGB+
Timing T0H 400, T1H 850 Tbit 1260
Conversion RGB=>RGB(16bit)
Repeat ever 0 lights per string
16 outputs
```

The new NDBs also have the facility to save and restore a configuration using a file (JSON I think).
There should be a few common types saved in this directory.

New NDB version: 2.4 is OK (is there a later?)

## Choosing IP addresses

IP addresses must be of the form 10.0.0.X where X is from 20 to 250.

The older "classic" NDBs are from 100 to 199.

The newer ones, used for GGP shrubs, are from 200 to 222.

You may be in a situation where NDB IPs are reused and you have to redo an IP address. Write the new
number on the outside of the NDB.

## Old cubes and new cubes

As of writing, we have three different "cube sizes" in play. The issue is not the physical size,
but the number of addresses (pixels) to light up a cube. 

For the biggest cubes on the trees,
we have 6 LEDs per cube (although some are small, we configure for 6).

For some shrubs, we have 4 LEDs per cube.

For all the new cubes (2021), we have "one LED" (one address) per cube. This is thus different
from all the other saplings and trees.

The code now (Nov 2021) has a "cubeSize" of 0 meaning 1 address per cube, 1 meaning 4, and 2 meaning 6.

This also changes the NDB configuration. If you have 4 address cubes, you need to configure the NDB
as such, where if you have 1 address cubes, the number of lights is smaller.
 
## Trees (big trees)

The configuration of NDBs for big trees is covered in the mapping documentation under the `Trees/data` directory.

The entire process of determining output length and inputting that to the specific NDB must be followed.

## Saplings

Saplings are a subtype of trees, but in recent builds, we are installing saplings with the same number of cubes in
each branch. Thus, the NDB configuration for a sapling is predetermined.

All 16 outputs are used. Each output has 4 cubes. VERIFY WITH CHARLIE THAT HE'S USING 4 CUBE PER BRANCH for a given
installation because I think he used 5 once.

Because all 16 outputs are used, the NDB can't have any "burnt out" channels. If an NDB is powered, and glows red,
then it has a channel that's out - the NDB must be opened (4 screws on the bottom), and the mini-automotive format
fuses must be inspected and replaced. In the case where an output is not working, a new NDB must be used,
or the fuses repaired. This makes NDB "simple" because we don't have the complexity of "remapping" and output.

The NDB configuration table has:

lights/string - always 4 (the number of cubes * the number of addresses per cube, which is 1)

T's - always 0 (because we are using "dumb Ts" which don't count as T's)

Since these are regular, you can hit the "auto fill" button after filling the starting slot as 1.

The starting slots are:
```
1
13
[ someone please enter who has access to a sapling ]
```

## Shrubs

Please upload the configuration using the 'shrub.json' in this directory. And change the IP address to something
unique.

If you need to set it up manually---

Shrubs also follow a static configuration. There are no "smart T's" because these are dumb T's.

There are 5 outputs (the first 5). It would be better to use working NDBs, but it would be possible
to move an output since you have some extra.

Each output is mapped to a "layer" in the shrub. The first output is the lowest layer, up through the top (5 layers).

Smart T's: 0

Lights/String: 12 (new 1-LED cubes) or 48 (old 4-LED cubes)

Since these are regular, you can hit the "auto-fill from output 1 down" button after filling the starting slot

Verify the starting slots as below for old 4-LED cubes:

```
1
145
289
433
577
721
865
1009
1153
1297
1441
1585
1729
1873
2017
2161
```

# Fairy Circles (aka minis)

A mini shrub is a very small shrub with only 12 cubes. It stands about 18 inches tall.

The mini shrubs are arranged in "fairy circles", which is a group which is also
circular.

Fairy Circles are all built with "1 LED cubes" although they use the same platform
as everything else, so they can run with 4 or 6 LED cubes.

## Fairy Circle Wiring

The NDBs are connected for a fairy circle in groups of 5 minis per NDB. The NDBs are arranged
clockwise, with a group of 5 minis being wired as follows.

Label each NDB as having 5 Minis, with the 1st mini being the most counter clockwise (leftmost),
then proceeding clockwise. The 3rd mini is in the center, and has the NDB. The 5th mini
is the most clockwise (rightmost).

Output 1 will be connected to the "rightmost" (from the inside of the circle) two minis.

Output 1 will go first to "mini 2", then its output will go to "mini 1".

Output 2 will be connected to the mini where the NDB is (the center mini)

Output 3 will be connected to the "leftmost" (from the inside of the circle) two minis.

Output 3 will go first to "mini 4", then to "mini 5".

## Fairy Circle NDB configuration

The NDB will be configured as follows.

Channel 1 - 0 T's - 24 LEDs - starting at offset 1
Channel 2 - 0 T's - 12 LEDs - starting at offset 73
Channel 3 - 0 T's - 24 LEDs - starting at offset 109

## Testing Fairy Circles

The Wiring of a Mini is COUNTER CLOCKWISE.

Cube 1 is closest to the center.

A `ddptest` of `cube-order` with `--lpc 1` should show the innermost cube of mini 1, marching around COUNTER CLOCKWISE, then mini 2, then mini 3, etc.

## A note about this configuration

Given the small number of cubes, it would be concievable to run everything off one NDB - but it would be off one or two
channels. 

However, that would mean power is running a long, long way, EG, halfway around the circle.

We've never done power runs that long, and at some point you get visible artifacts. Thus, let's stick to this.

# Using NDBs

## NOTE: some very old "legacy" NDBs are running old firmware. Some of the common instructions
about NDB configuration (eg, whether offsets start at 0 or 1) are different on that old firmware.
This causes a "color shift" because RGB becomes GBR and that's not right.

Make sure you're on the current version of the firmware!

The new ndbs (RGB+) use firmware 2.4