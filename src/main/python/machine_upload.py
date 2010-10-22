#!/usr/bin/python

# This script just takes a tab-sep file
# and adds its content to the upload table.
# The only minor complication is that it
# has to look up the genreId. It does not
# add genres itself.

import MySQLdb
import sys

from base import *

def main() :
    if len(sys.argv)!=4 :
	logg("Usage: machine_upload.py username passwd db < uploadtable.txt")
	sys.exit(-1)

    username, password, database = sys.argv[1:]
    db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
    for l in sys.stdin :
	if l=="\n" or l[0]=="#" :
	    continue
	a = l.strip().split("\t")
	assert len(a)==?
	

if __name__ == "__main__" :
    main()
