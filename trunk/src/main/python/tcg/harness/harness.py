import os
import sys
import time
import ConfigParser

def logg(s) :
    sys.stderr.write(s+"\n")

try:
    import pp
except ImportError:
    logg('No ParallelPython support\n')


#local
import daemon
import collecting
from graph import Graph, Edge
from parallel import submit_jobs

def read_commands(f, daemon=False):
    config = ConfigParser.SafeConfigParser()
    config.read(f)
    items = config.items('commands')
    
    commands = {}
    for item in items:
        if len(item) != 2:
            raise "Wrong commands file"
        if item[0].endswith('_cmd'):
            cmd = item[1]
            start = cmd.find('[[')
            end = cmd.find(']]')
            while start>0 and end>0 and start < end:
                replace_this = cmd[start:end+2]
                if daemon:
                    replaceWith = replace_this.strip('[]').split(',')[1].strip()
                else:
                    replaceWith = replace_this.strip('[]').split(',')[0].strip()
                cmd = cmd.replace(replace_this, replaceWith)
                
                start = cmd.find('[[')
                end = cmd.find(']]')
            
            cmd = cmd.replace('../', os.getcwd() + '/')
            commands[item[0][:-4]] = cmd
    
    return commands

def get_file_list(root, edge, catalog=None):
    l = []
    
    if catalog:
        files = edge.sourceFiles(root=root, filelist=catalog)
        
        # collect file names from first source
        for f in files[0]:
            if os.path.exists(f):
                #appending only basename
                l.append(f.split('/')[-1].rsplit('.', 2)[0])
            else:
                # file is not in this source directory
                # this can happen because there may be more sources
                pass
        print l
        # checking if files exist in other sources
        for source in files[1:]:
            for f in source:
                if f.split('/')[-1].rsplit('.', 2)[0] not in l:
                    continue
                if not os.path.exists(f):
                    logg('File defined in catalog does not exist: '+f)
                    sys.exit(-1)
    else:
        # collect file names from first source
        srcdir = edge.sourceDirs(root)[0]
        
        entries = sorted(os.listdir(srcdir))
        for entry in entries:
            if entry.endswith('%(lang)s.%(what)s' % {
                               'lang': edge.sources[0]['lang'],
                               'what': edge.sources[0]['tag']
                              }
                             ):
                l.append(entry.rsplit('.', 2)[0])
                
        # checking if files exist in other sources
        sourceFiles = edge.sourceFiles(root, l)[1:]
        for source in sourceFiles:
            for filename in source:
                if not os.path.exists(filename):
                    logg('Files in different folders differ: '+filename)
                    sys.exit(-1)
    return l

def read_catalog(f):
    l = []
    for line in f:
	if len(line)==0 or line[0]=="#" :
	    continue
        s = line.strip().split()
	if len(s)>1 :
	    logg('There should be just one field per each line of a catalog file. Bad line:')
	    logg(line)
	    sys.exit(-1)
        l.append(s[0])
    return l

def processCommand(cmd):
    if not os.system(cmd) == 0:
        return False
    return True

def regenerate_file(filename, edge, root='.', dryrun='', job_server=None,
                    jobs=None):
    command = edge.command
    
    input_files = [input_file[0] for input_file in 
                   edge.sourceFiles(root=root,filelist=[filename])]
    target = edge.target
    output_file = edge.targetFiles(root=root, filelist=[filename])[0]
    runnable = False
    
    if len(input_files)==1:
        if edge.daemon:
            msg = '# processing %s to %s via %s:%d daemon' % (
                   input_files[0], output_file, 'localhost', edge.port)
            if dryrun:
                if dryrun == 'normal':
                    print msg
            else:
                if not job_server:
                    logg(msg)
                    daemon.process(input_files[0], output_file,
                                   'localhost', edge.port)
                else:
                    jobs.append((input_files[0], output_file))
            
        else:
            to_run = 'cat %(input)s | %(command)s > %(output)s' % {
                      'input': input_files[0],
                      'output': output_file,
                      'command': command
                     }
            if dryrun:
                if dryrun=='normal':
                    print to_run
            else:
                runnable = True
    elif len(input_files)>=2:
        to_run = command + ' ' + ' '.join(input_files) + ' >' + output_file
        if dryrun:
            if dryrun=='normal':
                print to_run
        else:
            runnable = True
    if runnable:
        if not job_server:
            logg(to_run)
            if not os.system(to_run) == 0:
                logg("Wrong return status of last command")
                sys.exit(-1)
        else:
            print to_run
            jobs.append( job_server.submit(processCommand, (to_run, ),
                         (processCommand, ) , ('os',) ) )

