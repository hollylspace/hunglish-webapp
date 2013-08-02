#!/bin/bash

# This should be simply
# catdoc -dISO-8859-2
# , but that does not work. (F*cks o" and u")

# Some older doc and rtf files are identified as latin1 (weird, as in MS Word they look fine).
# These are then converted to utf-8, and iconv -c would lose the o" and u" characters.
# That's why this ugly sed is needed.

# o~ c3 b5
# o" c5 91
# u^ c3 bb
# u" c5 b1
# O~ c3 95
# O" c5 90
# U^ c3 9b
# U" c5 b0

catdoc -dutf-8 |\
 sed "s/\xc3\xb5/\xc5\x91/g" | sed "s/\xc3\xbb/\xc5\xb1/g" | sed "s/\xc3\x95/\xc5\x90/g" | sed "s/\xc3\x95/\xc5\xb0/g" |\
 ( iconv --f utf8 --t latin2 -c || true )
