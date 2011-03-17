#!/bin/sh
# usage:
#   $1 : model file
#   $2 : input file
#   $3 : morphtable
model=$1
input=$2
morphtable=$3

disambig=`mktemp`
hundisambig --morphtable $morphtable --tagger-model $model --lowercase no < $input > $disambig

first=`mktemp`
cut -f1 < $disambig >$first

second=`mktemp`
cut -f2 < $disambig | sed "s/||$//" | sed "s/|\+/\//g" | tr '@' '+' > $second
paste $first $second | sed "s/^\t$//g"
rm $first
rm $second
rm $disambig