import sys
for line in sys.stdin:
  if line == '\n':
    print
    continue
  l=line.strip('\n')
  if l[-1]=='\t':
    print l[:-1]
  else:
    print l