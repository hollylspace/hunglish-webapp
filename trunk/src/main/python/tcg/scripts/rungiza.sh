#!/bin/sh
rootdir=$1
factors=$2
tokhu=$3
token=$4

tmpdir=/tmp/moses_harness
mkdir $tmpdir
cp $tokhu $tmpdir/tok.hu
cp $token $tmpdir/tok.en

# deleting rootdir
if [ -d $rootdir ]; then
    rm -r -d $rootdir
fi

# redirecting stdout
out=`mktemp`
/home/zseder/.local/bin/moses-scripts/scripts-20090421-1127/training/train-factored-phrase-model.perl -scripts-root-dir /home/zseder/.local/bin/moses-scripts/scripts-20090421-1127 --root-dir $rootdir --corpus $tmpdir/tok --f hu --e en --alignment grow-diag-final-and --alignment-factors $factors --translation-factors $factors --reordering msd-bidirectional-fe --lm 0:3:/home/zseder/lm/surface.lm:0 1>$out
rm $out

# result printed to stdout
for file in $rootdir/model/aligned.*
do
    while read line
    do
        echo $line
    done < $file
done

# removing temp files
# rm -r -d -f $tmpdir
