
#!/bin/bash

echo "installing from directory: installing crontab: statuscake and ddptest etc $1"

if [[ ! -d $1 ]]
then
        echo "directory does not exist, try again"
        exit
fi

echo "directory exists, copying files, compiling"
cp $1/Config.java ../Trees
cp $1/entwined* ../Trees/data
../compile.sh

if test -f "${1}/custom.sh"; then
    $1/custom.sh $1
fi
