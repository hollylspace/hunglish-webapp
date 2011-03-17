#!/usr/bin/python

import sys

import textcat

def main() :
    utf = False
    if len(sys.argv)!=2 :
	if len(sys.argv)==3 and sys.argv[2]=="--utf8" :
	    utf = True
	else :
	    sys.stderr.write("usage: meta.py textcat.models [ --utf8 ] < doc.sen\n")
	    sys.exit(-1)
    textCatModelDir = sys.argv[1]

    tc = textcat.TextClassificator()
    tc.load(textCatModelDir)

    lines = 0
    linesNonDelimiter = 0
    bytes = 0
    chars = 0
    badUTFbytes = 0
    longest = 0
    
    data = sys.stdin.read()

    lang,score = tc.classify(data)

    for line in data.split("\n") :
	l = len(line)
	lines += 1
	if line!="<p>" :
	    linesNonDelimiter += 1
	bytes += l
	if utf :
	    length = len(line.decode("UTF-8",'ignore'))
	    chars += length
	    badUTFbytes -= length
	    badUTFbytes += len(line.decode("UTF-8",'replace'))
	else :
	    chars += l
	if l>longest :
	    longest = l

    print "sentence_count\t%d" % lines
    print "sentence_count_nondelimiter\t%d" % linesNonDelimiter
    print "raw_file_size\t%d" % bytes
    print "raw_file_chars\t%d" % chars
    print "longest_line_length\t%d" % longest
    print "language_detected\t%s" % lang
    if utf :
	print "invalid_utf8_byte_count\t%s" % badUTFbytes

if __name__ == "__main__" :
    main()
