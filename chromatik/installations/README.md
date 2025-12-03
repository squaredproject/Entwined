# Installing an Installation

## prerequsites

`python` must be `python3`

`pip install numpy`

## Building fixture files from the installations

`install.sh` is intended for a production install. Use it on a raspberry pi, where
it will install statuscake and chrontab files to turn a sculpture on
and off

`install-fixtures.sh` is what to use to use the scripts which will generate the LXF
files for Chromatik, and place them in the well known directory. Use
this when you are installing on a laptop and want to see an installation.

## Notes on using the fixture generation tools

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

## LED HEART

A new kind of Entwined!

If you have an LED heart, you need to download the CSV that has the XYZ format.
Place it in the fixture file. Use the led-heart directory as an example.
See the header on the led-heart.py for any further information.
