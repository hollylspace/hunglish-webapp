import os
import sys

class Edge:
    def __init__(self):
        self.sources = []
        self.target = None
        self.command = None
        self.daemon = False
    
    def __str__(self):
        s = '%(src)s -> %(tgt)s'
            
        stringdict = {} 
        stringdict['src'] = "(%s)" % ' '.join(
                                ["%s/%s" % (src['lang'], src['tag'])
                                      for src in self.sources
                                ])
        stringdict['tgt'] = "%s/%s" % (self.target['lang'],
                                       self.target['tag'])
        s = s % stringdict
        return s
    
    def add_source(self, tag, lang):
        self.sources.append({'tag': tag, 'lang': lang})
    
    def set_target(self, tag, lang):
        self.target = {'tag': tag, 'lang': lang}
    
    def set_command(self, command):
        self.command = command
    
    def check_sources(self, filelist, root):
        for source in self.sources:
            for f in filelist:
                _dir = root + '/' + source['lang'] + '/' + source['tag']
                filename = '%s/%s.%s.%s' % (
                                       _dir, f, source['lang'], source['tag'])
                if not os.path.exists(filename):
                    sys.stderr.write('Not exists: %s\n' % filename)
                    return False
        
        return True
    
    def sourceDirs(self, root):
        dns = []
        for source in self.sources:
            d = '%(root)s/%(lang)s/%(what)s' % {'root': root,
                                                'lang': source['lang'],
                                                'what': source['tag']
                                               }
            dns.append(d)
        return dns
    
    def targetDir(self, root):
        return '%(root)s/%(lang)s/%(what)s' % {'root': root,
                                               'lang': self.target['lang'],
                                               'what': self.target['tag']
                                               }
    
    def sourceFiles(self, root, filelist):
        allFiles = []
        for source in self.sources:
            sourceFiles = []
            for f in filelist:
                filename = '%(root)s/%(lng)s/%(w)s/%(fn)s.%(lng)s.%(w)s' % {
                            'lng': source['lang'],
                            'w': source['tag'],
                            'root': root,
                            'fn': f
                           }
                sourceFiles.append(filename)
            allFiles.append(sourceFiles)
        return allFiles
    
    def targetFiles(self, root, filelist):
        outputNames = []
        for f in filelist:
            filename = '%(root)s/%(lng)s/%(w)s/%(fn)s.%(lng)s.%(w)s' % {
                        'lng': self.target['lang'],
                        'w': self.target['tag'],
                        'root': root,
                        'fn': f
                       }
            outputNames.append(filename)
        return outputNames

class Graph:
    def __init__(self, filename, daemon=False):
        self.edges = []
        priority = 0
        nextport = 10000
        for line in filename:
            if len(line.strip()) < 1:
                continue
            if line.strip()[0] == '#':
                continue
            e = Edge()
            self.edges.append(e)
            e.priority = priority
            priority += 1
            
            # sources
            sources = [tok.strip() for tok in line.split('->')[0].split(',')]
            for source in sources:
                s = source.split('/')
                e.add_source(tag=s[1].strip(), lang=s[0].strip())
            
            # target
            try:
                target = line.split('->')[1].split(':')[0].strip()
            except IndexError:
                sys.stderr.write('Missing "->" in a command')
                sys.exit()
            try:
                s = target.split('/')
                e.set_target(tag=s[1].strip(), lang=s[0].strip())
            except IndexError:
                sys.stderr.write('Missing "/" in a command')
                sys.exit()
            
            #command
            try:
                command = line.split(':')[1].split('(')[0].strip()
                e.set_command(command)
            except IndexError:
                sys.stderr.write('Missing ":" in a command')
                sys.exit()
            
            #daemon
            if daemon:
                try:
                    s = line.split('(')[1].split(')')[0].strip()
                except IndexError:
                    e.daemon = False
                    s = ''
                if 'daemon' in s:
                    e.daemon = True
                    if '=' in s:
                        try:
                            e.host = s.split('=')[1].split(':')[0].strip()
                            e.port = int(s.split('=')[1].split(':')[1].strip())
                        except IndexError:
                            pass
                    else:
                        e.host = 'localhost'
                        e.port = nextport
                        nextport += 1
            else:
                e.daemon = False
            
            # collector/separator
            try:
                s = line.split('(')[1].split(')')[0].strip()
            except IndexError:
                e.collector = False
            if 'collect' in s:
                e.collector = True
    
    def __str__(self):
        s = '# Edges to do:\n'
        for i, edge in enumerate(self.edges):
            s += '#\t%s\n' % str(edge)            
        return s
    
    def setCommands(self, commands):
        for edge in self.edges:
            edge.set_command(commands[edge.command])
    
    def getSpecificEdges(self, lang, target):
        e = set([])
        for edge in self.edges:
            if edge.target['tag'] == target and edge.target['lang']==lang:
                e.add(edge)
        return e
    
    def __nextEdge__(self, done):
        # All edges
        toDo = []
        for edge in self.edges:
            toDo.append(edge)
            
        # Remove edges that are done
        toDo = set(toDo) - set(done)
        
        # collect edge targets because they are not complete (in toDo)
        toDo_targets = []
        for e in toDo:
            if not e.target in toDo_targets:
                toDo_targets.append(e.target)
        
        # go and find a good edge
        edgeToReturn = None
        while not edgeToReturn:
            # finds the min priority
            minPrior = min([edge.priority for edge in toDo])
            
            # go through edges
            for edge in toDo:
                # see only minPrior edges
                if edge.priority == minPrior:
                    # marks ready only if none of the sources are in toDo
                    ready = True
                    for src in edge.sources:
                        if src in toDo_targets:
                            ready = False
                            break
                    if ready:
                        edgeToReturn = edge
                        break
                    else:
                        edge.priority += 1
        
        return edgeToReturn
    
    def order(self):
        done = set([])
        edges = []
        while len(edges) < len(self.edges):
            edge = self.__nextEdge__(done)
            done.add(edge)
            edges.append(edge)
        
        self.edges = edges
    
    def filter_by_edges(self, edges):
        while True:
            to_add = []
            done = [srcEdge.target for srcEdge in edges]
            for e in self.edges:
                if e in edges:
                    continue
                good = False
                for src in e.sources:
                    if src in done:
                        good = True
                        break
                if good:
                    to_add.append(e)
            if len(to_add) < 1:
                break
            for ta in to_add:
                edges.append(ta)
        self.edges = edges
        self.order()
                
    
    def filter(self, target, lang, norecursion):
        edges = []
        for edge in self.edges:
            if ( (edge.target['tag'] == target or target == 'all') and
                 (edge.target['lang'] == lang or lang == 'all') ):
                edges.append(edge)
        if not norecursion:
            self.filter_by_edges(edges)
        else:
            self.edges = edges
            self.order()

