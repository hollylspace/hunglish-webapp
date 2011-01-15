#!/bin/bash

set -e

dep=/big3/Work/HunglishMondattar/deployment

dir=$1

echo Copying \"$dir\" savegame to \"$dep\"
rm -rf $dep/fileUpload
rm -rf $dep/harness.data
rm -rf $dep/hunglishIndex
rm -rf $dep/hunglishIndexTmp
rm -rf $dep/logs
cp -r --preserve=timestamps $dir/fileUpload $dep/
cp -r --preserve=timestamps $dir/harness.data $dep/
cp -r --preserve=timestamps $dir/hunglishIndex $dep/
cp -r --preserve=timestamps $dir/hunglishIndexTmp $dep/

echo Done.
