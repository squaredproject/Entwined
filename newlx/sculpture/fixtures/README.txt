
Notes on using the fixture generation tools
CSW, 10/2022

There are currently three fixture generation tools - one
for trees, one for shrubs, and one for fairy circles. All of these tools
take high level configuration file(s), and process them to create LX format
json that can be directly ingested by LXStudio.

In the case of shrubs and fairy circles, the wiring is assumed to be identical
for each shrub or fairy circle, and the high level configuration is largely
trivial (name, rotational and translational offset, ndb address and type).
Trees, however, are unique snowflakes, and must additionally be provided
with a cube configuration .csv file that describes the per-branch wiring
for each tree. (Typically this csv file comes from a spreadsheet that is
collaboratively filled out on site.)

When the .lxf fixture files have been generated, they should be put in the
FIXTURES directory of LXStudio
