#!/usr/bin/python

# This script just takes a tab-sep file
# and adds its content to the upload table.
# The only minor complication is that it
# has to look up the genreId. It does not
# add genres itself.
# Another, even more minor complication
# is that file sizes must be filled.

import MySQLdb
import sys

from base import *

def addOne(db,record) :
    assert len(record)==6
    author_name, hu_title, en_title, genre_name, hu_uploaded_file_path, en_uploaded_file_path = record

    # we have to find the genre id for the genre name.
    genre = lookup(db,"genre","name",genre_name)
    assert genre!=-1

    # stripped from full path.
    hu_original_file_name = hu_uploaded_file_path.split("/")[-1]
    en_original_file_name = en_uploaded_file_path.split("/")[-1]

    # we have to provide file sizes. no problem.
    hu_original_file_size = len(file(hu_uploaded_file_path).read())
    en_original_file_size = len(file(en_uploaded_file_path).read())

    cursor = getCursor(db)
    cursor.execute("insert into upload \
	( author_name, hu_title, en_title, genre, \
	hu_uploaded_file_path, en_uploaded_file_path, \
	hu_original_file_name, en_original_file_name, \
	created_timestamp, version ) \
	values ( %s, %s, %s, %s, %s, %s, %s, %s, now(), 1 )",
	( author_name, hu_title, en_title, genre,
	hu_uploaded_file_path, en_uploaded_file_path,
	hu_original_file_name, en_original_file_name ) )

def main() :
    if len(sys.argv)!=4 :
	logg("Usage: machine_upload.py username passwd db < uploadtable.txt")
	logg("uploadtable.txt is tab-separated, with the following fields:")
	logg("author_name, hu_title, en_title, genre_name, hu_uploaded_file_path, en_uploaded_file_path")
	sys.exit(-1)

    username, password, database = sys.argv[1:]
    db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
    db.set_character_set("utf8")

    try :
	for l in sys.stdin :
	    if l=="\n" or l[0]=="#" :
		continue
	    record = l.decode("utf-8").strip().split("\t")
	    assert len(record)==6
	    addOne(db,record)
    except :
	db.rollback()
	raise
    else :
	db.commit()

if __name__ == "__main__" :
    main()
