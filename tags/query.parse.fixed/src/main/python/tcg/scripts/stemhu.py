import sys
posFile = file(sys.argv[1])
disambigFile = file(sys.argv[2])
posLine = posFile.readline()
disambigLine = disambigFile.readline()
while not posLine == '' or disambigLine == '':
    posLine = posLine.strip()
    disambigLine = disambigLine.strip()
    
    posElements = posLine.split()
    disambigElements = disambigLine.split()
    
    to_write = []
    if len(posElements)<2:
        pass
    else:
        to_write += posElements[:2]
        stem = ''.join([part.split('/')[0] for part in disambigElements[1].split('+')])
        if stem == '':
            stem = posElements[0]
        to_write.append(stem)
    print '\t'.join(to_write)
    posLine = posFile.readline()
    disambigLine = disambigFile.readline()
    if posLine == '' or not posLine.endswith('\n'):
        break
