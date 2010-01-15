#!/usr/bin/env python

# treeviz.py
# Give this an s-expression, treebank-style parse tree on STDIN
# It will make a graphviz graphic and open it on your computer
# to look at.

# has only been tested on linux and mac
# and requires GraphViz to be installed - the 'dot' command.

from __future__ import with_statement
import sys,os,time,pprint

def parse_sexpr(s):
  s = s[s.find('('):]
  tree = []
  stack = []  # top of stack (index -1) points to current node in tree
  stack.append(tree)
  curtok = ""
  for c in s:
    if c=='(':
      new = []
      stack[-1].append(new)
      stack.append(new)
      curtok = ""
    elif c==')':
      if curtok:
        stack[-1].append(curtok)
        curtok = ""
      stack.pop()
      curtok = ""
    elif c.isspace():
      if curtok:
        stack[-1].append(curtok)
        curtok = ""
    else:
      curtok += c
  return tree[0]

def is_balanced(s):
  d = 0
  for c in s:
    if c=='(': d += 1
    if c==')': d -= 1
    if d<0: return False
  return d==0

counter = 0
def graph_tuples(node):
  # makes both NODE and EDGE tuples from the tree
  global counter
  my_id = counter
  if isinstance(node,str):
    return [("NODE", my_id, node, {'shape':'box'})]
  tuples = []
  name = node[0]
  name = name.replace("=H","")
  color = 'blue' if name=="NP" else 'black'
  tuples.append(("NODE", my_id, name, {'shape':'none','fontcolor':color}))
  
  for child in node[1:]:
    counter += 1
    child_id = counter
    opts = {}
    if len(node)>2 and isinstance(child,list) and child[0].endswith("=H"):
      opts['arrowhead']='none'
      opts['style']='bold'
    else:
      opts['arrowhead']='none'
    tuples.append(("EDGE", my_id, child_id, opts))
    tuples += graph_tuples(child)
  return tuples

def dot_tuples(tuples):
  # takes graph_tuples and makes them into graphviz 'dot' format
  dot = "digraph { "
  for t in tuples:
    if t[0]=="NODE":
      more = " ".join(["%s=%s" % (k,v) for (k,v) in t[3].items()]) 
      dot += """%s [label="%s" %s]; """ % (t[1], t[2], more)
    elif t[0]=="EDGE":
      more = " ".join(["%s=%s" % (k,v) for (k,v) in t[3].items()]) 
      dot += """ %s -> %s [%s]; """ % (t[1],t[2], more)
  dot += "}"
  return dot

def call_dot(dotstr, filename="/tmp/tmp.png", format='png'):
  with open("/tmp/tmp.dot",'w') as f:
    print>>f, dotstr
  if format=='pdf':
    os.system("dot -Teps < /tmp/tmp.dot | ps2pdf -dEPSCrop -dEPSFitPage - > " + filename)
  else:
    os.system("dot -T" +format+ " < /tmp/tmp.dot > " + filename)


if sys.platform=='darwin':
  format = "pdf"
else:
  format = "png"

def open_file(filename):
  import webbrowser
  f = "file://" + os.path.abspath(filename)
  webbrowser.open(f)
  # os.system(opener + " " + filename)

def show(sexpr, format=format):
  tree = parse_sexpr(sexpr)
  tuples = graph_tuples(tree)
  dotstr = dot_tuples(tuples)
  filename = "/tmp/tmp.%s.%s" % (time.time(),format)
  call_dot(dotstr, filename, format=format)
  open_file(filename)

def do_multi(parses):
  base = "/tmp/tmp.%s_NUM.pdf" % (time.time(),)
  for i,parse in enumerate(parses):
    if '(' not in parse: continue
    # print>>sys.stderr, (i+1),
    output = base.replace("NUM", "%.03d" % (i+1))
    call_dot(dot_tuples(graph_tuples(parse_sexpr(parse))), filename=output, format='pdf')
  # print>>sys.stderr,""
  output = base.replace("NUM","merged")
  inputs = base.replace("NUM","*")
  os.system("gs -q -dNOPAUSE -dBATCH -sDEVICE=pdfwrite -sOutputFile=%s %s" % (output,inputs))
  open_file(output)
  # os.system(opener + " " + output)
  
if __name__=='__main__':
  import sys
  input = sys.stdin.read().strip()
  lines = input.split("\n")
  #pprint.pprint(lines)
  lines = [l for l in lines if l and not l.isspace()]
  if len(lines)>1 and all(is_balanced(line) or (not (set('()') & set(line))) for line in lines):
    do_multi(lines)
    sys.exit(0)

  parse = input
  #pprint.pprint(parse_sexpr(parse))
  if '-png' in sys.argv:
    show(parse, 'png')
  elif '-eps' in sys.argv:
    show(parse, 'eps')
  elif '-pdf' in sys.argv:
    show(parse, 'pdf')
  else:
    show(parse)
