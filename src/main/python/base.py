#!/usr/bin/python

import MySQLdb
import sys
import os
import shutil
import datetime

def logg(s) :
    sys.stderr.write(str(s)+"\n")

def loggnow(s) :
    sys.stderr.write( str(datetime.datetime.now()) + " : " + s + "\n" )

def mkdir(f) :
    try :
        os.mkdir(f)
    except :
	# Ez csuf, de csak a mar letezo dir-t fedi el. Egyelore megfelel.
	pass

def cp(f1,f2) :
    logg( "cp "+f1+" "+f2 )
    shutil.copy(f1,f2)

def getCursor(db) :
    return db.cursor(MySQLdb.cursors.SSDictCursor)

def extension(filename) :
    a = filename.split(".")
    assert len(a)>1
    return a[-1]

def isExtensionWeLike(ext) :
    return ext in ( 'doc','htm','html','pdf', 'rtf', 'txt', 'srt' )

def lookup(db,tableName,fieldName,value) :
    cursor = getCursor(db)
    command = 'select id from %s where %s = ' % (tableName,fieldName)
    cursor.execute( command+' %s', value )
    rows = cursor.fetchall()
    assert len(rows)<=1
    if len(rows)==0 :
	return None
    else :
	return rows[0]['id']
