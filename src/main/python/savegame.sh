#!/bin/bash

# Save the full server state to a specified savegame directory.
# Note: Does not stop the server.

set -e

dep=/big3/Work/HunglishMondattar/deployment

dir=$1

if [ -f $dir ]
then
    echo The savegame directory already exists. Stopping.
    exit -1
fi

cp -r --preserve=timestamps $dep $dir

mysqldump -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp > $dir/mysqldump
