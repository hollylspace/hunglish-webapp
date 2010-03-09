#!/usr/bin/python

# import MySQL module
import MySQLdb
import sys

'''quality fitlered aligned file -> array of tuples holding the line number and the bisentences
this is the format of quality fitlered aligned file:
[hu sentence]\t[en sentence]
'''
def readAlignFile(name, enc):
    def getSentences(line):
        sentences = line.encode('utf-8').split('\t')
        if len(sentences) != 2:
            # TODO here we need an assert len(sentences) == 2
            return ('', '')
        else:
            return sentences
    sentences = map(getSentences, open(name, 'r').read().decode(enc).split('\n'))
    numberedlines = map(lambda (a,(b,c)) : (a,b,c) , enumerate(sentences))
    return list(numberedlines)

'''retrive the ID of a record or, if it does not exists then create it and return its ID'''
def handleMetaData(cursor, tableName, value):
    if cursor.execute("select id from "+tableName+" where name = %s", value) > 0:
        return cursor.fetchmany(1)[0]
    else:
        cursor.execute("insert into "+tableName+" (name) values (%s)", value)
        return cursor.lastrowid

'''process quality fitlered aligned file that is save its content into the database'''
def processFile(username, password, database, filepath, author, genre, title):
    sentences = readAlignFile(filepath, 'ISO-8859-2')
    # connect
    db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
    
    # create a cursor
    cursor = db.cursor()
    db.begin()
    if cursor.execute("select id from doc where aligned_file_path = %s", filepath) > 0:
        docId = cursor.fetchmany(1)[0]
    else:
        authorId = handleMetaData(cursor, 'author', author)
        genreId = handleMetaData(cursor, 'genre', genre)
        cursor.execute("insert into doc (genre_id, author_id, title, aligned_file_path) values (%s, %s, %s, %s)", (genreId, authorId, title, filepath))
        docId = cursor.lastrowid
    cursor.executemany("insert into bisen (doc_id, line_number, hu_sentence, en_sentence) VALUES (" + str(docId) + ", %s, %s,%s)", sentences)
    db.commit()


def main() :
    if len(sys.argv) == 8:
        username = sys.argv[1]
        password = sys.argv[2]
        database = sys.argv[3]
        filepath = sys.argv[4]
        author = sys.argv[5]
        genre = sys.argv[6]
        title = sys.argv[7]

        processFile(username, password, database, filepath, author, genre, title)
    else:
        print 'usage: load_hunglish_into_db.py [username] [passwd] [db] [filepath] [author] [genre] [title]'
        sys.exit(-1)

if __name__ == "__main__" :
    main()

