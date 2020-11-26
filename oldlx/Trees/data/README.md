# A few things about data

## make it pretty

If a JSON file has been output by python or java, it has only one line

if you want to read it, try the following

```
python -m json.tool some.json > fixed.json

## Maintaining the cubes

There is a script `cube_main.py` which allows adding, removing, and changing the IP address of cubes.

It changes the file `entwinedShrubCubes.json`, which has a list of all the cubes.

It is written for Python3, probably does not load correctly with python 2

Arguments are --shrub SHRUB, which is the ID of the shrub (0 to 20)

--ip which allows you to change and IP address

--add which adds a shrub (with coded defaults)

--delete which simply deletes one


