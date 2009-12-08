import libxml2

DOC = """<?xml version="1.0" encoding="UTF-8"?>
<verse>
  <attribution>Christopher Okibgo</attribution>
  <line>For he was a shrub among the poplars,</line>
  <line>Needing more roots</line>
  <line>More sap to grow to sunlight,</line>
  <line>Thirsting for sunlight</line>
</verse>
"""

doc = libxml2.parseDoc(DOC)
root = doc.children
print root
#iterate over children of verse
child = root.children
while child is not None:
    print child
    if child.type == "element":
        print "\tAn element with ", child.lsCountNode(), "child(ren)"
        print "\tAnd content", repr(child.content)
    child = child.next
doc.freeDoc()
