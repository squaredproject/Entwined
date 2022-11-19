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

echo "building LXF files from JSON descriptions"
python fairy_circle.py --config $install_dir/fairy_circles.json --fixtures_folder $fixtures_dir
python shrub.py --config $install_dir/shrubs.json --fixtures_folder $fixtures_dir
python tree.py --tree_config $install_dir/trees.json --branch_config $install_dir/tree_branches.csv --fixtures_folder $fixtures_dir
cp $install_dir/entwined.lxp "$HOME/Chromatik/Projects"
