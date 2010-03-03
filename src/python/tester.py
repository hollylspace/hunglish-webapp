#!/usr/bin/python

# import MySQL module
import MySQLdb
import sys

'''file -> array holding the lines of the file'''
def readfile(name):
    # Open the input files and read lines
    infile = file(name, 'r')
    return map( lambda s : s.strip("\n"), infile.readlines() )

def testDBConn(username, password, database):
    # connect
    db = MySQLdb.connect(host="localhost", user=username, passwd=password, db=database)
    
    # create a cursor
    cursor = db.cursor()
    
    # execute SQL statement
    cursor.execute("SELECT * FROM source")
    
    # get the resultset as a tuple
    result = cursor.fetchall()
    
    # iterate through resultset
    for record in result:
        print record[0] , "-->", record[1]


def main() :
    if len(sys.argv) == 4:
        username = sys.argv[1]
        password = sys.argv[2]
        database = sys.argv[3]
        testDBConn(username, password, database)
    else:
        print 'usage: ***.py [username] [passwd] [db]'
        sys.exit(-1)

if __name__ == "__main__" :
    main()

