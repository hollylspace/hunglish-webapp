#!/usr/bin/python

import MySQLdb
import sys
import os
import shutil

from base import *

## Globals hardwired:
#TODO remove absolute path of tomcat from here
#g_harnessAppDir = "/var/lib/tomcat6/webapps/ROOT/WEB-INF/python/tcg/harness"
g_harnessAppDir = "/srv/tomcat6/webapps/ROOT/WEB-INF/python/tcg/harness"
# Ez tobb dolgot is befolyasol: azt, hogy milyen command file-lal
# hivjuk a harnesst, es hogy a qf-et milyen kodolasunak tekinti.
# TODO Jelenleg a minosegszurest is kikapcsolja az utf8 mod.
g_isUTF8 = False

# Globals filled by the command line arguments:
g_harnessDataDirectory = ""
g_logDir = None
g_logDate = None

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
	try :
	    val = int(a[1])
	except :
	    val = a[1]
	metadata[a[0]] = val
    return metadata

def decideIfWorthIndexing(metadata) :
    id = metadata['id']
    huLines = metadata['hu_sentence_count_nondelimiter']
    enLines = metadata['en_sentence_count_nondelimiter']
    alignLines = metadata['align_bisentence_count']

    keepIt = True

    if not (alignLines>0) :
	keepIt = False
	logg("Zero aligned bisentences found. Throwing away upload #%s." % id)
    elif alignLines <= min((huLines,enLines))*0.6 :  #TODO this is the quality filter quotient, please remove this from here, put it into config
	# Jelenleg az utf8-as kodon ki kellett kommentalni ezt a minosegszurest:
	if g_isUTF8 :
	    keepIt = True
	    logg("WARNING: The ratio of unaligned sentences is too large. (huLines:%d,enLines:%d,alignLines:%d). Keeping upload #%s anyway!"
		% (huLines,enLines,alignLines,id) )
	else :
	    keepIt = False
	    logg("The ratio of unaligned sentences is too large. (huLines:%d,enLines:%d,alignLines:%d). Throwing away upload #%s."
		% (huLines,enLines,alignLines,id) )

    if keepIt :
	logg("Upload #%s successfully passed decideIfWorthIndexing()." % id)

    metadata['keep_it'] = str(keepIt).lower()
    return metadata

def runHarness(metadata) :
    global g_harnessAppDir
    global g_harnessDataDirectory
    global g_logDir
    global g_logDate
    catalogFile = g_harnessDataDirectory+"/catalog.tmp"
    
    id = metadata['id']
    
    f = file(catalogFile,"w")
    f.write(str(id)+"\n")
    f.close()    

    command = "python %s/harness.py " % g_harnessAppDir
    command += "--graph=%s/hunglishstategraph.txt " % g_harnessAppDir
    
    if g_isUTF8 :
	command += "--commands=%s/hunglishcommands.utf8.txt " % g_harnessAppDir
    else :
	command += "--commands=%s/hunglishcommands.txt " % g_harnessAppDir
    command += "--root=%s --catalog=%s" % ( g_harnessDataDirectory, catalogFile )

    if g_logDir :
	command += " > %s/cout/%s.%s.cout" % (g_logDir,g_logDate,id)
	command += " 2> %s/%s.%s.cerr" % (g_logDir,g_logDate,id)

    doIt = True
    if doIt :
	logg( command )
	status = os.system(command)
	if status!=0 :
	    raise Exception( "harness returned with error code "+str(status) )
    else :
	# Csak teszteleshez, ha a qf fajl mar korabban a helyere kerult.
	logg( "NOT EXECUTING!: "+command )
	status = 0

    metadataFile = "%s/align/bimeta/%s.align.bimeta" % ( g_harnessDataDirectory,str(id) )
    metadata = collectMoreMetadata(metadata,metadataFile)

    # Adds a keep_it key with values true or false, depending
    # on the bimeta output.
    # This functionality could be added as a second
    # harness filter, analogous to filtersen.py.
    # But that solution means one more unnecessary copy operation,
    # and gives a bit less control.
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

    # Ezt csak abban a nagyon rendkivuli esetben vesszuk at, ha
    # azt mondja, hogy egy masik control_harness peldany mar
    # elhappolta az orrunk elol. Bugnak a jele.
    if r['is_processed']!='N' :
	metadata['is_processed'] = r['is_processed']

    # Izombol atemeljuk:
    def lift(to,frm,field) :
	to[field] = frm[field]
    lift(metadata,r,'id')
    lift(metadata,r,'hu_title')
    lift(metadata,r,'en_title')
    lift(metadata,r,'genre')
    lift(metadata,r,'hu_uploaded_file_path')
    lift(metadata,r,'en_uploaded_file_path')
    lift(metadata,r,'copyright')
    lift(metadata,r,'approved')
    lift(metadata,r,'boost')

    # Ha uj, akkor kibovitjuk vele az author tablat.
    author = r['author']
    if author==None :
        authorName = r['author_name']
        assert authorName!=None
        author = addAuthorIfNeeded(db,authorName)
    metadata['author'] = author

    # Konstans:
    # old_docid
    metadata['old_docid'] = ""

    # Szarmaztatjuk:
    # aligned_file_path
    metadata['aligned_file_path'] = g_harnessDataDirectory + "/align/qf/" + str(id) + ".align.qf"
    
    return metadata

