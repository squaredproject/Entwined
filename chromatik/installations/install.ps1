#!/usr/bin/env pwsh

param ($install_dir)
# powershell knows ~ but python doesn't so use $HOME
$fixtures_dir = "$HOME/Chromatik/Fixtures/Entwined/"

if (!$install_dir) {
        echo "required parameter: directory to install"
        exit
}

echo "making fixtures from directory: $install_dir WILL REMOVE EXISTING ENTWINED FIXTURES"

if (! ( Test-Path -Path $install_dir )) {
        echo "directory does not exist, try again"
        exit
}


mkdir -p $fixtures_dir -ea 0
rm $fixtures_dir/*
rm ~/Chromatik/autoplay.lxr -ea 0
rm ~/Chromatik/Projects/entwined.lxp -ea 0

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $install_dir/fairy_circles.json --fixtures_folder $fixtures_dir
python shrub.py --config $install_dir/shrubs.json --fixtures_folder $fixtures_dir
python tree.py --tree_config $install_dir/trees.json --branch_config $install_dir/tree_branches.csv --fixtures_folder $fixtures_dir
python bench.py --config $install_dir/bench.json --fixtures_folder $fixtures_dir
cp $install_dir/entwined.lxp "$HOME/Chromatik/Projects"

if (Test-Path -Path $install_dir/config.json -PathType Leaf) {
        cp $install_dir/config.json "$HOME/Chromatik"
}

if (Test-Path -Path $install_dir/autoplay.lxr -PathType Leaf) {
        cp $install_dir/autoplay.lxr "$HOME/Chromatik" 
}
else {
        echo "no autoplay recording available, will not autoplay"
}
