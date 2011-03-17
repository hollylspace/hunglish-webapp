#!/bin/sh
# the input is supposed to be tokenized
# this sript will transform the input so that every uniq word will be one line
tr ' ' '\n' | sed "s/-$//" | grep -v "^$" | sort -u 