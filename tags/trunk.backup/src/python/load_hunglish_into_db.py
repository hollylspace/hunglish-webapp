#!/usr/bin/python

import MySQLdb
import sys

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

'''retrive the ID of a record or, if it does not exists then create it and return its ID'''
def handleMetaData(cursor, tableName, value):
    if cursor.execute("select id from "+tableName+" where name = %s", value) > 0:
        return int(cursor.fetchmany(1)[0][0])
    else:
        cursor.execute("insert into "+tableName+" (name) values (%s)", value)
        return int(cursor.lastrowid)

'''process quality fitlered aligned file that is save its content into the database'''
def processFile(db, qfpath, genre, author, hutitle, entitle, opencontent, rawHuPath, rawEnPath, oldDocId):
    sentences = readAlignFile(qfpath, 'ISO-8859-2')
    cursor = db.cursor()
    db.begin()
    if cursor.execute("select id from doc where aligned_file_path = %s", qfpath) > 0:
        docId = int(cursor.fetchmany(1)[0][0])
        #TODO we will need a flag here to decide whether to delete or do nothing in this case
        cursor.execute("delete from bisen where doc_id = %s", docId)
        cursor.execute("delete from doc where id = %s", docId)

    authorId = handleMetaData(cursor, 'author', author)
    genreId = handleMetaData(cursor, 'genre', genre)
    cursor.execute("insert into doc (old_docid, genre_id, author_id, en_title, hu_title, is_open_content, hu_raw_file_path, en_raw_file_path, aligned_file_path) values (%s, %s, %s, %s, %s, %s, %s, %s, %s)", (oldDocId, genreId, authorId, entitle, hutitle, opencontent, rawHuPath, rawEnPath, qfpath))
    docId = cursor.lastrowid
    if len(sentences) > 0:
        cursor.executemany("insert into bisen (doc_id, line_number, hu_sentence, en_sentence) VALUES (" + str(docId) + ", %s, %s,%s)", sentences)
    db.commit()

'''Process the doclist file (sort of a catalog)
This is the format of the Doclist:
qfPath t genre t author t hutitle t entitle t opencontent t rawHuPath t rawEnPath t docid
'''
def processDoclistFile(db, path):
    lines = readfile(path, 'ISO-8859-2')
    def handleDoclistLine(line):
        params = line.encode('utf-8').split('\t')
        if len(params) == 9:
            processFile(db, params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8])
    map(handleDoclistLine, readfile(path, 'ISO-8859-2'))

def main():
    if len(sys.argv) > 4:
        username = sys.argv[1]
        password = sys.argv[2]
        database = sys.argv[3]
        db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
    if len(sys.argv) == 9:
        filepath = sys.argv[4]
        author = sys.argv[5]
        genre = sys.argv[6]
        title = sys.argv[7]
        isopen = sys.argv[8]
        processFile(db, filepath, genre, author, None, title, isopen, None, None, None)
    elif len(sys.argv) == 5:
        filepath = sys.argv[4]
        processDoclistFile(db, filepath)
    else:
        print 'usage: load_hunglish_into_db.py [username] [passwd] [db] [quality filtered file path] [author] [genre] [title] [isopen; possible values=Y,N] \n or \n load_hunglish_into_db.py [username] [passwd] [db] [Doclist file path]'
        sys.exit(-1)

if __name__ == "__main__" :
    main()

