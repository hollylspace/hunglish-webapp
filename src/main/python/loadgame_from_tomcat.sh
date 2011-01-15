#!/bin/bash

dir=$1

dep=/big3/Work/HunglishMondattar/deployment

echo Copying \"$dir\" savegame to \"$dep\"
rm -rf $dep/fileUpload
rm -rf $dep/harness.data
rm -rf $dep/hunglishIndex
rm -rf $dep/hunglishIndexTmp
rm -rf $dep/logs
cp -r $dir/fileUpload $dep/
cp -r $dir/harness.data $dep/
cp -r $dir/hunglishIndex $dep/
cp -r $dir/hunglishIndexTmp $dep/

echo Done.
