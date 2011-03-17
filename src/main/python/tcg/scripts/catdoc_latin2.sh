#!/bin/bash

# This should be simply
# catdoc -dISO-8859-2
# , but that does not work. (F*cks o" and u")

catdoc -dutf-8 | ( iconv --f utf8 --t latin2 -c || true )
