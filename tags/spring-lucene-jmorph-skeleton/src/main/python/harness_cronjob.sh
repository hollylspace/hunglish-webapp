#!/bin/bash

# control_control_harness.sh kellene legyen a neve.
# Ez a shell mar kozvetlenul elindithato cronjob-bol.

appdata_rootdir=/big3/Work/HunglishMondattar/deployment
appcode_rootdir=/big3/Work/HunglishMondattar/hunglish-webapp

logdir=$appdata_rootdir/logs
logprefix=$logdir/`date +"%Y%m%d-%H%M%S"`

harnessdatadir=$appdata_rootdir/harness.data

codedir=$appcode_rootdir/src/main/python

python $codedir/control_harness.py hunglish sw6x2the hunglishwebapp $harnessdatadir $logprefix > $logprefix.cout 2> $logprefix.cerr
