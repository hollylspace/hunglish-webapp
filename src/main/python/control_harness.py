#!/usr/bin/python

import MySQLdb
import sys

def getCursor(db) :
    return db.cursor(MySQLdb.cursors.SSDictCursor)

def moveFilesToHarness(db,id) :
    pass

def runHarness(db,id) :
    pass

def lookup(tableName,fieldName,value) :
    cursor = getCursor(db)
    cursor.execute("select id from %s where %s = %s", (tableName,fieldName,value) )
    rows = cursor.fetchAll()
    assert len(rows)<=1
    if len(rows)==0 :
	return -1
    else :
	return rows[0]['id']

def addAuthorIfNeeded(db,author) :
    authorId = lookup("author","name",author)
    if authorId!=-1 :
	return authorId
    cursor = getCursor(db)
    # Hozzaadunk az author tablahoz.
    raise "unimplemented"
    return authorId

def metadataFromUploadToDoc(db,id) :
    cursor = getCursor(db)
    cursor.execute("select * from upload where id = %s", id)
    rs = cursor.fetchall()
    assert len(rs)==1
    r = rs[0]

    # Izombol atemeljuk:
    # id, hu_title, en_title, author, genre
    # doc.hu_raw_file_path = upload.hu_file_path, ue. en
    id = r['id']
    hu_title = r['hu_title']
    en_title = r['en_title']
    authorName = r['author']
    author = addAuthorIfNeeded(db,authorName)
    genre = lookup("genre","name",r['genre'])
    assert genre!=-1
    hu_raw_file_path = r['hu_file_path']
    en_raw_file_path = r['en_file_path']

    # Konstans:
    # old_docid, is_open_content
    old_docid = ""
    is_open_content = "false"

    # Szarmaztatjuk:
    # aligned_file_path
    aligned_file_path = ?
    
    # TODO Azert meg kellene nezni, hogy van-e mar id doc-ban.

    cursor.execute("insert into doc \
	(id, old_docid, genre, author, en_title, hu_title, \
	is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path) \
	values (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
	(id, old_docid, genre, author, en_title, hu_title,
	is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path)
    )


def harnessOutputFileToBisenTable(db,id) :
    pass

def setUploadAs(db,id,status) :
    cursor = getCursor(db)
    cursor.execute("update upload set is_processed = %s where id = %s", status, id)

def newUploads(db) :
    cursor = getCursor(db)
    unprocessed = "N"
    ids = []
    if cursor.execute("select id from upload where is_processed = %s", unprocessed) > 0 :
	ids = [ int(r["id"]) for r in cursor.fetchall() ]
    return ids

def processOneUpload(db,id) :
    db.begin()
    try :
	moveFilesToHarness(db,id)
	runHarness(db,id)
    except :
	db.rollback()
	setUploadAs(db,id,"E")
    else :
	setUploadAs(db,id,"Y")
	db.commit()

def main_egyelore(db) :
    ids = newUploads(db)
    for id in ids :
	processOneUpload(db,id)
	