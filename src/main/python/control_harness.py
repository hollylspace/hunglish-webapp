#!/usr/bin/python

import MySQLdb
import sys
import os
import shutil

from base import *

g_harnessDataDirectory = ""
g_harnessAppDir = "/big3/Work/HunglishMondattar/tcg/harness"

def moveFileToHarness(rawPath,lang,ext,id) :
    global g_harnessDataDirectory
    assert lang in ('hu','en')
    targetDir = g_harnessDataDirectory
    mkdir(targetDir)
    targetDir += "/"+lang
    mkdir(targetDir)
    targetDir += "/"+ext
    mkdir(targetDir)
    targetFilename = str(id)+"."+lang+"."+ext
    cp( rawPath, targetDir+"/"+targetFilename )

def moveFilesToHarness(metadata) :
    huRawPath = metadata['hu_uploaded_file_path']
    enRawPath = metadata['en_uploaded_file_path']
    id = metadata['id']
    huExt = extension(huRawPath)
    enExt = extension(enRawPath)
    assert isExtensionWeLike(huExt)
    assert isExtensionWeLike(enExt)
    moveFileToHarness(huRawPath,'hu',huExt,id)
    moveFileToHarness(enRawPath,'en',enExt,id)
    logg("Finished moving files to harness.")

def collectMoreMetadata(metadata,metadataFile) :
    f = file(metadataFile)
    for l in f :
	a = l.strip("\n").split("\t")
	metadata[a[0]] = int(a[1])
    return metadata

def decideIfWorthIndexing(metadata) :
    huLines = metadata['hu_sentence_count']
    enLines = metadata['en_sentence_count']
    alignLines = metadata['align_bisentence_count']
    if alignLines >= min((huLines,enLines))*0.8 :
	keepIt = True
    else :
	keepIt = False
    keepIt = True
    metadata['keep_it'] = str(keepIt).lower()
    return metadata

def runHarness(metadata) :
    global g_harnessAppDir
    global g_harnessDataDirectory
    catalogFile = "catalog.tmp"
    
    id = metadata['id']
    
    f = file(catalogFile,"w")
    f.write(str(id)+"\n")
    f.close()    

    command = "python %s/harness.py " % g_harnessAppDir
    command += "--graph=%s/hunglishstategraph.txt " % g_harnessAppDir
    command += "--commands=%s/hunglishcommands.txt " % g_harnessAppDir
    command += "--root=%s --catalog=%s" % ( g_harnessDataDirectory, catalogFile )

    logg( command )
    doIt = True
    if doIt :
	status = os.system(command)
	if status!=0 :
	    raise Exception( "harness returned with error code"+str(status) )
    else :
	# Csak teszteleshez, ha a qf fajl mar korabban a helyere kerult.
	status = 0

    # TODO Cso"ro:zzu:k at a kimenetet valami $LOGDIR/$id.log fajlba.

    metadataFile = "%s/align/bimeta/%s.align.bimeta" % ( g_harnessDataDirectory,str(id) )
    metadata = collectMoreMetadata(metadata,metadataFile)
    metadata = decideIfWorthIndexing(metadata)
    return metadata

def addAuthorIfNeeded(db,author) :
    authorId = lookup(db,"author","name",author)
    if authorId!=None :
	return authorId
    cursor = getCursor(db)
    cursor.execute("insert into author ( name, version ) values ( %s, 1 )", author )
    db.commit()
    authorId = cursor.lastrowid
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
    metadata['hu_uploaded_file_path'] = r['hu_uploaded_file_path']
    metadata['en_uploaded_file_path'] = r['en_uploaded_file_path']
    
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
    metadata['is_open_content'] = False

    # Szarmaztatjuk:
    # aligned_file_path
    metadata['aligned_file_path'] = g_harnessDataDirectory + "/align/qf/" + str(id) + ".align.qf"
    
    return metadata

def metadataToDoc(db,metadata) :
# (id, old_docid, genre, author, en_title, hu_title,
# is_open_content, aligned_file_path)

    m = metadata
    cursor = getCursor(db)
    cursor.execute("insert into doc \
	(old_docid, genre, author, en_title, hu_title, \
	is_open_content, aligned_file_path, upload, version) \
	values (%s, %s, %s, %s, %s, %s, %s, %s, 1)",
	(m['old_docid'], m['genre'], m['author'],
         m['en_title'],m['hu_title'],
	 m['is_open_content'],
         m['aligned_file_path'], m['id'] )
    )
    docId = cursor.lastrowid
    return docId

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


