#!/bin/bash

# control_control_harness.sh kellene legyen a neve.
# Ez a shell mar kozvetlenul elindithato cronjob-bol.

appdata_rootdir=/big3/Work/HunglishMondattar/deployment
appcode_rootdir=/big3/Work/HunglishMondattar/hunglish-webapp

logdir=$appdata_rootdir/logs
mkdir $logdir
mkdir $logdir/cout

logdate=`date +"%Y%m%d-%H%M%S"`
# TODO ha mar letezik 

harnessdatadir=$appdata_rootdir/harness.data

codedir=$appcode_rootdir/src/main/python

python $codedir/control_harness.py hunglish sw6x2the hunglishwebapp $harnessdatadir $logdir/harness $logdate > $logdir/cout/$logdate.cout 2> $logdir/$logdate.cerr
