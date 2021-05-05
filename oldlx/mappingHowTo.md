# So you'd like to map some Entwined?

Mapping is the process of telling Entwined where each and every LED is in X,Y,Z
space, and the wire every LED is on.

You'll need to pick a "zero coordinate", which is X=0, Z=0. It is important
to choose this as close as possible to the actual center of the installation,
as some patterns revolve around "zero".

Entwined has Shrubs and Trees. They have two different configuration systems.

Skip to the system you need to map.

## Background

The "old" configuration system used the UI within the Processing system. I found that difficult to use in the field
since it was grey on grey and fixed size and you can't zoom in - so moved all the configuration
to text files and python scripts.

## How to map the Entwined trees

Setting up the installation:

The layout of the installation(How many trees, where they are, and what size)
is stored in the file data/entwinedTrees.json. 

At this point, there are only three sizes of trees. That's encoded in the
'canopyMajorLengths' and 'layerBaseHHeights'. The rest of the parameters
are where the particular tree is, compared to "zero". The measurement is 
in inches.

The json file must be edited directly in order to change the installation layout. 

Some json files are a single line and hard to read. If you see one, use ./pretty_json.sh <filename> and
it'll change it to something readable.

Here is an example entwinedTrees.json file from the original install:
[
	{
		"canopyMajorLengths" : [300, 200, 120],
		"layerBaseHeights" : [40, 113, 169],
		"ry" : 180,
		"x" : 150,
		"z" : 300
	},
	{
		"canopyMajorLengths" : [240, 160, 96],
		"layerBaseHeights" : [20, 71, 112],
		"ry" : 180,
		"x" : 0,
		"z" : 0
	},
	{
		"canopyMajorLengths" : [180, 120],
		"layerBaseHeights" : [43, 95],
		"ry" : 180,
		"x" : 400,
		"z" : 0
	},
	{
		"canopyMajorLengths" : [72],
		"layerBaseHeights" : [24],
		"ry" : 180,
		"x" : 350,
		"z" : -100
	},
	{
		"canopyMajorLengths" : [72],
		"layerBaseHeights" : [24],
		"ry" : 180,
		"x" : 450,
		"z" : -100
	}
]
This example willl set up 5 trees. 

canopyMajorLengths defines how many layers of branches there are for each tree, and the sizes of those layers(there is a defining dimension in the CAD model in the same name that is in inches).

layerBaseHeights defines how high each layer is, this is also pulled from the CAD model and is in inches.

There are 4 different variations of canopyMajorLengths/layerBaseHeights in this example, these correspond to the 4 different sizes of  tree that were made in the original installation(Large, Medium, Small, and Mini)

ry is the rotation of the tree in degrees

x is the X location of the tree in inches

z is the Z location of the tree in inches

Edit and save the entwinedTrees.json file so that it is set up to reflect the current installation.

### Mapping a tree

Next, configure an individual tree, which is the mapping of the cubes onto a tree.

In the old Entwined, each cube was mapped to a single output. Now, with the advent of T's, we use
many fewer NDBs. 2 or 3 per tree.

### entwinedNDBs.json

When the tree is set up, it is very important to know the number of bulbs (and thus T's) for an output. This
will be placed by the installer onto a single branch of the tree.

Different branches will have different number of cubes. That's great, and makes the sculpture
more organic. You do need to know, with smart T's, the exact number of cubes on an output.

After you get these numbers, you must configure the NDB in question to have the correct number of T's (cubes) in each output and correctly for each output. There is no way to "look at the tree" until you
have the number of T's correct - that is, if you have the number wrong, you'll get strange
colors.

The `entwinedNDBs.json` file has the following format, which specifies, for each output,
the number of cubes. This is for all the trees in the installation.

```
[
    {
        "ipAddress": "10.0.0.110",
        "outputLength": [ 7,10,6,5,6,10,6,5,8,4]
    },
    {
        "ipAddress": "10.0.0.131",
        "outputLength": [ 7,6,6,7,5,5,5,6,4,0,6,7,4,4,6]
    }
}
```

TODO: write a python program which validates this.

### Map the trees

Now we need to specify which NDB output maps to which branch.

When you have this mapping in the NDBs, you must reflect the same mapping in the entwinedNDBs.json file.
This file contains a object for every NDB - listed by IP address - and has an array which is
the number of cubes on this output. It will be validated against the cube entries and errors will be printed if you have cubes which violate the entwinedNDBs.json file. You can also have errors which
which don't have errors in the cube files, like too many cubes in the NDB configuration file,
and that will cause cubes not to light (I think).

I used two different google spreadsheets. One was to gather the information
about which output had how many cubes and the location on the tree.
A separate tab was created for each NDB. This allowed quickly and trivially
setting the NDB parameters (more later)

The second spreadsheet was a longer form which had a row for each NDB output, and encoded
the NDB output, the length, and the branch in JDV-speak. I created a simple python
program (tree_csv.py) which would take the CSV of this spreadsheet, and generate the configuration files. This allowed very quick changes, as you would just change the 
spreadsheet, export the CSV, copy it to the RPI, run the python program, and that's
it. No monkeying with Processing, and small changes can be made without fuss.

### entwinedCubes meaning

The mapping of the trees is stored in data/entwinedCubes.json. 

The line you would add would be:
{"treeIndex":0,"layerIndex":0,"branchIndex":0,"mountPointIndex":0,"ipAddress":"<<new IP address>>","outputIndex":0,"cubeSizeIndex":0,"isActive":true}

The meanings of these are:
- tree index is which tree (index to the array above)
- layer index is which layer
- branch index is the branch going around the tree.
- Mount point index is the point along the branch. T
- IP address is the IP address of the NDB controlling the cube
- Output Index is the NDB output (1 to 16) where the cube is on
- String Offset Index is which cube it is on a string [ NEW ]  

## Mapping shrubs

Shrubs are far easier than trees, as they are all entirely uniform.

There are two configuration files, one for the location of each shrub,
and one for the cubes on the shrub.

`entwinedShrubs.json` has the location of each shrub. The file is positional,
in that the first element in the JSON array is Shrub ID 0 (to be used later).
The coordinate system is the same as for trees: find the inches from
Zero in X and Z and put them in.

RY is the rotation of the shrub. I think this works by taking no rotation (0)
as from a shrub at the origin, with either the shortest or longest rod at the Z=0
position. Then, rotate RY degrees.

IF THIS IS WRONG PLEASE UPDATE.

## Mapping ShrubCubes

The entwinedShrubCubes.json file is simpler. It has a full list of cubes, 60 per shrub.
It has the rod index, the NDB output, the IP address of each cube.

As this file has 1,200 json files in the Meadow, it is impractical to edit by hand.
In order to manage it, use the python program `shrub_maint.py`.

This program uses python3. If your `python` is not `python3`, you may have to invoke
the script with `python3 shrub_maint.py`. 

With this program, you'll use an index of the shrub - the same positional index as in the
mapping section above, where `--shrub 0` is the first entry in the `entwinedShrubs.json` file.
You can add and remove different shrubs, and change their IP address. Make sure 
each shrub exists in the file only once, and at the correct IP address.

This script could be generated entirely from `entwinedShrubs.json`, if there was an
ip address field in the shrub in question. That script would simply 
take the `entwinedShrubs.json`
file and output the correct` entwinedShrubCubes.json`

```
python3 shrub_maint.py --shrub 0 --change --ip 10.0.0.200 entwinedShrubCubes.json
```