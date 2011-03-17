#!/usr/bin/python

import sys

# converts values to int if possible!
def dictFromFile(filename) :
    d = {}
    for l in file(filename) :
	a = l.strip().split("\t")
	assert len(a)==2
	try :
	    d[a[0]] = int(a[1])
	except :
	    d[a[0]] = a[1]
    return d

def main() :
    if len(sys.argv)!=4 :
	sys.stderr.write("usage: filtersen.py doc.sen hu.meta en.meta\n")
	sys.exit(-1)
    senFilename,huFilename,enFilename = sys.argv[1:]
    hu = dictFromFile(huFilename)
    en = dictFromFile(enFilename)
    huSentenceNum = hu['sentence_count_nondelimiter']
    enSentenceNum = en['sentence_count_nondelimiter']

    # TODO Ide egy csomo heurisztikat bele lehet meg tenni.
    # Kezdetnek egy:
    likeIt = True
    ratio = 1.3
    slack = 100
    if (huSentenceNum+slack)*ratio<(enSentenceNum+slack) or (huSentenceNum+slack)>(enSentenceNum+slack)*ratio :
	sys.stderr.write( "Too big difference between sentence numbers.\n" )
	likeIt = False

    huLang = hu['language_detected']
    enLang = en['language_detected']

    if huLang!='hungarian-latin2' :
	sys.stderr.write( "The text from the Hungarian side is not Hungarian latin2, but %s\n" % huLang )
	likeIt = False

    if enLang!='english' :
	sys.stderr.write( "The text from the English side is not English latin1, but %s\n" % enLang )
	likeIt = False

    if likeIt :
	sys.stdout.write(file(senFilename).read())
    else :
	sys.stderr.write( "filtersen dropped file %s\n" % senFilename )

if __name__ == "__main__" :
    main()
