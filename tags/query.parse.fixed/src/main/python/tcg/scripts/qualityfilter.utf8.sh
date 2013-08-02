# Figyelem, a hunglishcommands-ban ennek a kimenete me'g atmegy
# az enoughalpha.py-on, ami egy ronda heurisztikus keplet szerint
# eldobja azokat, akikben tul keves az alfabetikus karakter.

# Szabalyok:
# 
# Semmibol nem lehet haromnal tobb egymas utan, kiveve szamjegybol es pontbol.
# Pontbol 4-nel tobb nem lehet.
# Az Aaaaargh meg ilyesmi odavesznek, de sebaj.
#
# A | karaktert azert vesszuk ki, mert
# 1. html2text-nel tablazatokat jelent 2. feliratoknal OCR hibat.
# Nagyon keves olyat dobunk el igy, ami posztprocesszalassal mentheto lenne.
#
# Randa, nem tul kifinomult keplet szerint kidobjuk a tulsagosan kulonbozo mondathosszu parokat.
# A keplet mar regi, de most beleujitottam, hogy eldobjuk azokat, akik valamelyik oldalon
# kevesebb, mint ketbetusek.

cut -f2,3 |\
grep -v "~~~" |\
grep -v "<p>" |\
grep -v "\([^0-9.]\)\1\1\1" | grep -v "\.\.\.\.\." |\
grep -v "|" |\
LC_ALL=C awk '
BEGIN { FS="\t" }

{
    hu = length($1)
    en = length($2)
    ra = ( hu>en ? (hu+10)/(en+10) : (en+10)/(hu+10) )
    if ((ra<1.5)&&(hu>1)&&(en>1))
    {
	print $0
    }
}'