def metadataToDoc(db,metadata) :
    # Figyelem, az ujabb verzioban nem autonkrementalt doc.id plusz
    # upload.id-tol orokolt doc.upload van, hanem a doc.id manualisan
    # egyenlo"ve' van teve az upload.id-val, es nincs kulon doc.upload.
    # Remelhetoleg a mysql dob egy exceptiont ha valami brutalis
    # hiba miatt a doc.id mar foglalt.
    m = metadata
    cursor = getCursor(db)
    cursor.execute("insert into doc \
	(id, old_docid, genre, author, en_title, hu_title, \
	copyright, approved, boost, \
	aligned_file_path, version) \
	values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 1)",
	(m['id'], m['old_docid'], m['genre'], m['author'],
         m['en_title'], m['hu_title'],
	 m['copyright'], m['approved'], m['boost'],
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
        sentences = line.split('\t')
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

# Eleg bena, de 2m mondatra 140sec a futasideje, ugyhogy kit erdekel.
def normalizeSentence(sentence) :
    s = sentence.lower()

    s = "".join([ c for c in s if c==" " or c.isalnum() ])

    # I'm sorry.
    a = s.split(" ")
    s = " ".join( [ w for w in a if w!="" ] )

    return s

def normalizedHash(sentence) :
    norm = normalizeSentence(sentence)
    return hash(norm) # Lehet, hogy ez a hash nem eleg eros, nem tudom.

# Input: list of 3-length tuples (line_number, hu_sentence, en_sentence)
# Output: list of 5-length tuples (line_number, hu_sentence, en_sentence, hu_sentence_hash, en_sentence_hash)
def hashSentences(bisentences) :
    hashedBisentences = []
    for (line_number, hu_sentence, en_sentence) in bisentences :
	hu_sentence_hash = normalizedHash(hu_sentence)
	en_sentence_hash = normalizedHash(en_sentence)
	hashedBisentences.append( (line_number, hu_sentence, en_sentence, hu_sentence_hash, en_sentence_hash) )
    return hashedBisentences

def harnessOutputFileToBisenTable(db,id,alignedFilePath) :
    logg( "Loading into database: "+alignedFilePath )
    enc = 'UTF-8' if g_isUTF8 else 'ISO-8859-2'
    logg( "Encoding assumed: %s" % enc )
    bisentences = readAlignFile(alignedFilePath, enc)
    hashedBisentences = hashSentences(bisentences)
    cursor = getCursor(db)
    # state='D' means that the next task for this bisen is duplumfiltering.
    cursor.executemany("insert into bisen (doc, state, \
	line_number, hu_sentence, en_sentence, hu_sentence_hash, en_sentence_hash, version) \
	values (" +str(id)+ ",'D', %s,%s,%s,%s,%s,1)", hashedBisentences)
    return len(bisentences)

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
    # _nondelimiter means that "<p>" paragraph delimiter lines are not counted.
    hu_sentence_count = metadata['hu_sentence_count_nondelimiter']
    en_sentence_count = metadata['en_sentence_count_nondelimiter']
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
	loggnow("starting processOneUpload")
	logg("Looking up data from update table...")
        cursor = db.cursor(MySQLdb.cursors.Cursor)
        metadata = metadataFromUpload(db,id)

	if 'is_processed' in metadata :
	    # Nagyon furcsa helyzet, egy parhuzamosan futo control_harness
	    # elhappolta az orrunk elol a bemenetet.
	    logg("ERROR: for id %s, is_processed=%s. Another harness is already working on the data. We will not do anything with this upload."
		% (id,metadata['is_processed']) )
	    return

	logg("Marking ducument as under processing...")
        cursor.execute("update upload set is_processed='P' where id=%s" % id )

	logg("Moving document files to harness...")
	moveFilesToHarness(metadata)
	logg("Running harness...")
	metadata = runHarness(metadata)
	logg("Results:")
	for k,v in sorted(metadata.iteritems()) :
	    logg( "%s\t%s" % (k,str(v)) )

	keepIt = metadata['keep_it']=="true"
        if keepIt :
	    processedFlag = "Y"
	    logg("Adding metadata to doc table...")
    	    metadataToDoc(db,metadata)
	    logg("Adding harvested bisentences to bisen table...")
            bisentenceNum = harnessOutputFileToBisenTable(db,id,metadata['aligned_file_path'])
	    if bisentenceNum==0 :
		logg("WARNING: zero bisentences added. keep_it should be false in this case.")
        else :
	    processedFlag = "L"
	    logg("Throwing it away.")
	logg("Updating upload record with metadata...")
	updateUploadTable(db,metadata,processedFlag)
    except Exception, e :
	exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = exc_tb.tb_frame.f_code.co_filename
        logg("Exception %s: %s in file \"%s\", line %d" % ( str(type(e)), str(e), fname, exc_tb.tb_lineno) )
	db.rollback()
	logg("%d (%s) had some error, it could not be loaded into bisen." % (id,metadata['en_title']) )
        processedFlag = "E"
	setUploadTo(db,id,processedFlag)
	db.commit()
    else :
	db.commit()
	logg(str(id)+" was successfully loaded into bisen.")

def flagDuplicates(db) :
    logg("Flagging duplicates...")
    # 21 sec 3000 mondatot utanapakolni 2.5milliohoz a small path-on. Linearisan skalaz.
    # 5:40 es 6:30 kozotti perc akar egyetlen mondatot is utanapakolni 2.5 milliohoz a big path-on.
    # Ebbol jon ki a duplumFilterMethodThreshold, azaz hogy hany mondat felett valasszuk a big-et.
    duplumFilterMethodThreshold = 60000
    loggnow("Counting number of new sentences for duplicate index.")
    cursor = db.cursor(MySQLdb.cursors.Cursor)
    cursor.execute("select count(*) from bisen where state='D' ")
    row = cursor.fetchone()
    numberOfNew = row[0]
    doBigPath = ( numberOfNew >= duplumFilterMethodThreshold )
    loggnow("%d new sentences found. Choosing '%s' flagging method" % (numberOfNew, "big" if doBigPath else "small" ) )
    if doBigPath :
	flagDuplicatesForBigBatch(db)
    else :
	flagDuplicatesForSmallBatch(db)

def isThereAlready(cursor,huHash,enHash) :
    cursor.execute("select count(*) from bisen where state='X' and hu_sentence_hash=%s and en_sentence_hash=%s " % (huHash,enHash) )
    row = cursor.fetchone()
    numberInOld = row[0]
    cursor.execute("select count(*) from bisen where state='I' and hu_sentence_hash=%s and en_sentence_hash=%s " % (huHash,enHash) )
    row = cursor.fetchone()
    numberInNew = row[0]
    return numberInOld+numberInNew>0

def flagDuplicatesForSmallBatch(db) :
    try :
	cursor = db.cursor(MySQLdb.cursors.Cursor)
        cursor.execute("select id,hu_sentence_hash,en_sentence_hash from bisen where state='D' ")
        results = cursor.fetchall()
        dups = []
        nonDups = []
        for (id,huHash,enHash) in results :
    	    isThere = isThereAlready(cursor,huHash,enHash)
	    if isThere :
		dups.append(id)
        	cursor.execute("update bisen set is_duplicate=True, state='N' where id=%s" % id )
	    else :
		nonDups.append(id)
        	cursor.execute("update bisen set is_duplicate=False, state='I' where id=%s" % id )
	loggnow("%d duplicates and %d nonduplicates found and marked." % (len(dups),len(nonDups)) )
	logg("Done.")
    except Exception, e:
	logg("ERROR in flagDuplicates()!")
	exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = exc_tb.tb_frame.f_code.co_filename
        logg("Exception %s: %s in file \"%s\", line %d" % ( str(type(e)), str(e), fname, exc_tb.tb_lineno) )
        db.rollback()
        logg("Flagging duplicates rolled back.")
    else :
        db.commit()
        logg("Done flagging duplicates.")

def flagDuplicatesForBigBatch(db) :
    loggnow("big batch select started.")
    try :
        cursor = db.cursor(MySQLdb.cursors.Cursor)
	# A (state='D') (regebben: is_duplicate is NULL) szerinti
	# rendezes garantalja, hogy az egyforma hash-uek kozul a vegere
	# keruljenek azok, akik me'g nem estek at soha duplumszuresen.
	#
	# Az itt a jopofa trukk, hogy az egyforma hash-ueket (es indexeltsegueket)
	# mar az osszhosszuk szerint rendezi, hogy a sok nemalfanumerikust ne tolja
	# a felhasznalo arcaba feleslegesen. De ha ket kulonbozo turnusban
	# futnak be, akkor akkor is a korabbi valik reprezentanssa, ha o a
	# nagyobb karakterszamu.
        cursor.execute("""select
	    id,hu_sentence_hash,en_sentence_hash,state,is_duplicate
	    from bisen order by
	    hu_sentence_hash,en_sentence_hash,state='D',
	    CHAR_LENGTH(CONCAT(en_sentence,hu_sentence))""")
        results = cursor.fetchall()

	loggnow("select executed.")

        dupIds = []
        prevHashes = (None,None)
        newBisenNum = 0
        exampleAlreadyGiven = False
        for (id,huHash,enHash,state,isDup) in results :
            hashes = (huHash,enHash)

	    # Normalis uzemmenet mellett ez a ketto egybeesik:
	    toBeFiltered = (state=='D')
	    neverFiltered = (isDup==None)

	    if (toBeFiltered!=neverFiltered) and not exampleAlreadyGiven :
		logg("WARNING: Serious inconsistency: state=%s, is_duplicate=%s for bisen #%d" % (state,str(isDup),id) )
		exampleAlreadyGiven = True

	    if toBeFiltered :        	    
                newBisenNum += 1
                if hashes==prevHashes :
                    dupIds.append(id)
            prevHashes = hashes

        logg("%d duplicates found in %d new records. total # of records %d" % (len(dupIds),newBisenNum,len(results)) )
	loggnow("Duplicates found.")

	logg("Marking non-duplicates and setting states to 'I'.")
	# Actually, marking all, and being corrected in the next phase.
	# Ez a control_harness egyetlen olyan sora, ami miatt semmikeppen sem
	# szabad ket control_harness-nek egyszerre futnia.
	cursor.execute("update bisen set is_duplicate=False, state='I' where state='D' ")
	loggnow("Non-duplicates marked.")

	logg("Marking duplicates...")
        for id in dupIds :
            cursor.execute("update bisen set is_duplicate=True, state='N' where id=%s" % id )
	loggnow("Duplicates marked.")

	logg("Done.")
    except Exception, e:
	logg("ERROR in flagDuplicates()!")
	exc_type, exc_obj, exc_tb = sys.exc_info()
        fname = exc_tb.tb_frame.f_code.co_filename
        logg("Exception %s: %s in file \"%s\", line %d" % ( str(type(e)), str(e), fname, exc_tb.tb_lineno) )
        db.rollback()
        logg("Flagging duplicates rolled back.")
    else :
        db.commit()
        logg("Done flagging duplicates.")

def indexThemLucene(db) :
    logg("Sending signal to indexer...")
    cursor = getCursor(db)
    cursor.execute("insert into job_queue (status) values ('N')")
    db.commit()
    logg("Done.")

def main():
    global g_harnessDataDirectory
    global g_logDir
    global g_logDate
    
    if len(sys.argv) not in (5,7) :
        logg("Usage: control_harness.py username passwd db harnessDataDir [ logDir logDate ]")
	logg("If you omit the last two arguments, harness instances will log the stdout/stderr.")
	logg("If you provide them, they will log to:")
	logg(" > logDir/cout/logDate.uploadId.cout 2> logDir/logDate.uploadId.cerr")
	logg("NOTE: logDir will normally be deployment/logs/harness, NOT deployment/logs .")
        sys.exit(-1)

    username = sys.argv[1]
    password = sys.argv[2]
    database = sys.argv[3]
    g_harnessDataDirectory = sys.argv[4]
    if len(sys.argv)==7 :
    	g_logDir = sys.argv[5]
	g_logDate = sys.argv[6]
	# Jobb ketszer mint soha.
	mkdir(g_logDir)
	mkdir(g_logDir+"/cout")
	
    db = MySQLdb.connect( host="localhost",
	user=username, passwd=password, db=database )
    db.set_character_set("utf8")

    ids = newUploads(db)
    for id in ids :
	logg("Processing "+str(id)+"...")
	processOneUpload(db,id)

    flagDuplicates(db)

    indexThemLucene(db)

if __name__ == "__main__" :
    main()
