# Azokat a sorokat engedi at, ahol a latin2 kodolas szerinti alfabetikus
# karakterek aranya legalabb 2/3, nem beleszamolva a space-eket es irasjeleket,
# plane nem a szamjegyeket.

import sys
import locale
import math

locale.setlocale(locale.LC_ALL, 'hu_HU.ISO8859-2')

threshold = 0.85

# s bytestream, a fuggveny feladata konvertalni. 
def ratio(s,utf) :
    if len(s)==0 :
	return 0.0
    if utf :
	s = s.decode("UTF-8",'replace')

    al = sum( 1 for c in s if (c.isalpha() or c in " -,.?!'\"" ) )
    n = float(len(s))

    q = 2*math.sqrt(n)
    return (float(al)+q)/(n+q)

def main() :
    if len(sys.argv)>1 :
	assert len(sys.argv)==2
	assert sys.argv[1]=="--utf8"
	utf = True
    else :
	utf = False

    if not utf :
	locale.setlocale(locale.LC_ALL, 'hu_HU.ISO8859-2')

    for l in sys.stdin :
	l = l.strip("\n")
	a = l.split("\t")
	assert len(a)==2
	hu,en = a
	r = ratio(hu+en,utf)
	# rhu = ratio(hu)
	# ren = ratio(en)
	# rmin = min((rhu,ren))
	# print "%f\t%f\t%s" % (rmin,r,l)
	if r>=threshold :
	    print l

main()
