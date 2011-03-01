
# Egy ilyen tablazatot:
# magyar	magyar_form	angol	angol_form
# abe-a_homok_asszonya	rtf	abe-woman_in_the_dunes	pdf
# ilyenne alakit:
# author_name hu_title en_title genre_name hu_uploaded_file_path en_uploaded_file_path copyright old_docid approved
#
# old_docid uresstringre toltodik ki, jelezve, hogy a hunglish2-ben nincs docid.
# Ugyhogy a parsolassal vigyazni kell.

# FIGYELEM: A bemeneti tablazat a cut -f1,2,4,5 paranccsal
# all elo Rung Andris master.txt formatumabol.

import sys

from base import *

def process(record,dataDir) :
    huBase,huExt,enBase,enExt = record

    a = huBase.split("-")
    if len(a)!=2 :
	logg("ERROR: Improper base filename: hu/"+huBase)
	sys.exit(-1)
    huAuthor,huTitle = a
    a = enBase.split("-")
    if len(a)!=2 :
	logg("ERROR: Improper base filename: en/"+enBase)
	sys.exit(-1)
    enAuthor,enTitle = a

    if not isExtensionWeLike(huExt) :
	logg("ERROR: Can't work with extension: "+"hu/"+huBase+"."+huExt)
	sys.exit(-1)
    if not isExtensionWeLike(enExt) :
	logg("ERROR: Can't work with extension: "+"en/"+enBase+"."+enExt)
	sys.exit(-1)

    if huAuthor!=enAuthor :
	logg("WARNING: Authors do not match, choosing English one: %s != %s" % (huAuthor,enAuthor))
    author_name = enAuthor.capitalize()
    hu_title = huTitle.replace("_"," ").capitalize()
    en_title = enTitle.replace("_"," ").capitalize()
    genre_name = "lit"
    hu_uploaded_file_path = dataDir+"/hu/"+huBase+"."+huExt
    en_uploaded_file_path = dataDir+"/en/"+enBase+"."+enExt

    try :
	assert file(hu_uploaded_file_path)
	assert file(en_uploaded_file_path)
    except Exception,e:
	logg( "ERROR: File missing: "+str(e) )
	sys.exit(-1)

    print "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s" % (
	author_name, hu_title, en_title, genre_name,
	hu_uploaded_file_path, en_uploaded_file_path,
	"C", "", "Y" ) # C=Copyrighted, ""=no old_docid, Y=approved

def main() :
    if len(sys.argv)!=2 :
	logg("usage: master2uploadtable.py data_dir < master.txt > uploadtable.txt")
	sys.exit(-1)
    dataDir = sys.argv[1]
    
    for l in sys.stdin :
	if l=="\n" or l[0]=="#" :
	    continue
	record = l.strip("\n").split("\t")
	assert len(record)==4
	process(record,dataDir)

main()
