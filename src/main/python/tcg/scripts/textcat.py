import os,glob
import re

nb_ngrams = 400

class NGram:
    """ represents one language ngram """
    def __init__(self):
        self.ngrams = {}
        
    def addText(self,text,logging=False):
        """ adding text to this ngram and parsing thus """
	# TODO We should work on streams.
	tokens = text.split()
	tokenNum = len(tokens)
        for tokenPos,word in enumerate(tokens) :
	    percent = 1
	    if logging :
		if (tokenPos*100/tokenNum/percent) != ((tokenPos-1)*100/tokenNum/percent) :
		    sys.stderr.write(str(tokenPos*100/tokenNum/percent)+"% ")
            word = '_'+word+'_'
            size = len(word)
            for i in xrange(size):
                for s in (1,2,3,4):
                    sub = word[i:i+s]
                    if not self.ngrams.has_key(sub):
                        self.ngrams[sub] = 0
                    self.ngrams[sub] += 1
                    if i+s >= size:
                        break

    def addGram(self,ngrams):
        """ adding 'binary' ngram to this instance """
        self.ngrams = ngrams
        
    def buildRanks(self):
        freqList = [ (self.ngrams[k],k) for k in self.ngrams.keys() ]
        freqList.sort()
        freqList.reverse()
        freqList = freqList[:nb_ngrams]
	self.ranks = dict( [ (k,i+1) for (i,(f,k)) in enumerate(freqList) ] )
    
    def compare(self,other):
        """ compare two ngrams, self and the argument.
	Note that this is NOT a symmetric function.
	In text_cat, the new text to be classified in in the self position,
	and the old model is in the other position."""
        d = 0
        thisRanks = self.ranks
	otherRanks = other.ranks
        for gram,rank in thisRanks.iteritems() :
            if gram in otherRanks :
                d += abs(rank - otherRanks[gram])
            else:
                d += nb_ngrams
        return d


class TextClassificator:
    """ builds models from txt sources found in specified directory """
    def build(self,folder,ext='.txt',logging=True):
        self.ngrams = {}
        lst = os.listdir(folder)
        for filename in lst:
            if filename.endswith(ext):
		if logging :
		    sys.stderr.write("Processing "+filename+"\n")
                language = filename[:-len(ext)]
                fc = open(folder + '/' + filename,'r')
                text = fc.read()
                n = NGram()
                n.addText(text,logging)
		n.buildRanks()
                self.ngrams[language] = n
		if logging :
		    sys.stderr.write(" Done.\n")

    def save(self,folder,ext='.lm'):
        """ saving generated models to specified output directory """
        for lang,model in self.ngrams.iteritems() :
            fname = os.path.join(folder,lang+ext)
            file = open(fname,'w')
	    ranks = model.ranks
	    s = sorted( [ (r,k) for (k,r) in ranks.iteritems() ] )
            for i,(rank,gram) in enumerate(s) :
		assert i+1==rank
		freq = model.ngrams[gram]
                file.write( "%s\t%d\n" % (gram,freq) )
            file.close()

    def load(self,folder,ext='.lm'):
        """ 
        loading ngram rank lists (not ngrams as Pezo thought)
        """
        self.ngrams = {}
        
        lst = os.listdir(folder)
        for filename in lst:
            if filename.endswith(ext):
                model = NGram()
		model.ranks = {}
		model.ngrams = {}
                lang = filename[:-len(ext)]
                fc = open(folder + '/' + filename,'r')
                for i,line in enumerate(fc.readlines()) :
                    gram,freq = line.strip('\n').split("\t")
		    model.ngrams[gram] = int(freq)
		    model.ranks[gram] = i+1
                fc.close()
                self.ngrams[lang] = model

    def classify(self,text,verbose=False):
        """
        1. generates ngram for the given text
        2. compare generated ngram with pre loaded ngrams
        """
        
        newNgram = NGram()
        newNgram.addText( text, logging=False )
	newNgram.buildRanks()
	
	scores = {}
        for lang,ngram in self.ngrams.iteritems() :
            scores[lang] = ngram.compare(newNgram)
	if verbose :
	    sys.stderr.write( "\n".join(map(str,scores.iteritems())) + "\n" )
	score,lang = min( [ (v,k) for (k,v) in scores.iteritems() ] )
        return lang,score

if __name__ == '__main__':
    import sys
    
    trainMode = 'train'
    classifyRawMode = 'classify-raw'
    classifyLMMode = 'classify-lm'
    
    assert len(sys.argv)>1
    command = sys.argv[1]
    assert command in (trainMode,classifyRawMode,classifyLMMode)

    if command==trainMode :
	assert len(sys.argv)==4
	sourceDir,ngramDir = sys.argv[2:]
	tc = TextClassificator()
	tc.build( sourceDir, logging=True )
	tc.save(ngramDir)
	sys.exit()
    
    elif command==classifyRawMode :
	assert len(sys.argv)==3
	sourceDir = sys.argv[2]
	tc = TextClassificator()
	tc.build(sourceDir)

    elif command==classifyLMMode :
	assert len(sys.argv)==3
	ngramDir = sys.argv[2]
	tc = TextClassificator()
	tc.load(ngramDir)

    text = sys.stdin.read()
    lang,score = tc.classify(text)
    print lang