class LowQualityException(Exception) :
    pass

# docId nem tevesztendo ossze id-vel, az utobbit az upload tabla
# adja autoincrementtel, az elobbit a doc.
def harnessOutputFileToBisenTable(db,docId,alignedFilePath) :
    logg( "Loading into database: "+alignedFilePath )
    sentences = readAlignFile(alignedFilePath, 'ISO-8859-2')
    cursor = getCursor(db)
    if len(sentences) > 0 :
        cursor.executemany("insert into bisen (doc, line_number, hu_sentence, en_sentence, version) \
	values (" +str(docId)+ ", %s,%s,%s,1)", sentences)
    else :
	raise Exception("There should be binsentences in the qf file.")

def setUploadTo(db,id,status) :
    cursor = getCursor(db)
    cursor.execute("update upload set is_processed = %s where id = %s", (status,id) )

def newUploads(db) :
    cursor = getCursor(db)
    processedFlag = "N" # Unprocessed
    ids = []
    if cursor.execute("select id from upload where is_processed = %s", processedFlag) > 0 :
	ids = [ r["id"] for r in cursor.fetchall() ]
    return ids

# The task of this function is to update the following fields:
# is_processed
# hu_raw_file_size, en_raw_file_size
# hu_sentence_count, en_sentence_count
# align_bisentence_count, harnessed_timestamp
def updateUploadTable(db,metadata,processedFlag) :
    cursor = getCursor(db)
    id = metadata['id']
    hu_raw_file_size = metadata['hu_raw_file_size']
    en_raw_file_size = metadata['en_raw_file_size']
    hu_sentence_count = metadata['hu_sentence_count']
    en_sentence_count = metadata['en_sentence_count']
    align_bisentence_count = metadata['align_bisentence_count']

    cursor.execute("update upload set is_processed=%s, \
	hu_raw_file_size=%s  , en_raw_file_size=%s, \
	hu_sentence_count=%s , en_sentence_count=%s, \
	align_bisentence_count=%s , harnessed_timestamp=now() \
	where id=%s",
	    (processedFlag,
	    hu_raw_file_size,en_raw_file_size,
	    hu_sentence_count,en_sentence_count,
	    align_bisentence_count,id) )

def processOneUpload(db,id) :
    try :
	logg("Looking up data from update table...")
        metadata = metadataFromUpload(db,id)
	logg("Moving document files to harness...")
	moveFilesToHarness(metadata)
	logg("Running harness...")
	metadata = runHarness(metadata)
	keepIt = metadata['keep_it']=="true"
        if keepIt :
	    logg("Adding metadata to doc table...")
    	    docId = metadataToDoc(db,metadata)
	    logg("Adding harvested bisentences to bisen table...")
            harnessOutputFileToBisenTable(db,docId,metadata['aligned_file_path'])
	    processedFlag = "Y"
        else :
	    processedFlag = "L"
	    logg("Throwing it away.")
	logg("Updating upload record with metadata...")
	updateUploadTable(db,metadata,processedFlag)
    except Exception, e :
	logg("Exception: "+str(type(e))+" "+str(e))
	db.rollback()
	logg("%d (%s) had some error, it could not be loaded into bisen." % (id,metadata['en_title']) )
        processedFlag = "E"
	setUploadTo(db,id,processedFlag)
	db.commit()
    else :
	db.commit()
	logg(str(id)+" was successfully loaded into bisen.")

# 
def indexThemLucene(db) :
    logg("Sending signal to indexer...")
    cursor = getCursor(db)
    cursor.execute("insert into job_queue (status) values ('N')")
    db.commit()

def main():
    global g_harnessDataDirectory
    if len(sys.argv)!=5 :
        logg("Usage: control_harness.py username passwd db harnessDataDir")
        sys.exit(-1)

    username = sys.argv[1]
    password = sys.argv[2]
    database = sys.argv[3]
    g_harnessDataDirectory = sys.argv[4]
    
    db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
        
    ids = newUploads(db)
    for id in ids :
	logg("Processing "+str(id)+"...")
	processOneUpload(db,id)

    indexThemLucene(db)

if __name__ == "__main__" :
    main()