def regenerate_edge(edge, filelist, root, dryrun, job_server):
    def fileListSize(files):
        size = 0
        for f in files:
            stat = os.stat(f)
            size += stat.st_size
        return size
    
    if not edge.check_sources(filelist, root):
        return
    tgtdir = '%(root)s/%(lang)s/%(to_what)s' % {'root': root,
                                                'lang': edge.target['lang'],
                                                'to_what': edge.target['tag']
                                               }
    
    # printout
    msg = "# Doing %s" % str(edge)
    msg += ' (%s %s)' % ('.'.join([str(i) for i in time.localtime()[:3]]),
                        ':'.join([str(i) for i in time.localtime()[3:6]]))
    if dryrun:
        print msg
        if not os.path.exists(tgtdir):
            print 'mkdir %s 2>/dev/null' % tgtdir
        if hasattr(edge, 'collector') and edge.collector:
            print '# Collecting before running giza'
        else:
            #regenerating files one by one
            for f in filelist:
                regenerate_file(filename=f, edge=edge, root=root, dryrun=dryrun)
    else:
        logg(msg)
        
        # Starting daemon if needed
        if edge.daemon and not job_server:
            start_daemon(edge)
        
        if not os.path.exists(tgtdir):
            os.mkdir(tgtdir)
        
        # checking if edge is in "collector" mode
        if hasattr(edge, 'collector') and edge.collector:
            dirs_to_clear = [tgtdir]
            # collecting all the data first
            sourceDirs = edge.sourceDirs(root)
            for i, src in enumerate(edge.sources):
                dirs_to_clear.append(sourceDirs[i])
                collecting.collect(sourceDirs[i], suffix='.%s.%s' % (
                                                          src['lang'],
                                                          src['tag']
                                                          )
                                  )
            
            # run the command on the collected data
            regenerate_file(filename=collecting.DATA, edge=edge,
                            root=root, dryrun=dryrun)
            
            # separating file
            src0dir = sourceDirs[0]
            src0_catalog = '%s/%s.%s.%s' % (src0dir,
                                           collecting.CATALOG,
                                           edge.sources[0]['lang'],
                                           edge.sources[0]['tag']
                                          )
            suffix = '.%s.%s' % (edge.target['lang'], edge.target['tag'])
            collecting.separate(tgtdir, suffix=suffix, catalog=src0_catalog)
            
            collecting.clear_dirs(dirs_to_clear)
        else:
            #regenerating files one by one
            if not job_server:
                sourceFiles0 = edge.sourceFiles(root, filelist)[0]
                targetSize = fileListSize(sourceFiles0)
                actualSize = 0
                for sourceFile, filename in zip(sourceFiles0, filelist):
                    regenerate_file(filename=filename, edge=edge, root=root,
                                    dryrun=dryrun)
                    actualSize += os.stat(sourceFile).st_size
                    try:
                        percent = float(actualSize)/float(targetSize) * 100.0
                    except ZeroDivisionError:
                        sys.stdout.write('Nothing to be done. Only empty files.')
                        break
                    sys.stdout.write('%.2f%% ' % percent)
                    sys.stdout.flush()
                sys.stdout.write('\n')
            else:
                jobs = []
                sourceFiles0 = edge.sourceFiles(root, filelist)[0]
                for sourceFile, filename in zip(sourceFiles0, filelist):
                    regenerate_file(filename=filename, edge=edge, root=root,
                                    dryrun=dryrun, job_server=job_server,
                                    jobs=jobs)
                if not edge.daemon:
                    for i, job in enumerate(jobs):
                        job()
                        percent = float(i)/float(len(jobs)) * 100.0
                        sys.stdout.write('%.2f%% ' % percent)
                        sys.stdout.flush()
                    sys.stdout.write('\n')
                else:
                    submit_jobs(job_server, edge.command, 6, edge.port, jobs)

