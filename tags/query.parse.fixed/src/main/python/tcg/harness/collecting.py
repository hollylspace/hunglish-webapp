import os
import sys

CATALOG = 'catalog.txt'
DATA = 'text.txt'

def collect(directory, suffix='', text=None, catalog=None):
    if not os.path.exists(directory):
        sys.stderr.write("Directory not exist that should be collected\n")
        sys.stderr.write(directory + "\n")
        sys.exit()
    
    if catalog:
        _catalog = open(catalog, 'w')
    else:
        _catalog = open(directory + '/' + CATALOG + suffix, 'w')
    
    if text:
        _text = open(text, 'w')
    else:
        _text = open(directory + '/' + DATA + suffix, 'w')
    
    for _f in os.listdir(directory):
        if _f.startswith(CATALOG) or _f.startswith(DATA):
            continue
        
        f = directory + '/' + _f
        
        f = open(f)
        lines = f.readlines()
        _catalog.write(_f.split('.')[0] + '\t' + str(len(lines)) + '\n')
        f.close()
        for line in lines:
            _text.write(line)
    
    _text.close()
    _catalog.close()

def separate(directory, suffix='', text=None, catalog=None):
    if catalog:
        _catalog = file(catalog)
    else:
        _catalog = file(directory + '/' + CATALOG + suffix)
    
    if text:
        _text = file(text)
    else:
        _text = file(directory + '/' + DATA + suffix)
    
    _text = _text.readlines()
        
    all_line = 0
    for line in _catalog:
        name = line.split('\t')[0]
        num = int(line.split('\t')[1])
        out = open(directory + '/' + name + suffix, 'w')
        for l in _text[all_line:num+all_line]:
            out.write(l)
        out.close()
        all_line += num

def clear_dirs(dirlist):
    for d in dirlist:
        for f in os.listdir(d):
            if f.startswith(CATALOG) or f.startswith(DATA):
                os.remove(d + '/' + f)
