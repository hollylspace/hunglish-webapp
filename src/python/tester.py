#!/usr/bin/python

# import MySQL module
import MySQLdb

# connect
db = MySQLdb.connect(host="localhost", user="hunglish", passwd="", db="hunglishwebapp")

# create a cursor
cursor = db.cursor()

# execute SQL statement
cursor.execute("SELECT * FROM source")

# get the resultset as a tuple
result = cursor.fetchall()

# iterate through resultset
for record in result:
    print record[0] , "-->", record[1]

#hehehe
