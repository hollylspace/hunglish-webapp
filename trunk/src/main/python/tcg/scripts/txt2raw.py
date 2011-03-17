
# Reads unformatted text, autodetects its encoding,
# and dumps it as latin1 or latin2.

import sys
import chardet

def main() :
    if len(sys.argv)!=2 or not(sys.argv[1] in ('hu','en')) :
	sys.stderr.write("Usage: txt2raw.py ( hu | en )")
	sys.exit(-1)
    lang = sys.argv[1]
    assert lang in ('hu','en')
    outEnc = 'ISO-8859-2' if lang=='hu' else 'ISO-8859-1'

    s = sys.stdin.read()
    result = chardet.detect(s)
    inEnc = result['encoding']
    sys.stderr.write("Input encoding %s detected.\n" % inEnc)

    # TODO Lehetne egy kicsivel megengedobb esetleg.
    if inEnc!="utf-8" and inEnc!=outEnc :
	sys.stderr.write( "This looks suspicious. Overriding it to %s, thus doing nothing.\n" % outEnc )
	inEnc = outEnc
    
    if inEnc==outEnc :
	sys.stdout.write(s)
    else :
	s = s.decode(inEnc, 'ignore')
	s = s.encode(outEnc,'ignore')
	sys.stdout.write(s)

if __name__ == "__main__" :
    main()
