#!/bin/bash

# Restores the full server state from a specified savegame directory.

set -e

dir=$1

if [ ! -d $dir ]
then
    echo The savegame path is not a directory. Stopping.
    exit -1
fi

if [ ! -d $dir/harness.data ]
then
    echo The savegame dir does not contain a harness.data directory. Stopping.
    exit -1
fi

pushd /big3/Work/HunglishMondattar/hunglish-webapp/ > "/dev/null"
mvn tomcat:stop
popd > "/dev/null"

chmod -R a+r $dir

sudo -u tomcat6 bash loadgame_from_tomcat.sh $dir

echo Loading database dump $dir/mysqldump
mysql -uhunglish -psw6x2the --default-character-set=utf8 hunglishwebapp < $dir/mysqldump

pushd /big3/Work/HunglishMondattar/hunglish-webapp/ > "/dev/null"
mvn tomcat:start
popd > "/dev/null"
