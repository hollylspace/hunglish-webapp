#!/usr/bin/python

import MySQLdb
import sys
import os
import shutil

g_harnessDataDirectory = ""

def mkdir(f) :
    try :
        os.mkdir(f)
    except :
	# Ez csuf, de csak a mar letezo dir-t fedi el. Egyelore megfelel.
	pass

def cp(f1,f2) :
    print "cp "+f1+" "+f2
#    shutil.copy(f1,f2)

def getCursor(db) :
    return db.cursor(MySQLdb.cursors.SSDictCursor)

def extension(filename) :
    a = filename.split(".")
    assert len(a)>1
    return a[-1]

#TODO 'srt'
def isExtensionWeLike(ext) :
    return ext in ( 'doc','htm','html','pdf', 'rtf', 'txt', )

def moveFileToHarness(rawPath,lang,ext,id) :
    global g_harnessDataDirectory
    assert lang in ('hu','en')
    targetDir = g_harnessDataDirectory +"/"+ lang +"/"+ ext +"/"
    targetFilename = id+"."+lang+"."+ext
    cp( rawPath, targetPath+targetFilename )

def moveFilesToHarness(metadata) :
    huRawPath = metadata['hu_raw_file_path']
    enRawPath = metadata['en_raw_file_path']
    id = metadata['id']
    huExt = extension(huRawPath)
    enExt = extension(enRawPath)
    assert isExtensionWeLike(huExt)
    assert isExtensionWeLike(enExt)
    moveFileToHarness(huRawPath,'hu',huExt,id)
    moveFileToHarness(enRawPath,'en',enExt,id)

def runHarness(db,id) :
    return acceptable
    raise "unimplemented"

def lookup(tableName,fieldName,value) :
    cursor = getCursor(db)
    cursor.execute("select id from %s where %s = %s", (tableName,fieldName,value) )
    rows = cursor.fetchAll()
    assert len(rows)<=1
    if len(rows)==0 :
	return None
    else :
	return rows[0]['id']

def lastInsertId(cursor) :
    cursor.execute("select LAST_INSERT_ID()")
    rs = cursor.fetchall()
    assert len(rs)==1
    return rs[0][0]

def addAuthorIfNeeded(db,author) :
    authorId = lookup("author","name",author)
    if authorId!=None :
	return authorId
    cursor = getCursor(db)
    cursor.execute("insert into author ( name )", (author,) )
    authorId = lastInsertId(cursor)
    return authorId

def recordToDict(db,id) :
    cursor = getCursor(db)
    cursor.execute("select * from upload where id = %s", id)
    rs = cursor.fetchall()
    assert len(rs)==1
    return rs[0]

def metadataFromUpload(db,id) :
    global g_harnessDataDirectory

    metadata = {}

    r = recordToDict(db,id)

    # Izombol atemeljuk:
    # id, hu_title, en_title, genre
    # doc.hu_raw_file_path = upload.hu_file_path, ue. en
    metadata['id']       = r['id']
    metadata['hu_title'] = r['hu_title']
    metadata['en_title'] = r['en_title']
    metadata['genre'] = r['genre']
    metadata['hu_raw_file_path'] = r['hu_file_path']
    metadata['en_raw_file_path'] = r['en_file_path']
    
    # Ha uj, akkor kibovitjuk vele az author tablat.
    author = r['author']
    if author==None :
        authorName = r['author_name']
        assert authorName!=None
        author = addAuthorIfNeeded(db,authorName)
    metadata['author'] = author

    # Konstans:
    # old_docid, is_open_content
    metadata['old_docid'] = ""
    metadata['is_open_content'] = "false"

    # Szarmaztatjuk:
    # aligned_file_path
    metadata['aligned_file_path'] = g_harnessDataDirectory + "/align/qf/" + id + ".align.qf"
    
    return metadata

def metadataToDoc(db,metadata) :
# (id, old_docid, genre, author, en_title, hu_title,
# is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path)

    m = metadata
    cursor = getCursor(db)
    cursor.execute("insert into doc \
	(id, old_docid, genre, author, en_title, hu_title, \
	is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path) \
	values (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
	(m['id'], m['old_docid'], m['genre'], m['author'],
         m['en_title'],m['hu_title'],
	 m['is_open_content'], m['hu_raw_file_path'], m['en_raw_file_path'],
         m['aligned_file_path'] )
    )

'''file -> lines of the file'''
def readfile(path, enc):
    return open(path, 'r').read().decode(enc).split('\n')[:-1]

'''quality fitlered aligned file -> array of tuples holding the line number and the bisentences
this is the format of quality fitlered aligned file:
[hu sentence]\t[en sentence]
'''
def readAlignFile(path, enc):
    def getSentences(line):
        sentences = line.encode('utf-8').split('\t')
        if len(sentences) != 2:
            # TODO here we need an assert len(sentences) == 2
            return ('', '')
        else:
            return sentences
    try:
        sentences = map(getSentences, readfile(path, enc))
        numberedlines = map(lambda (a,(b,c)) : (a,b,c) , enumerate(sentences))
    except:
        numberedlines = []
    return list(numberedlines)


def harnessOutputFileToBisenTable(id,alignedFilePath) :
    sentences = readAlignFile(alignedFilePath, 'ISO-8859-2')
    cursor = db.cursor()
    db.begin()
    if len(sentences) > 0 :
        try :
            cursor.executemany("insert into bisen (doc, line_number, hu_sentence, en_sentence) VALUES (" + id + ", %s, %s,%s)", sentences)
        except :
            db.rollback()
            raise
        else :
            db.commit()

def setUploadTo(db,id,status) :
    cursor = getCursor(db)
    cursor.execute("update upload set is_processed = %s where id = %s", status, id)

def newUploads(db) :
    cursor = getCursor(db)
    processedFlag = "N" # Unprocessed
    ids = []
    if cursor.execute("select id from upload where is_processed = %s", processedFlag) > 0 :
	ids = [ int(r["id"]) for r in cursor.fetchall() ]
    return ids

class LowQualityException(Exception) :
    pass

def processOneUpload(db,id) :
    db.begin()
    try :
        metadata = metadataFromUpload(db,id)
	moveFilesToHarness(metadata)
	acceptable = runHarness(metadata)
        if acceptable :
            harnessOutputFileToBisenTable(id,metadata['aligned_file_path'])
        else :
            raise LowQualityException()
    except Exception, e :
	db.rollback()
        db.begin()
        if type(e)==LowQualityException :
            processedFlag = "L"
        else :
            processedFlag = "E"
	setUploadTo(db,id,processedFlag)
	db.commit()
    else :
        metadataToDoc(db,metadata)
        processedFlag = "Y"
	setUploadTo(db,id,processedFlag)
	db.commit()

def main_egyelore(db) :
    ids = newUploads(db)
    for id in ids :
	processOneUpload(db,id)
	
def main():
    global g_harnessDataDirectory
    if len(sys.argv)!=5 :
        print "Usage: control_harness.py username passwd db harnessDataDir"
        sys.exit(-1)
    else :
        username = sys.argv[1]
        password = sys.argv[2]
        database = sys.argv[3]
        g_harnessDataDirectory = sys.argv[4]
        db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
        
    ids = newUploads(db)
    for id in ids :
	processOneUpload(db,id)


if __name__ == "__main__" :
    main()

