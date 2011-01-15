#!/bin/bash

# Save the full server state to a specified savegame directory.
# Note: Does not stop the server.

dir=$1

if [ -f $dir ]
then
    echo The savegame directory already exists. Stopping.
    exit -1
fi

cp -r /big3/Work/HunglishMondattar/deployment $dir

mysqldump -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp > $dir/mysqldump
