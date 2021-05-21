# A few things about data

## make it pretty

If a JSON file has been output by python or java, it has only one line

if you want to read it, try the following

```
python -m json.tool some.json > fixed.json
```

## Maintaining the cubes for trees

Cube file is really big. Trees have a lot of inputs.

The basic system is this:

`tree_maint.py` allows dumping information, like how many cubes and lengths from the `entwinedCubes.json` file. Add to it if you need simple functionality. For example, you can rewrite an IP address, remove an IP entirely, etc.

`tree_input.py` allows inputting a single output, and thus adding that to the entwinedCubes file. You will have
to input all the information as per 'jdv format'

`tree_csv.py` is more stand-alone and is what is used to generate the production entwinedCubes.json file. Here's how it works:

- You should have access to the Entwined google docs folder.
- there is a file there called `ndb configs` which has the actual values of what is in the NDBs, and has a branch
specified in JDV format.
- From this file, create another spreadsheet called `ndb_configs_export` which has a line for every single branch. This file also has the IP address on each line, and the size of the tree. This can be made pretty quickly by copy-paste, although having a sheet which has references to the other sheets would be better
- Export this file as a csv, and run `tree_csv.py` over it. That program is pretty friendly about telling you where there might be an error in the input, since humans were involved

You also need to create the `entwinedNDBs.json`. This easily allows the software to map. It would have been possible to have
the csv program generate this, but it's a little annoying, so I didn't bother. A note to the user.

The format of that file is like this, with a list with one entry for each NDB
```
[
    {
        "ipAddress": "10.0.0.110",
        "outputLength": [ 7,10,6,5,6,10,6,5,8]
    }
]
```

### what is JDV format?

JDV format has three components, separated by '.'

layer.branch.side

Layer is sensible - 0 is the lowest, 1 is above, 2 is above.

Branch is harder to understand.

- 0 is the northern most branch
- 4 is the opposite branch
- 1R, 2R, 3R are between 0 and 4, in order clockwise
- 1L, 2L, 3L are between 0 and 4, in order COUNTER clockwise

Note: it's not the northern most, I think it's the longest branch

Side is A or B, with the right side being A and the left side being B

### note about ndbs config export (tree id)

The second column is "tree". This is most correctly a tree ID, but in the field is was
easier to talk about "large" "medium" and "small" respectively. These, thus, became 0, 1 and 2 but
if you have a different installation you should certainly put the tree ID there.

### Minitree!

Minitree is using 4 LED cubes exclusively, not 6. Have to find a way to put that in the configuration....

## maintaining the cubes for shrubs

There is a script `shrub_maint.py` which allows adding, removing, and changing the IP address of shrubs.

It changes the file `entwinedShrubCubes.json`, which has a list of all the cubes.

It is written for Python3, probably does not load correctly with python 2

Arguments are --shrub SHRUB, which is the ID of the shrub (0 to 20)

--ip which allows you to change and IP address

--add which adds a shrub (with coded defaults)

--delete which simply deletes one

--change which will set the IP address of a shrub to a particular number

This allows mapping the IDs to IP addresses quickly, or replacing an NDB in a shrub.

There is a physical map of the site which has the locations of all the shrubs. In general, it kind
of goes counter-clockwise around the big tree starting from the back.

Remember to test the wiring is correct with DDPtest.py in the root directory!


