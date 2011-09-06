import sys

def main() :
    assert len(sys.argv)==3
    f1,f2 = sys.argv[1:]
    d1 = file(f1).readlines()
    d2 = file(f2).readlines()
    if len(d1)!=len(d2) :
	sys.stderr.write("Number of lines differ in translation memory halves: %d versus %d.\n" % (len(d1),len(d2)) )
	sys.exit(-1)
    for l1,l2 in zip(d1,d2) :
	print l1.strip("\n") +"\t"+ l2.strip("\n")

main()
