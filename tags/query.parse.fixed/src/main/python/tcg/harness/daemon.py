import socket
import popen2
from time import sleep
import sys
from threading import Thread

def check_port(host, port):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.connect((host, int(port)))
        s.close()
    except socket.error:
        return False
    return True

def wait_for_port(host, port, time=30):
    ready = False
    counter = 0
    while not ready and counter < time:
        counter += 1
        if check_port(host, port):
            ready = True
            break
        else:
            sleep(1)
    return ready

def process(file_in, file_out, host, port, wait=False):
    fo = open(file_out, 'w')
    
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((host, int(port)))
    
    #line by line sending and receiving
    buf = ''
    ready_to_send = False
    for i, line in enumerate(file(file_in)):
        if line == '\n':
            ready_to_send = True
        buf += line
        
        if not ready_to_send:
            continue
        
        sent = False
        while not sent:
            try:
                s.send(buf)
                sent = True
            except socket.timeout:
                sent = False

        got_breakline = False
        resp = ''
        while not got_breakline:        
            try:
                resp += s.recv(4096)
                if resp[-2:] == '\n\n':
                    got_breakline = True
            except socket.timeout:
                continue
        
        fo.write(resp)
        buf = ''
        ready_to_send = False
    
    fo.close()
    s.close()

class Daemon(Thread):
    def __init__(self, host, port, command):
        Thread.__init__(self)
        self.stdout, self.stdin, self.stderr = popen2.popen3(command)
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.bind((host, port))
        self.server.listen(1)
    
    def run(self):
        while True:
            conn, addr = self.server.accept()
            try:
                recv = ''
                data = ''
                while True:
                    sentence_end = False
                    
                    while not sentence_end:
                        _recv = conn.recv(4096)
                        if len(_recv)<1:
                            raise socket.error
                        recv += _recv
                        if recv.find('\n\n') > 0:
                            sentence_end = True
                            data = recv.split('\n\n')[0] + '\n\n'
                            recv = recv.split('\n\n')[1]
                    self.stdin.write(data)
                    self.stdin.flush()
                    
                    
                    got_breakline = False
                    got_back = ''
                    while not got_breakline:
                        got_back += self.stdout.readline()
                        if got_back.endswith('\n\n'):
                            got_breakline = True
                    
                    conn.send(got_back)
            except:
                continue

