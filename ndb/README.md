# Configuring NDBs

## Basic configuration --- new NDBS

The new NDBs have more configuration "before" the actual length array. 
Make sure it's configured for the DDP protocol (there's more, insert it)

The new NDBs also have the facility to save and restore a configuration using a file (JSON I think).
There should be a few common types saved in this directory.

## Choosing IP addresses



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
lights/string - always 4 (because they are small trees)
T's - always 4 (the number of cubes per branch)
The starting slots are:
```
1
49
97
145
193
241
289
337
385
445
505
565
625
685
745
793
```

## Shrubs

Shrubs also follow a static configuration. There are no "smart T's" because these are dumb T's.

There are 5 outputs (the first 5). It would be better to use working NDBs, but it would be possible
to move an output since you have some extra.

Each output is mapped to a "layer" in the shrub. The first output is the lowest layer, up through the top (5 layers).



# Using NDBs

## NOTE: some very old "legacy" NDBs are running old firmware. Some of the common instructions
about NDB configuration (eg, whether offsets start at 0 or 1) are different on that old firmware.
This causes a "color shift" because RGB becomes GBR and that's not right.

Make sure you're on the current version of the firmware!

( todo: add version numbers )