#!/bin/bash

# control_control_harness.sh
# Ez a shell mar kozvetlenul elindithato cronjob-bol.

rootdir=/big3/Work/HunglishMondattar

logdir=$rootdir/logs
logprefix=$logdir/`date +"%Y%m%d-%H%M%S"`

harnessdatadir=$rootdir/harness.data

codedir=$rootdir/hunglish-webapp/src/main/python

python $codedir/control_harness.py hunglish sw6x2the hunglishwebapp $harnessdatadir $logprefix > $logprefix.cout 2> $logprefix.cerr
