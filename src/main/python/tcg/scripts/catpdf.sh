#!/bin/bash

t=`tempfile`

cat > $t

pdftotext $t -