#!/bin/bash

t=`tempfile`

cat > $t

# TODO Ez most nem kezeli teljesen becsuletesen az angol oldalt.
pdftotext $t - | iconv -f utf8 -t latin2 -c