def regenerate(graph, root, dryrun, catalog, job_server):
    for edge in graph.edges:
        filelist = get_file_list(root=root, edge=edge, catalog=catalog)
        regenerate_edge(edge=edge, filelist=filelist, root=root, dryrun=dryrun,
                        job_server=job_server)
        sys.stdout.write('# %s done\n' % str(edge))
        t = '%s %s' % ('.'.join([str(i) for i in time.localtime()[:3]]),
                     ':'.join([str(i) for i in time.localtime()[3:6]]))
        sys.stdout.write('# %d out of %d edges done at %s\n' % (
                          graph.edges.index(edge)+1, len(graph.edges), t)
                        )
                        

def start_daemon(edge):
    d = daemon.Daemon(edge.host, edge.port, edge.command)
    logg('Starting daemon with the following command:\n%s' % edge.command )
    d.start()

from optparse import OptionParser

parser = OptionParser()

parser.add_option("-g", "--graph", dest="graph",
                  help="read dependency graph from FILE", metavar="FILE")
parser.add_option("-c", "--commands", dest="command_file",
                  help="read commands from FILE", metavar="FILE")
parser.add_option("-r", "--root", dest="root_dir",
                  help="root directory", metavar="DIR")
parser.add_option("-d", "--dryrun", dest="dryrun",
                  help="dryrun in normal or brief mode", metavar='MODE',
                  choices=['normal', 'brief'])
parser.add_option("-l", "--language", dest="lang", help="language")
parser.add_option("-w", "--what", dest="what", help="what to regenerate")
parser.add_option("-n", "--norecursion", dest="norecursion",
                  action='store_true', help="skip recursive generation")
parser.add_option("", "--catalog", dest="catalog", help="catalog file")
parser.add_option("", "--daemon", dest="daemon", action='store_true',
                  help="process files with daemons if they are available")
parser.add_option("-p", "--parallel", dest="parallel",
                  action='store_true', help="parallel processing")

(options, args) = parser.parse_args()

if not options.graph:
    raise "There is no dependency graph specified"
if not options.command_file:
    raise "There is no command file specified"
if not options.root_dir:
    raise "There is no root directory specified"

if options.catalog:
    catalog = read_catalog(file(options.catalog))
else:
    catalog = None

if bool(options.what) ^ bool(options.lang):
    logg("-w and -l work only together")
    sys.exit(-1)


graph = Graph(file(options.graph), daemon=options.daemon)
commands = read_commands(options.command_file, daemon=options.daemon)
graph.setCommands(commands=commands)
graph.order()

if options.what or options.lang:
    graph.filter(options.what, options.lang, options.norecursion)

if options.parallel:
    try:
        ppservers = ("192.168.1.1", "192.168.1.2",)
        job_server = pp.Server(0, ppservers=ppservers)
    except:
        logg('parallel calling but no ParallelPython support!')
	sys.exit(-1)
else:
    job_server = None

sys.stdout.write('%s\n' % str(graph))
regenerate(graph=graph, root=options.root_dir, dryrun=options.dryrun,
           catalog=catalog, job_server=job_server)

sys.exit(0)
# another useless change
