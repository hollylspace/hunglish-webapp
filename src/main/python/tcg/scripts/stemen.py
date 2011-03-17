import sys
posFile = file(sys.argv[1])
morphtableFile = file(sys.argv[2])

def processMorphtable(f):
    d = {}
    for l in f:
        elements = l.split()
        if len(elements)<2:
            continue
        surface = elements[0]
        tokens = []
        for tok in elements[1:]:
            tokens.append(tok)
        if len(set(tokens))>1:
            shortest = None
            deepest = ''
            for tok in tokens:
                if tok.count('<') > deepest.count('<'):
                    deepest = tok
                elif tok.count('<') == deepest.count('<'):
                    if len(tok)<len(deepest):
                        deepest = tok
                if shortest is None:
                    shortest = tok
                elif len(tok)<len(shortest):
                    shortest = tok
            d[surface] = deepest.split('<')[0] # place to change to shortest
        elif len(set(tokens))==1:
            if tokens[0] == 'UNKNOWN':
                d[surface] = surface
            else:
                d[surface] = tokens[0].split('<')[0]
    return d

stems = processMorphtable(morphtableFile)
for line in posFile:
    elements = line.strip().split()
    if len(elements)<2:
        print ''
        continue
    elements.append(stems[elements[0]])
    print '\t'.join(elements)
