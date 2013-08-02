#!/usr/bin/python

import sys
import os
import shutil

# Figyelem, az angol fajlnevek vegerol a hunglish2.justlaw miatt
# lecsippentunk egy _en vegzodest.

def mkdir(f) :
    try :
        os.mkdir(f)
    except :
        # Ez csuf, de csak a mar letezo dir-t fedi el. Egyelore megfelel.
        pass
    assert os.path.isdir(f)

def cp(f1,f2) :
    shutil.copy(f1,f2)
                                            
def addOne(target,record) :
    assert len(record)==10
    ( author_name, hu_title, en_title, genre_name, 
      hu_uploaded_file_path, en_uploaded_file_path,
      copyright, old_docid, approved, boostStr ) = record
    boost = float(boostStr)
    assert copyright in "COP" # Copyrighted, Open license, Public domain
    assert approved in "YN" # yes/no
    # stripped from full path.
    hu_original_file_name = hu_uploaded_file_path.split("/")[-1]
    en_original_file_name = en_uploaded_file_path.split("/")[-1]

    hu_base,hu_ext = hu_original_file_name.split(".")
    en_base,en_ext = en_original_file_name.split(".")
    
    # Figyelem, kivetelkod!
    if en_base.endswith("_en") and hu_base.endswith("_hu") :
	en_base = en_base[:-3]
    
    hu_target_dir = target +"/hu/"+ hu_ext
    en_target_dir = target +"/en/"+ en_ext
    hu_new_base    = en_base +".hu."+ hu_ext
    en_new_base    = en_base +".en."+ en_ext

    hu_target = hu_target_dir +"/"+ hu_new_base
    en_target = en_target_dir +"/"+ en_new_base

    cp(hu_uploaded_file_path,hu_target)
    cp(en_uploaded_file_path,en_target)
    
    print "%s\t%s\t%s\t%s\t%s\t%s\t%s" % ( author_name, hu_title, en_title, genre_name, copyright,
	"hu/"+ hu_ext +"/"+ hu_new_base,
	"en/"+ en_ext +"/"+ en_new_base )


def main() :
    assert len(sys.argv)==2
    target = sys.argv[1]

    for lang in ("hu","en") :
	mkdir(target+"/"+lang)
	for ext in ("doc","htm","html","pdf","rtf","srt","tm","txt") :
	    mkdir(target+"/"+lang+"/"+ext)

    for l in sys.stdin :
	if l=="\n" or l[0]=="#" :
	    continue
	record = l.decode("utf-8").strip().split("\t")
	addOne(target,record)

if __name__ == "__main__" :
    main()

