#!/usr/bin/python

import sys

# converts values to int!
def listFromFile(filename) :
    d = []
    for l in file(filename) :
	a = l.strip().split("\t")
	assert len(a)==2
	try :
	    val = int(a[1])
	except :
	    val = a[1]
	d.append( (a[0], val) )
    return d

def main() :
    argv = sys.argv[:]
    if not ( len(argv)==4 or (len(argv)==5 and argv[1]=="--utf8") ) :
	sys.stderr.write("usage: bimeta.py [ --utf8 ] align.qf hu.meta en.meta\n")
	sys.exit(-1)
    utf = ( len(argv)==5 )
    if utf :
	argv = argv[2:]
    else :
	argv = argv[1:]
    qfFilename,huFilename,enFilename = argv
    hu = listFromFile(huFilename)
    en = listFromFile(enFilename)
    huLines = dict(hu)['sentence_count']
    enLines = dict(en)['sentence_count']
    
    lines = 0
    bytes = 0
    for l in file(qfFilename) :
	if utf :
	    l = l.decode("UTF-8",'ignore')
	lines += 1
	bytes += len(l)
	# TODO Ide sokmindent lehet es erdemes me'g tenni.

    for k,v in hu :
	print "hu_%s\t%s" % (k,str(v))
    for k,v in en :
	print "en_%s\t%s" % (k,str(v))
    print "align_bisentence_count\t%d" % lines
    print "align_size\t%d" % bytes

if __name__ == "__main__" :
    main()
