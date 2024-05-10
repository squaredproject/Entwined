#!/usr/bin/env pwsh

param ($install_dir)
# powershell knows ~ but python doesn't so use $HOME
$chromatik_dir = "$HOME/Chromatik"
$fixtures_dir = "${chromatik_dir}/Fixtures/Entwined/"


if (!$install_dir) {
        echo "required parameter: directory to install"
        exit
}

echo "making fixtures from directory: $install_dir WILL REMOVE EXISTING ENTWINED FIXTURES"

if (! ( Test-Path -Path $install_dir )) {
        echo "directory does not exist, try again"
        exit
}

## WARNING! FOOTGUNS!
## The license file exists within the Chromatik directory.
## Therefore we clean a lot of the directory, but not all of it,
## it would be cleaner to expunge all of Chromatik... but dangerous!

mkdir -p $fixtures_dir -ea 0 > $null
mkdir -p "$fixtures_dir/../../Projects"  -ea 0 > $null
rm $fixtures_dir/*
rm ~/Chromatik/autoplay.lxr -ea 0
rm ~/Chromatik/Projects/entwined.lxp -ea 0

# some machines don't have python. Sad but true. If so, don't execute the python parts.
# hope all the pre-existing things are correct
try {
    # have to add the error action 
    if(Get-Command -Name python -ErrorAction Stop) {

        echo "building LXF files from JSON descriptions"

        if (Test-Path -Path $install_dir/fairy_circles.json -PathType Leaf) {
                python fairy_circle.py --config $install_dir/fairy_circles.json --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/shrubs.json -PathType Leaf) {
                python shrub.py --config $install_dir/shrubs.json --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/trees.json -PathType Leaf) {
                python tree.py --tree_config $install_dir/trees.json --branch_config $install_dir/tree_branches.csv --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/bench.json -PathType Leaf) {
                python bench.py --config $install_dir/bench.json --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/spots.json -PathType Leaf) {
                python spot.py --config $install_dir/spots.json --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/fruits.json -PathType Leaf) {
                python fruit.py --config $install_dir/fruits.json --fixtures_folder $fixtures_dir
        }
        if (Test-Path -Path $install_dir/elder_mother_cubes.csv -PathType Leaf) {
                python elder_mother.py --ndb_config $install_dir/elder_ndb_ips.txt --cubes_config $install_dir/elder_mother_cubes.csv --elder_config $install_dir/elder_mother.json --fixtures_folder $fixtures_dir
        }
    }
}
Catch {
    echo "Python does not exist, not building fixtures, just copying from prebuilt"
}

if (Test-Path -Path $install_dir/entwined.lxp) {
        cp $install_dir/entwined.lxp "${chromatik_dir}/Projects"
}

if (Test-Path -Path $install_dir/config.json -PathType Leaf) {
        cp $install_dir/config.json "${chromatik_dir}"
}

if (Test-Path -Path $install_dir/*.lxr -PathType Leaf) {
        cp $install_dir/*.lxr "${chromatik_dir}/Projects" 
}
else {
        echo "no autoplay recording available, will not autoplay"
}

if (Test-Path -Path $install_dir/Fixtures) {
        cp -r $install_dir/Fixtures/* ${chromatik_dir}/Fixtures -ea 0
}
if (Test-Path -Path $install_dir/Models) {
        mkdir -p "${chromatik_dir}/Models"  -ea 0
        cp -r $install_dir/Models/* ${chromatik_dir}/Models -ea 0
}

# copy video files over
mkdir -p "${chromatik_dir}/Videos" -ea 0 > $null
cp ../videos/* "${chromatik_dir}/Videos"
