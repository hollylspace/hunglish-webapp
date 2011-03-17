import os
import time
import signal
import socket
import subprocess
from operator import itemgetter

from daemon import process

def orderFiles(filelist):
    sizes = {}
    for f_in, f_out in filelist:
        stat = os.stat(f_in)
        sizes[(f_in, f_out)] = stat.st_size
    ordered = []
    for item in sorted(sizes.items(), key=itemgetter(1), reverse=True):
        ordered.append(item[0])
    return ordered

def splitFiles(filelist, n=6):
    orderedList = orderFiles(filelist)
    
    splitted = []
    for i in range(n):
        splitted.append([])
    
    count = 0
    reverse = False
    for f in orderedList:
        splitted[count].append(f)
        if not reverse:
            if count != n-1:
                count += 1
            else:
                reverse = True
        else:
            if count != 0:
                count -= 1
            else:
                reverse = True
    
    return tuple(splitted)

def start_daemon(cmd, port):
    basecmd = '/home/zseder/Progs/moses/np/tcg/pptest/start_daemon.py'
    torun = "%s %d \"%s\"" % (basecmd, port, cmd)
    pid = subprocess.Popen(torun, shell=True).pid
    host = socket.gethostbyname(socket.gethostname())
    return (pid, host, port)


def do_all(cmd, port, files):
    pid, host, port = start_daemon(cmd, port)
    time.sleep(60)
    for f_in, f_out in files:
        process(f_in, f_out, 'localhost', port)
    os.kill(pid, signal.SIGKILL)


def submit_jobs(job_server, cmd, n, starting_port, files):
    splitted = splitFiles(files, n)
    jobs = [job_server.submit(do_all, (cmd, starting_port+i, splitted[i]),
                              (do_all, start_daemon, process),
                              ('socket', 'subprocess', 'time', 'os', 'signal'))
            for i in range(n)]
    for job in jobs:
        job()

    


