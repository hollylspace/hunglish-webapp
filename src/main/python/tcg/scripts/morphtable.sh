#!/bin/sh
# usage:
#   $1 : ocamorph binary db
morphdb=$1
ocamorph=$2

grep -v "^$" | sort -T /big1/tmp | uniq | $ocamorph --bin $morphdb --tag_preamble "" --tag_sep "	" --blocking --guess Fallback
