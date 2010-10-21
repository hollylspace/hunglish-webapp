#!/usr/bin/python

import MySQLdb
import sys
import os
import shutil

g_harnessDataDirectory = ""
g_harnessAppDir = "/big1/Work/Pipeline/cvs/tcg/harness"

def logg(s) :
    sys.stderr.write(s+"\n")

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

#TODO 'srt'
def isExtensionWeLike(ext) :
    return ext in ( 'doc','htm','html','pdf', 'rtf', 'txt', )

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
    huRawPath = metadata['hu_raw_file_path']
    enRawPath = metadata['en_raw_file_path']
    id = metadata['id']
    huExt = extension(huRawPath)
    enExt = extension(enRawPath)
    assert isExtensionWeLike(huExt)
    assert isExtensionWeLike(enExt)
    moveFileToHarness(huRawPath,'hu',huExt,id)
    moveFileToHarness(enRawPath,'en',enExt,id)

def runHarness(id) :
    global g_harnessAppDir
    global g_harnessDataDirectory
    catalogFile = "catalog.tmp"

    f = file(catalogFile,"w")
    f.write(str(id)+"\n")
    f.close()    

    command = "python %s/harness.py " % g_harnessAppDir
    command += "--graph=%s/hunglishstategraph.txt " % g_harnessAppDir
    command += "--commands=%s/hunglishcommands.txt " % g_harnessAppDir
    command += "--root=%s --catalog=%s" % ( g_harnessDataDirectory, catalogFile )

    logg( command )
    doIt = False
    if doIt :
	status = os.system(command)
    else :
	# Csak teszteleshez, ha a qf fajl mar korabban a helyere kerult.
	status = 0

    # TODO Cso"ro:zzu:k at a kimenetet valami $LOGDIR/$id.log fajlba.
    return status==0

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

def addAuthorIfNeeded(db,author) :
    authorId = lookup(db,"author","name",author)
    if authorId!=None :
	return authorId
    cursor = getCursor(db)
    cursor.execute("insert into author ( name ) values ( %s )", author )
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
    metadata['is_open_content'] = False

    # Szarmaztatjuk:
    # aligned_file_path
    metadata['aligned_file_path'] = g_harnessDataDirectory + "/align/qf/" + str(id) + ".align.qf"
    
    return metadata

def metadataToDoc(db,metadata) :
# (id, old_docid, genre, author, en_title, hu_title,
# is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path)

    m = metadata
    cursor = getCursor(db)
    cursor.execute("insert into doc \
	(old_docid, genre, author, en_title, hu_title, \
	is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path, upload) \
	values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
	(m['old_docid'], m['genre'], m['author'],
         m['en_title'],m['hu_title'],
	 m['is_open_content'], m['hu_raw_file_path'], m['en_raw_file_path'],
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
        cursor.executemany("insert into bisen (doc, line_number, hu_sentence, en_sentence) VALUES (" +str(docId)+ ", %s,%s,%s)", sentences)
    else :
	raise LowQualityException()

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

def processOneUpload(db,id) :
    try :
        metadata = metadataFromUpload(db,id)
	moveFilesToHarness(metadata)
	acceptable = runHarness(id)
        if acceptable :
    	    docId = metadataToDoc(db,metadata)
            harnessOutputFileToBisenTable(db,docId,metadata['aligned_file_path'])
        else :
            raise LowQualityException()
    except Exception, e :
	logg(e)
	db.rollback()
        if type(e)==LowQualityException :
            processedFlag = "L"
	    logg("%d (%s) was not loaded into bisen, because its quality was low." % (id,metadata[en_title]) )
        else :
            processedFlag = "E"
	    logg("%d (%s) had some error, it could not be loaded into bisen." % (id,metadata[en_title]) )
	setUploadTo(db,id,processedFlag)
	db.commit()
    else :
        processedFlag = "Y"
	setUploadTo(db,id,processedFlag)
	db.commit()
	logg(str(id)+" was successfully loaded into bisen.")

def indexThemLucene() :
    # command = "java -Xmx1500M -classpath lib:hunglish-0.1.0.jar:. hu.mokk.hunglish.lucene.Launcher"
    pass

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
	processOneUpload(db,id)

    indexThemLucene()

if __name__ == "__main__" :
    main()
