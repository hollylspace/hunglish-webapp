# Azokat a sorokat engedi at, ahol a latin2 kodolas szerinti alfabetikus
# karakterek aranya legalabb 2/3, nem beleszamolva a space-eket es irasjeleket,
# plane nem a szamjegyeket.

import sys
import locale
import math

locale.setlocale(locale.LC_ALL, 'hu_HU.ISO8859-2')

alphaThreshold  = 0.6
usefulThreshold = 0.85

def charNums(s) :
    alphabeticCharNum = sum( 1 for c in s if (c.isalpha() ) )
    usefulCharNum =     sum( 1 for c in s if (c.isalpha() or c in " -,.?!'\"" ) )
    totalCharNum = float(len(s))
    return alphabeticCharNum,usefulCharNum,totalCharNum

# s bytestream, a fuggveny feladata konvertalni. 
def ratio(s,utf) :
    if len(s)==0 :
	return 0.0
    if utf :
	# Az U+FFFD 'REPLACEMENT CHARACTER'-t rakja a helyukre.
	s = s.decode("UTF-8",'replace')

    alphabeticCharNum,usefulCharNum,totalCharNum = charNums(s)
    deviation = 2*math.sqrt(totalCharNum)
    return (float(alphabeticCharNum)+deviation)/(totalCharNum+deviation),\
	   (float(usefulCharNum)    +deviation)/(totalCharNum+deviation)

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
	huAlphaRatio,huUsefulRatio = ratio(hu,utf)
	enAlphaRatio,enUsefulRatio = ratio(en,utf)
	alphaRatio = min((huAlphaRatio,enAlphaRatio))
	usefulRatio = min((huUsefulRatio,enUsefulRatio))
	debugMode = True
	if debugMode :
	    print "%f\t%f\t%s" % (alphaRatio,usefulRatio,l)
	else :
	    if alphaRatio>=alphaThreshold and usefulRatio>=usefulThreshold :
		print l

main()
